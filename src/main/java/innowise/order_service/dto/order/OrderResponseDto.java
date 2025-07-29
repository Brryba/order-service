package innowise.order_service.dto.order;

import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.order_items.OrderItemResponseDto;
import innowise.order_service.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Integer id;

    private Long userId;

    private OrderStatus status;

    private LocalDateTime creationDate;

    private List<OrderItemResponseDto> orderItems;
}
