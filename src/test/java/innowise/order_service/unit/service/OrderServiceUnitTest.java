package innowise.order_service.unit.service;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderUpdateDto;
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
import innowise.order_service.mapper.OrderItemsMapperImpl;
import innowise.order_service.mapper.OrderMapperImpl;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.repository.OrderRepository;
import innowise.order_service.service.OrderService;
import innowise.order_service.service.UserServiceClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {OrderMapperImpl.class, OrderItemsMapperImpl.class, OrderService.class})
class OrderServiceUnitTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Autowired
    private OrderService orderService;

    private static OrderCreateDto orderCreateDto;
    private static OrderUpdateDto orderUpdateDto;
    private static OrderResponseDto orderResponseDto;
    private static Order order;
    private static Item item1;
    private static Item item2;
    private static UserResponseDto userResponseDto;
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long ITEM_ID_1 = 10L;
    private static final Long ITEM_ID_2 = 20L;
    private static final String TOKEN = "Bearer test-token";
    private static final String USER_NAME = "Test User";

    @BeforeAll
    static void setUpAll() {
        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.NEW)
                .creationDate(LocalDateTime.now())
                .build();

        List<OrderItemRequestDto> orderItemDtos = Arrays.asList(
                OrderItemRequestDto.builder()
                        .itemId(ITEM_ID_1)
                        .quantity(2)
                        .build(),
                OrderItemRequestDto.builder()
                        .itemId(ITEM_ID_2)
                        .quantity(1)
                        .build()
        );

        orderCreateDto = OrderCreateDto.builder()
                .status(OrderStatus.NEW)
                .orderItems(orderItemDtos)
                .build();

        orderUpdateDto = OrderUpdateDto.builder()
                .status(OrderStatus.PROCESSING)
                .build();

        item1 = Item.builder()
                .id(ITEM_ID_1)
                .name("Item 1")
                .price(BigDecimal.valueOf(10.00))
                .build();

        item2 = Item.builder()
                .id(ITEM_ID_2)
                .name("Item 2")
                .price(BigDecimal.valueOf(15.00))
                .build();

        List<OrderItem> orderItems = Arrays.asList(
                OrderItem.builder()
                        .id(1)
                        .quantity(2)
                        .order(order)
                        .item(item1)
                        .build(),
                OrderItem.builder()
                        .id(2)
                        .quantity(2)
                        .order(order)
                        .item(item2)
                        .build()
        );

        order.setOrderItems(orderItems);

        userResponseDto = UserResponseDto.builder()
                .id(USER_ID)
                .name(USER_NAME)
                .build();
    }

    @Test
    void addOrder_WhenValidRequest_ShouldCreateAndReturnOrder() {
        when(itemRepository.findItemsByIdIn(Arrays.asList(ITEM_ID_1, ITEM_ID_2)))
                .thenReturn(Arrays.asList(item1, item2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userServiceClient.getUserById(USER_ID, TOKEN)).thenReturn(userResponseDto);

        OrderResponseDto result = orderService.addOrder(orderCreateDto, TOKEN, USER_ID);

        assertEquals(orderCreateDto.getStatus(), result.getStatus());
        assertEquals(orderCreateDto.getOrderItems().size(), result.getOrderItems().size());
        assertEquals(userResponseDto, result.getUser());
        verify(itemRepository).findItemsByIdIn(Arrays.asList(ITEM_ID_1, ITEM_ID_2));
        verify(orderRepository).save(any(Order.class));
        verify(userServiceClient).getUserById(USER_ID, TOKEN);
    }

    @Test
    void addOrder_WhenItemsNotFound_ShouldThrowItemNotFoundException() {
        when(itemRepository.findItemsByIdIn(Arrays.asList(ITEM_ID_1, ITEM_ID_2)))
                .thenReturn(Collections.singletonList(item1));

        assertThatThrownBy(() -> orderService.addOrder(orderCreateDto, TOKEN, USER_ID))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Some items in order were not found");

        verify(itemRepository).findItemsByIdIn(Arrays.asList(ITEM_ID_1, ITEM_ID_2));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserById(USER_ID, TOKEN)).thenReturn(userResponseDto);

        OrderResponseDto result = orderService.getOrderById(ORDER_ID, TOKEN, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORDER_ID);
        assertThat(result.getOrderItems().getFirst().getName())
                .isEqualTo(order.getOrderItems().getFirst().getItem().getName());
        verify(orderRepository).findById(ORDER_ID);
        verify(userServiceClient).getUserById(USER_ID, TOKEN);
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, TOKEN, USER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order with id " + ORDER_ID + " was not found");

        verify(orderRepository).findById(ORDER_ID);
        verify(userServiceClient, never()).getUserById(any(), any());
    }

    @Test
    void getOrderById_WhenUserNotOwner_ShouldThrowOrderAccessDeniedException() {
        long differentUserId = 999L;
        Order orderWithDifferentUser = Order.builder()
                .id(ORDER_ID)
                .userId(differentUserId)
                .status(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderWithDifferentUser));

        assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, TOKEN, USER_ID))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessage("You are only allowed to access your own orders");

        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    void getOrdersByIds_WhenOrdersExist_ShouldReturnOrders() {
        List<Long> orderIds = Arrays.asList(ORDER_ID, 2L);
        Order order2 = Order.builder()
                .id(2L)
                .userId(USER_ID)
                .status(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findOrdersByIdIn(orderIds)).thenReturn(Arrays.asList(order, order2));
        when(userServiceClient.getUserById(USER_ID, TOKEN)).thenReturn(userResponseDto);

        List<OrderResponseDto> result = orderService.getOrdersByIds(orderIds, TOKEN, USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(ORDER_ID);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(orderRepository).findOrdersByIdIn(orderIds);
        verify(userServiceClient).getUserById(USER_ID, TOKEN);
    }

    @Test
    void getOrdersByIds_WhenOrdersNotFound_ShouldThrowOrderNotFoundException() {
        List<Long> orderIds = Arrays.asList(ORDER_ID, 2L);
        when(orderRepository.findOrdersByIdIn(orderIds)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.getOrdersByIds(orderIds, TOKEN, USER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("None of the orders with ids");

        verify(orderRepository).findOrdersByIdIn(orderIds);
    }

    @Test
    void getOrdersByStatus_WhenValidStatus_ShouldReturnOrders() {
        String status = "NEW";
        when(orderRepository.findOrdersByStatusAndUserId(OrderStatus.NEW, USER_ID))
                .thenReturn(Collections.singletonList(order));
        when(userServiceClient.getUserById(USER_ID, TOKEN)).thenReturn(userResponseDto);

        List<OrderResponseDto> result = orderService.getOrdersByStatus(status, TOKEN, USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(ORDER_ID);
        assertThat(result.getFirst().getStatus()).isEqualTo(OrderStatus.NEW);
        verify(orderRepository).findOrdersByStatusAndUserId(OrderStatus.NEW, USER_ID);
        verify(userServiceClient).getUserById(USER_ID, TOKEN);
    }

    @Test
    void getOrdersByStatus_WhenInvalidStatus_ShouldThrowIllegalOrderStatusException() {
        String invalidStatus = "INVALID_STATUS";

        assertThatThrownBy(() -> orderService.getOrdersByStatus(invalidStatus, TOKEN, USER_ID))
                .isInstanceOf(IllegalOrderStatusException.class)
                .hasMessageContaining("Illegal order status INVALID_STATUS");

        verify(orderRepository, never()).findOrdersByStatusAndUserId(any(OrderStatus.class), any(Long.class));
    }

    @Test
    void getOrdersByStatus_WhenNoOrdersFound_ShouldThrowOrderNotFoundException() {
        String status = "PROCESSING";
        when(orderRepository.findOrdersByStatusAndUserId(OrderStatus.PROCESSING, USER_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> orderService.getOrdersByStatus(status, TOKEN, USER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("None of the orders with status PROCESSING were found");

        verify(orderRepository).findOrdersByStatusAndUserId(OrderStatus.PROCESSING, USER_ID);
    }

    @Test
    void updateOrder_WhenValidRequest_ShouldUpdateAndReturnOrder() {
        Order updatedOrder = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.DELIVERED)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(userServiceClient.getUserById(USER_ID, TOKEN)).thenReturn(userResponseDto);

        OrderResponseDto result = orderService.updateOrder(ORDER_ID, orderUpdateDto, TOKEN, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORDER_ID);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository).save(any(Order.class));
        verify(userServiceClient).getUserById(USER_ID, TOKEN);
    }

    @Test
    void updateOrder_WhenOrderNotFound_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(ORDER_ID, orderUpdateDto, TOKEN, USER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order with id " + ORDER_ID + " was not found");

        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrder_WhenUserNotOwner_ShouldThrowOrderAccessDeniedException() {
        long differentUserId = 999L;
        Order orderWithDifferentUser = Order.builder()
                .id(ORDER_ID)
                .userId(differentUserId)
                .status(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderWithDifferentUser));

        assertThatThrownBy(() -> orderService.updateOrder(ORDER_ID, orderUpdateDto, TOKEN, USER_ID))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessage("You are only allowed to access your own orders");

        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteOrder() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.deleteOrder(ORDER_ID, USER_ID);

        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_WhenOrderNotFound_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrder(ORDER_ID, USER_ID))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order with id " + ORDER_ID + " was not found");

        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void deleteOrder_WhenUserNotOwner_ShouldThrowOrderAccessDeniedException() {
        long differentUserId = 999L;
        Order orderWithDifferentUser = Order.builder()
                .id(ORDER_ID)
                .userId(differentUserId)
                .status(OrderStatus.PROCESSING)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderWithDifferentUser));

        assertThatThrownBy(() -> orderService.deleteOrder(ORDER_ID, USER_ID))
                .isInstanceOf(OrderAccessDeniedException.class)
                .hasMessage("You are only allowed to access your own orders");

        verify(orderRepository).findById(ORDER_ID);
        verify(orderRepository, never()).delete(any());
    }
}