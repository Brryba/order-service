package innowise.order_service.dto.item;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ItemPageResponseDto {
    private int pageNumber;
    private int lastPage;
    private List<ItemResponseDto> items;
}
