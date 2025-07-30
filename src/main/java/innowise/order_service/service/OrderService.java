package innowise.order_service.service;

import innowise.order_service.dto.order.OrderRequestDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.exception.order.OrderNotFoundException;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ToString
public class OrderService {
    private final ItemService itemService;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemsMapper orderItemsMapper;

    @Transactional
    public OrderResponseDto addOrder(OrderRequestDto orderRequestDto) {
        Order order = orderMapper.toOrder(orderRequestDto);
        order.setCreationDate(LocalDateTime.now());

        setOrderItems(order, orderRequestDto.getOrderItems());

        order = orderRepository.save(order);
        log.info("Order {} created", order.getId());

        return orderMapper.toOrderResponseDto(order);
    }

    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.warn("Order {} not found in database", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " was not found");
        });

        log.info("Order {} loaded", orderId);

        return orderMapper.toOrderResponseDto(order);
    }

    public List<OrderResponseDto> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findOrdersByIdIn(ids);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders not found in database");
            throw new OrderNotFoundException("None of the orders with ids " + ids + " were found");
        }

        log.info("{} Orders loaded from database", orders.size());

        return orders.stream().map(orderMapper::toOrderResponseDto).collect(Collectors.toList());
    }

    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findOrdersByStatus(status);

        if (orders == null || orders.isEmpty()) {
            log.warn("Orders with status {} were not found in database", status);
            throw new OrderNotFoundException("None of the orders with status " + status + " were found");
        }

        log.info("{} Orders with {} status loaded from database", orders.size(), status);

        return orders.stream().map(orderMapper::toOrderResponseDto).collect(Collectors.toList());
    }

    private void setOrderItems(Order order, List<OrderItemRequestDto> orderItemDtos) {
        List<Long> itemIds = orderItemDtos
                .stream()
                .map(OrderItemRequestDto::getItemId)
                .toList();

        List<Item> items = itemRepository.findItemsByIdIn(itemIds);

        log.info("Loaded items with ids {}", itemIds);

        if (items.size() < orderItemDtos.size()) {
            log.warn("Some requested items were not found. Expected: {} items, Found: {}",
                    itemIds.size(), items.size());
            throw new ItemNotFoundException("Some requested items were not found. Expected:" +
                    itemIds.size() + " items, Found: " + items.size());
        }

        order.setOrderItems(new ArrayList<>());
        for (int i = 0; i < items.size(); i++) {
            OrderItem orderItem = orderItemsMapper.toOrderItem(orderItemDtos.get(i));
            orderItem.setItem(items.get(i));
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        log.info("Successfully linked {} items to order", items.size());
    }
}
