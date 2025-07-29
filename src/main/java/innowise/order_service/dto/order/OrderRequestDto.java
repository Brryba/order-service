package innowise.order_service.dto.order;

import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderRequestDto {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @NotEmpty(message = "Order items list cannot be empty")
    @Valid
    private List<OrderItemRequestDto> orderItems;
}
