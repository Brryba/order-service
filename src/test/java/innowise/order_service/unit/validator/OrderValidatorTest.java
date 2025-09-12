package innowise.order_service.unit.validator;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderItem;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.order.IllegalNewOrderException;
import innowise.order_service.exception.order.IllegalStatusChangeException;
import innowise.order_service.exception.security.OrderAccessDeniedException;
import innowise.order_service.repository.OrderRepository;
import innowise.order_service.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderValidatorTest {
    private static OrderCreateDto orderCreateDto;
    private static Order order;

    private final Long USER_ID = 100L;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderValidator orderValidator;

    @BeforeEach
    void setUp() {
        long ORDER_ID = 100L;
        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.CREATING)
                .creationDate(LocalDateTime.now())
                .build();

        order = Order.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .status(OrderStatus.CREATING)
                .creationDate(LocalDateTime.now())
                .build();

        long ITEM_ID_1 = 1L;
        Item item1 = Item.builder()
                .id(ITEM_ID_1)
                .name("Item 1")
                .price(BigDecimal.valueOf(10.00))
                .build();

        long ITEM_ID_2 = 2L;
        Item item2 = Item.builder()
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
                .status(OrderStatus.CREATING)
                .orderItems(orderItemDtos)
                .build();
    }

    @Test
    void validateCreateOrderRequest_success() {
        assertDoesNotThrow(() -> orderValidator.validateCreateOrderRequest(orderCreateDto, USER_ID));
    }

    @Test
    void validateCreateOrderRequest_wrongOrderStatus() {
        orderCreateDto.setStatus(OrderStatus.PROCESSING);
        assertThrows(IllegalNewOrderException.class, () ->
                orderValidator.validateCreateOrderRequest(orderCreateDto, USER_ID));
    }

    @Test
    void validateDeleteOrderRequest_onlyOneCreatingItemPerUser() {
        when(orderRepository.existsOrderByStatusAndUserId(OrderStatus.CREATING, 1L)).thenReturn(true);
        when(orderRepository.existsOrderByStatusAndUserId(OrderStatus.CREATING, 2L)).thenReturn(false);

        assertThrows(IllegalNewOrderException.class, () ->
                orderValidator.validateCreateOrderRequest(orderCreateDto, 1L)
        );
        assertDoesNotThrow(() -> orderValidator.validateCreateOrderRequest(orderCreateDto, 2L));
    }

    @Test
    void validateOrderOwner_success() {
        assertDoesNotThrow(() -> orderValidator.validateOrderOwner(order, USER_ID));
    }

    @Test
    void validateOrderOwner_wrongUser() {
        assertThrows(OrderAccessDeniedException.class,
                () -> orderValidator.validateOrderOwner(order, 99999L));
    }

    @Test
    void validateOrderCanBeChanged_success() {
        order.setStatus(OrderStatus.CREATING);
        assertDoesNotThrow(() -> orderValidator.validateOrderCanBeChanged(order));
    }

    @Test
    void validateOrderCanBeChanged_forbiddenStatus() {
        order.setStatus(OrderStatus.NEW);
        assertThrows(IllegalStatusChangeException.class,
                () -> orderValidator.validateOrderCanBeChanged(order));
    }

    @Test
    void validateOrderStatusChange_allowedTransition() {
        order.setStatus(OrderStatus.CREATING);
        OrderUpdateDto dto = OrderUpdateDto.builder().status(OrderStatus.NEW).build();

        assertDoesNotThrow(() -> orderValidator.validateOrderStatusChange(order, dto));
    }

    @Test
    void validateOrderStatusChange_forbiddenTransition() {
        order.setStatus(OrderStatus.NEW);
        OrderUpdateDto dto = OrderUpdateDto.builder().status(OrderStatus.PROCESSING).build();

        assertThrows(IllegalStatusChangeException.class,
                () -> orderValidator.validateOrderStatusChange(order, dto));
    }
}
