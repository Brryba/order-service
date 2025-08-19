package innowise.order_service.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innowise.order_service.controller.ExceptionController;
import innowise.order_service.controller.OrderController;
import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.order_items.OrderItemResponseDto;
import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.exception.order.OrderNotFoundException;
import innowise.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        OrderController.class,
        ExceptionController.class
})
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private static OrderCreateDto orderCreateDto;
    private static OrderUpdateDto orderUpdateDto;
    private static OrderResponseDto orderResponseDto;
    private static UserResponseDto userResponseDto;
    private static final long ORDER_ID = 1;
    private static final OrderStatus ORDER_STATUS = OrderStatus.NEW;
    private static final long USER_ID = 1;
    private static final String USER_ID_HEADER = "X-User-Id";

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

        userResponseDto = UserResponseDto.builder()
                .id(USER_ID)
                .name("User name")
                .birthDate(LocalDate.now().minusYears(20))
                .email("example@example.com")
                .build();

        orderResponseDto = OrderResponseDto.builder()
                .id(ORDER_ID)
                .status(ORDER_STATUS)
                .user(userResponseDto)
                .orderItems(List.of(OrderItemResponseDto.builder()
                                .itemId(1L)
                                .name("Item 1")
                                .quantity(1)
                                .build(),
                        OrderItemResponseDto.builder()
                                .itemId(2L)
                                .name("Item 2")
                                .quantity(2)
                                .build()))
                .build();
    }

    @Test
    void testCreateItem_whenValidRequest_201() throws Exception {
        when(orderService.addOrder(any(OrderCreateDto.class), eq(USER_ID)))
                .thenReturn(orderResponseDto);

        mockMvc.perform(post("/api/order")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId()))
                .andExpect(jsonPath("$.user.name").value(userResponseDto.getName()))
                .andExpect(jsonPath("$.status").value(orderResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$.orderItems[0].id").value(orderResponseDto.getOrderItems().getFirst().getId()));
    }

    @Test
    void testCreateItem_whenInvalidRequestParams_shouldReturn_400() throws Exception {
        mockMvc.perform(post("/api/order")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("no params"))).
                andExpect(status().isBadRequest());

        verify(orderService, never()).addOrder(any(OrderCreateDto.class), eq(USER_ID));
    }

    @Test
    void testGetItem_whenExists_shouldReturn200() throws Exception {
        when(orderService.getOrderById(ORDER_ID, USER_ID)).thenReturn(orderResponseDto);

        mockMvc.perform(get("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId()))
                .andExpect(jsonPath("$.user.name").value(userResponseDto.getName()))
                .andExpect(jsonPath("$.status").value(orderResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$.orderItems[0].id").value(orderResponseDto.getOrderItems().getFirst().getId()));
    }

    @Test
    void testGetItem_whenNotExists_shouldReturn404() throws Exception {
        when(orderService.getOrderById(ORDER_ID, USER_ID))
                .thenThrow(new OrderNotFoundException("Item not found"));

        mockMvc.perform(get("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetItemsByIds_whenExists_shouldReturn200() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        when(orderService.getOrdersByIds(ids, USER_ID))
                .thenReturn(List.of(orderResponseDto));

        mockMvc.perform(get("/api/order?ids=1,2,3")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(orderResponseDto.getId()));
    }

    @Test
    void testGetItemsByStatus_whenExists_shouldReturn200() throws Exception {
        when(orderService.getOrdersByStatus("NEW", USER_ID))
                .thenReturn(List.of(orderResponseDto));

        mockMvc.perform(get("/api/order/status/{status}", OrderStatus.NEW.toString())
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(orderResponseDto.getId()));
    }

    @Test
    void testUpdateItem_whenValid_shouldReturn200() throws Exception {
        when(orderService.updateOrder(ORDER_ID, orderUpdateDto, USER_ID))
                .thenReturn(orderResponseDto);

        mockMvc.perform(put("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderResponseDto.getId()))
                .andExpect(jsonPath("$.user.name").value(userResponseDto.getName()))
                .andExpect(jsonPath("$.status").value(orderResponseDto.getStatus().toString()))
                .andExpect(jsonPath("$.orderItems[0].id").value(orderResponseDto.getOrderItems().getFirst().getId()));
    }

    @Test
    void testUpdateItem_whenInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("invalid body")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteItem_whenExists_shouldReturn204() throws Exception {
        doNothing().when(orderService).deleteOrder(ORDER_ID, USER_ID);

        mockMvc.perform(delete("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(ORDER_ID, USER_ID);
    }

    @Test
    void testDeleteItem_whenNotExists_shouldReturn404() throws Exception {
        doThrow(new ItemNotFoundException("Item not found"))
                .when(orderService).deleteOrder(ORDER_ID, USER_ID);

        mockMvc.perform(delete("/api/order/{id}", ORDER_ID)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testAllEndpoints_whenUserIdHeader_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/order/{id}", ORDER_ID))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/order/status/new", OrderStatus.PROCESSING))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/order?ids=1,2"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/order/{id}", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderUpdateDto)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/order/{id}", ORDER_ID))
                .andExpect(status().isBadRequest());
    }
}
