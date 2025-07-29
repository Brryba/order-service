package innowise.order_service.dto.order_items;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;
}
