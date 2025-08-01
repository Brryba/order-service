package innowise.order_service.integration;

import innowise.order_service.configuration.TestcontainersConfiguration;
import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.order_items.OrderItemResponseDto;
import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.service.ItemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public class OrderIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    private static OrderCreateDto orderCreateDto;
    private static OrderUpdateDto orderUpdateDto;
    private static final long ORDER_ID = 1;
    private static final OrderStatus ORDER_STATUS = OrderStatus.NEW;
    private static final long USER_ID = 1;
    private static final String MOCK_TOKEN = "mockToken";

    @BeforeAll
    static void setup() {
        orderCreateDto = OrderCreateDto.builder()
                .status(OrderStatus.NEW)
                .orderItems(List.of(
                        OrderItemRequestDto.builder()
                                .itemId(1L)
                                .quantity(1)
                                .build(),
                        OrderItemRequestDto.builder()
                                .itemId(2L)
                                .quantity(2)
                                .build()))
                .build();

        orderUpdateDto = OrderUpdateDto.builder()
                .status(OrderStatus.PROCESSING)
                .build();
    }

    @Test
    void testCreateNew() {

    }
}
