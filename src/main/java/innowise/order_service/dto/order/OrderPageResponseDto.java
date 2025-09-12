package innowise.order_service.dto.order;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OrderPageResponseDto {
    private int currentPage;
    private int pageSize;
    private int maxPage;
    private List<OrderResponseDto> orders;
}
