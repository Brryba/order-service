package innowise.order_service.service;

import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.exception.order.IllegalOrderStatusException;
import innowise.order_service.exception.order.OrderNotFoundException;
import innowise.order_service.exception.security.OrderAccessDeniedException;
import innowise.order_service.mapper.OrderItemsMapper;
import innowise.order_service.mapper.OrderMapper;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemsMapper orderItemsMapper;
    private final UserServiceClient userServiceClient;

    @Transactional
    public OrderResponseDto addOrder(OrderCreateDto orderCreateDto, String token, Long userId) {
        Order order = orderMapper.toOrder(orderCreateDto);
        order.setUserId(userId);
        setOrderItems(order, orderCreateDto.getOrderItems());
        order.setCreationDate(LocalDateTime.now());

        order = orderRepository.save(order);
        log.info("Order {} created", order.getId());

        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId, token));
    }

    public OrderResponseDto getOrderById(Long orderId, String token, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order {} not found in database", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " was not found");
        });

        log.info("Order {} loaded", orderId);
        validateOrderOwner(order, userId);

        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId, token));
    }

    public List<OrderResponseDto> getOrdersByIds(List<Long> ids, String token, Long userId) {
        List<Order> orders = orderRepository.findOrdersByIdIn(ids);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders not found in database");
            throw new OrderNotFoundException("None of the orders with ids " + ids + " were found");
        }

        log.info("{} Orders loaded from database", orders.size());
        for (Order order : orders) {
            validateOrderOwner(order, userId);
        }

        UserResponseDto userResponseDto = userServiceClient.getUserById(userId, token);
        return orders.stream()
                .map(order -> orderMapper.toOrderResponseDto(order, userResponseDto))
                .toList();
    }

    public List<OrderResponseDto> getOrdersByStatus(String status, String token, Long userId) {
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

        UserResponseDto userResponseDto = userServiceClient.getUserById(userId, token);
        return orders.stream()
                .map(order -> orderMapper.toOrderResponseDto(order, userResponseDto))
                .toList();
    }

    @Transactional
    public OrderResponseDto updateOrder(long orderId, OrderUpdateDto orderUpdateDto, String token, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order with {} id was not found in database", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " was not found");
        });

        log.info("Order {} loaded from database", orderId);
        validateOrderOwner(order, userId);

        orderMapper.updateOrder(order, orderUpdateDto);

        order = orderRepository.save(order);

        log.info("Order {} updated", orderId);

        return orderMapper.toOrderResponseDto(order, userServiceClient.getUserById(userId, token));
    }

    @Transactional
    public void deleteOrder(long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order with {} id is not in the database", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " was not found");
        });
        validateOrderOwner(order, userId);

        orderRepository.delete(order);
        log.info("Order {} deleted", orderId);
    }

    private void validateOrderOwner(Order order, Long userId) throws OrderAccessDeniedException {
        if (order.getUserId() != userId) {
            log.warn("User {} can not see the order {}", userId, order.getId());
            throw new OrderAccessDeniedException("You are only allowed to access your own orders");
        }
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

        order.setOrderItems(new ArrayList<>());
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
