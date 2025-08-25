package innowise.order_service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import innowise.order_service.configuration.TestcontainersConfiguration;
import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.exception.order.OrderNotFoundException;
import innowise.order_service.exception.security.OrderAccessDeniedException;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.repository.OrderRepository;
import innowise.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
@AutoConfigureWireMock(port = 8081)
public class OrderIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @Autowired
    private ObjectMapper objectMapper;

    private static OrderCreateDto orderCreateDto;
    private static OrderUpdateDto orderUpdateDto;
    private static UserResponseDto userResponseDto;
    private static Item item;
    private static final long USER_ID = 1;

    @BeforeAll
    static void setup() {
        orderCreateDto = OrderCreateDto.builder()
                .status(OrderStatus.NEW)
                .orderItems(List.of(
                        OrderItemRequestDto.builder()
                                .itemId(1L)
                                .quantity(1)
                                .build()))
                .build();

        orderUpdateDto = OrderUpdateDto.builder()
                .status(OrderStatus.PROCESSING)
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(USER_ID)
                .name("User name")
                .surname("User surname")
                .birthDate(LocalDate.now().minusYears(20))
                .email("example@example.com")
                .build();
    }

    @BeforeEach
    void setUp() {
        item = Item.builder()
                .price(BigDecimal.ONE)
                .name("Item name")
                .build();
    }

    @BeforeEach
    public void before() throws JsonProcessingException {
        String jsonUserResponse = objectMapper.writeValueAsString(userResponseDto);
        stubFor(WireMock.get("/api/user/me").willReturn(ok()
                .withHeader("Content-Type", "application/json")
                .withBody(jsonUserResponse)
        ));
    }

    private void saveItem() {
        long itemId = itemRepository.save(item).getId();
        orderCreateDto.getOrderItems().forEach(orderItem -> orderItem.setItemId(itemId));
    }

    @Test
    @Transactional
    void testCreateNew_savedInDatabase_withOrderItems() {
        saveItem();

        OrderResponseDto createdOrder = orderService.addOrder(orderCreateDto, USER_ID);
        assertEquals(USER_ID, createdOrder.getUser().getId());

        Optional<Order> foundOrder = orderRepository.findById(createdOrder.getId());
        assertTrue(foundOrder.isPresent());
        assertEquals(createdOrder.getId(), foundOrder.get().getId());
        assertEquals(orderCreateDto.getOrderItems().size(), foundOrder.get().getOrderItems().size());
        assertEquals(orderCreateDto.getOrderItems().getFirst().getItemId(),
                foundOrder.get().getOrderItems().getFirst().getItem().getId());
        assertEquals(orderCreateDto.getOrderItems().getFirst().getQuantity(),
                foundOrder.get().getOrderItems().getFirst().getQuantity());
        assertNotNull(createdOrder.getUser().getEmail());
    }


    @Test
    @Transactional
    void testCreateNewOrder_noItemsFoundInDatabase() {
        orderCreateDto.getOrderItems().forEach(orderItem -> orderItem.setItemId(99999L));

        assertThrows(ItemNotFoundException.class, () ->
                orderService.addOrder(orderCreateDto, USER_ID));
    }

    @Test
    @Transactional
    void testGetOrdersByStatus_afterSave() {
        saveItem();
        orderService.addOrder(orderCreateDto, USER_ID);
        orderService.addOrder(orderCreateDto, USER_ID);

        List<OrderResponseDto> newOrders =
                orderService.getOrdersByStatus(orderCreateDto.getStatus().name(), USER_ID);
        assertEquals(2, newOrders.size());

        assertThrows(OrderNotFoundException.class, () ->
                orderService.getOrdersByStatus(OrderStatus.PAYMENT_FAILED.name(), USER_ID));
    }

    @Test
    @Transactional
    void testUpdateOrderStatus_afterSave() {
        saveItem();

        long orderId = orderService.addOrder(orderCreateDto, USER_ID).getId();
        orderService.updateOrder(orderId, orderUpdateDto, USER_ID);

        Optional<Order> foundOrder = orderRepository.findById(orderId);
        assertTrue(foundOrder.isPresent());
        assertEquals(orderUpdateDto.getStatus(), foundOrder.get().getStatus());
    }

    @Test
    @Transactional
    void testUpdateOrder_failsWhenUserIsNotOrderOwner() {
        saveItem();

        long orderId = orderService.addOrder(orderCreateDto,  USER_ID).getId();

        assertThrows(OrderAccessDeniedException.class, () ->
                orderService.updateOrder(orderId, orderUpdateDto, 99999L));
    }

    @Test
    @Transactional
    void deleteOrder_notFoundInDatabase() {
        saveItem();

        long orderId = orderService.addOrder(orderCreateDto, USER_ID).getId();
        orderService.deleteOrder(orderId, USER_ID);

        Optional<Order> foundOrder = orderRepository.findById(orderId);
        assertTrue(foundOrder.isEmpty());
    }
}
