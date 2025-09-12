package innowise.order_service.dto.order_items;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class OrderItemPatchDto {
    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    @Positive(message = "Quantity must be a positive number")
    private Integer quantity;

    private boolean isDeleted;
}
