package innowise.order_service.mapper;

import innowise.order_service.dto.order.OrderRequestDto;
import innowise.order_service.dto.order_items.OrderItemRequestDto;
import innowise.order_service.dto.order_items.OrderItemResponseDto;
import innowise.order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ERROR)
public interface OrderItemsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    OrderItem toOrderItem(OrderItemRequestDto orderItemRequestDto);

    @Mapping(target = "itemId", source = "item.id")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);
}