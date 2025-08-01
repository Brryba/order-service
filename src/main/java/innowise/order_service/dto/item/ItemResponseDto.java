package innowise.order_service.dto.item;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@EqualsAndHashCode
public class ItemResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
}
