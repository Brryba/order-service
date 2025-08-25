package innowise.order_service.dto.order;

import innowise.order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@EqualsAndHashCode
public class OrderUpdateDto {
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;
}