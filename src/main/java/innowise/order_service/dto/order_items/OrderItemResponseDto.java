package innowise.order_service.dto.order_items;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemResponseDto {
    private Long id;

    private Long itemId;

    private Integer quantity;

    private String name;
}
