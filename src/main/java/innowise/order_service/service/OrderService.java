package innowise.order_service.service;

import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order_items.OrderItemPatchDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.exception.order.IllegalOrderStatusException;
import innowise.order_service.exception.order.OrderNotFoundException;
import innowise.order_service.mapper.OrderItemsMapper;
import innowise.order_service.mapper.OrderMapper;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.repository.OrderRepository;
import innowise.order_service.service.kafka.KafkaProducerService;
import innowise.order_service.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ToString
public class OrderService {
    private final OrderValidator orderValidator;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemsMapper orderItemsMapper;
    private final UserServiceClient userServiceClient;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public OrderResponseDto addOrder(OrderCreateDto orderCreateDto, Long userId) {
        orderValidator.validateCreateOrderRequest(orderCreateDto, userId);

        Order order = orderMapper.toOrder(orderCreateDto);
        order.setUserId(userId);
        setOrderItems(order, orderCreateDto.getOrderItems());
        order.setCreationDate(LocalDateTime.now());

        order = orderRepository.save(order);
        log.info("Order {} created", order.getId());

        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId), countTotalAmount(order));
    }

    public OrderResponseDto getOrderById(Long orderId, Long userId) {
        Order order = loadOrderFromDatabase(orderId);

        log.info("Order {} loaded", orderId);
        orderValidator.validateOrderOwner(order, userId);

        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId), countTotalAmount(order));
    }

    public List<OrderResponseDto> getOrdersByIds(List<Long> ids, Long userId) {
        List<Order> orders = orderRepository.findOrdersByIdIn(ids);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders not found in database");
            throw new OrderNotFoundException("None of the orders with ids " + ids + " were found");
        }

        log.info("{} Orders loaded from database", orders.size());
        for (Order order : orders) {
            orderValidator.validateOrderOwner(order, userId);
        }

        UserResponseDto userResponseDto = userServiceClient.getUserById(userId);
        return orders.stream()
                .map(order -> orderMapper.toOrderResponseDto(order, userResponseDto, countTotalAmount(order)))
                .toList();
    }

    public List<OrderResponseDto> getAllUserOrders(Long userId) {
        List<Order> orders = orderRepository.findOrdersByUserId(userId);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders for user {} were not found in database", userId);
            throw new OrderNotFoundException("You haven't created any orders yet");
        }

        log.info("{} Orders loaded from database for user {}", orders.size(), userId);
        UserResponseDto userResponseDto = userServiceClient.getUserById(userId);
        return orders.stream()
                .map(order -> orderMapper.toOrderResponseDto(order, userResponseDto, countTotalAmount(order)))
                .toList();
    }

    public List<OrderResponseDto> getOrdersByStatus(String status, Long userId) {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalOrderStatusException("Illegal order status " + status +
                    ".\n Valid values are: " + Arrays.toString(OrderStatus.values()));
        }

        List<Order> orders = orderRepository.findOrdersByStatusAndUserId(orderStatus, userId);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders with status {} were not found in database", status);
            throw new OrderNotFoundException("None of the orders with status " + status + " were found");
        }
        log.info("{} Orders with {} status loaded from database", orders.size(), status);

        UserResponseDto userResponseDto = userServiceClient.getUserById(userId);
        return orders.stream()
                .map(order -> orderMapper.toOrderResponseDto(order, userResponseDto, countTotalAmount(order)))
                .toList();
    }

    @Transactional
    public OrderResponseDto updateOrder(long orderId, OrderUpdateDto orderUpdateDto, Long userId) {
        Order order = loadOrderFromDatabase(orderId);
        orderValidator.validateOrderOwner(order, userId);
        orderValidator.validateOrderStatusChange(order, orderUpdateDto);

        orderMapper.updateOrder(order, orderUpdateDto);
        if (orderUpdateDto.getOrderItems() != null && !orderUpdateDto.getOrderItems().isEmpty()) {
            orderValidator.validateOrderCanBeChanged(order);
            setOrderItems(order, orderUpdateDto.getOrderItems());
        }

        order = orderRepository.save(order);
        log.info("Order {} updated", order.getId());
        OrderResponseDto orderResponseDto = orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId), countTotalAmount(order));

        if (order.getStatus() == OrderStatus.PAYMENT_WAITING) {
            kafkaProducerService.sendCreatePaymentEvent(order, countTotalAmount(order));
        }

        return orderResponseDto;
    }

    @Transactional
    public void updateOrderStatus(long orderId, OrderStatus orderStatus) {
        Order order = loadOrderFromDatabase(orderId);

        order.setStatus(orderStatus);

        order = orderRepository.save(order);
        log.info("Order {} updated to {} status", orderId, order.getStatus());
    }

    @Transactional
    public OrderResponseDto patchCreatingOrder(long orderId, List<OrderItemPatchDto> updatedOrderItems, Long userId) {
        Order order = loadOrderFromDatabase(orderId);
        orderValidator.validateOrderOwner(order, userId);
        orderValidator.validateOrderCanBeChanged(order);

        for (OrderItemPatchDto orderItemPatchDto : updatedOrderItems) {
            applyPatchToOrderItem(order, orderItemPatchDto);
        }

        order = orderRepository.save(order);
        log.info("Order {} updated", order.getId());
        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId), countTotalAmount(order));
    }

    @Transactional
    public void deleteOrder(long orderId, Long userId) {
        Order order = loadOrderFromDatabase(orderId);
        orderValidator.validateOrderOwner(order, userId);

        orderRepository.delete(order);
        log.info("Order {} deleted", orderId);
    }

    private Order loadOrderFromDatabase(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order with {} id is not in the database", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " was not found");
        });
        log.info("Order {} loaded from database", orderId);
        return order;
    }

    private void applyPatchToOrderItem(Order order, OrderItemPatchDto orderItemPatchDto) {
        OrderItem existingItem = order.getOrderItems().stream()
                .filter(orderItem ->
                        orderItem.getItem().getId() == orderItemPatchDto.getItemId())
                .findFirst().orElse(null);
        if (orderItemPatchDto.isDeleted()) {
            order.getOrderItems().remove(existingItem);
        } else if (orderItemPatchDto.getQuantity() != null) {
            if (existingItem == null) {
                Item newItem = itemRepository.findById(orderItemPatchDto.getItemId()).orElseThrow(() ->
                        new ItemNotFoundException("Item with id " + orderItemPatchDto.getItemId() +
                                " was not found"));

                OrderItem orderItem = OrderItem.builder()
                        .quantity(orderItemPatchDto.getQuantity())
                        .item(newItem)
                        .order(order)
                        .build();
                order.getOrderItems().add(orderItem);
            } else {
                existingItem.setQuantity(orderItemPatchDto.getQuantity());
            }
        }
    }

    private BigDecimal countTotalAmount(Order order) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem orderItem : order.getOrderItems()) {
            totalAmount = totalAmount.add(orderItem.getItem().getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
        log.info("Order {} total amount is {}", order.getId(), totalAmount);
        return totalAmount;
    }

    private void setOrderItems(Order order, List<OrderItemRequestDto> orderItemDtos) {
        Map<Long, Integer> itemIdTotalQuantityMap = orderItemDtos.stream()
                .collect(Collectors.toMap(
                        OrderItemRequestDto::getItemId,
                        OrderItemRequestDto::getQuantity,
                        Integer::sum
                ));

        List<Item> items = itemRepository.findItemsByIdIn(itemIdTotalQuantityMap.keySet());

        log.info("Loaded items with ids {}", itemIdTotalQuantityMap);

        if (items.size() < itemIdTotalQuantityMap.size()) {
            log.warn("Some requested items were not found. Expected: {} items, Found: {}",
                    itemIdTotalQuantityMap.size(), items.size());
            throw new ItemNotFoundException("Some items in order were not found. Expected:" +
                    itemIdTotalQuantityMap.size() + " items, Found: " + items.size());
        }

        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        } else {
            order.getOrderItems().clear();
        }

        for (Item item : items) {
            OrderItem orderItem = OrderItem.builder()
                    .quantity(itemIdTotalQuantityMap.get(item.getId()))
                    .item(item)
                    .order(order)
                    .build();
            order.getOrderItems().add(orderItem);
        }
        log.info("Successfully linked {} items to order", items.size());
    }
}
