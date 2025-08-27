package innowise.order_service.dto.order;

import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@EqualsAndHashCode
public class OrderUpdateDto {
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    @Valid
    private List<OrderItemRequestDto> orderItems;
}