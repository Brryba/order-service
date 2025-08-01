package innowise.order_service.dto.order;

import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderCreateDto {
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @Valid
    @NotNull(message = "Order must contain some items")
    @NotEmpty(message = "Order items can't be empty")
    private List<OrderItemRequestDto> orderItems;
}