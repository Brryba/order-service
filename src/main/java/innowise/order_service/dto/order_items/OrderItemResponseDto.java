package innowise.order_service.dto.order_items;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDto {
    private Long id;

    private Long itemId;

    private Integer quantity;
}
