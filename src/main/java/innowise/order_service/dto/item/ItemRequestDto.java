package innowise.order_service.dto.item;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class ItemRequestDto {
    @NotBlank(message = "Item name can not be empty")
    @Size(max = 100, message = "Item name can not be longer than 100 symbols")
    private String name;
    @NotNull(message = "Item price can not be empty")
    @Positive(message = "Price must be positive value")
    @Digits(integer = 10, fraction = 2, message = "Must be ≤ 10 whole digits and exactly 2 decimal places")
    private BigDecimal price;
}