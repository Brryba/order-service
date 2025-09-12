package innowise.order_service.dto.order_items;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class OrderItemResponseDto {
    private Long itemId;

    private Integer quantity;

    private String name;

    private BigDecimal price;
}
