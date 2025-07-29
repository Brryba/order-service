package innowise.order_service.dto.item;

import lombok.Setter;

import java.math.BigDecimal;

@Setter
public class ItemResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
}
