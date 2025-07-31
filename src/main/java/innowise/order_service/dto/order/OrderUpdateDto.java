package innowise.order_service.dto.order;

import innowise.order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderUpdateDto {
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;
}