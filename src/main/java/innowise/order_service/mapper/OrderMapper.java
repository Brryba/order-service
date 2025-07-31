package innowise.order_service.mapper;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ERROR, uses = {OrderItemsMapper.class})
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toOrder(OrderCreateDto orderUpdateRequestDto);

    @Mapping(target = "user", ignore = true)
    OrderResponseDto toOrderResponseDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateOrder(@MappingTarget Order order, OrderUpdateDto orderUpdateDto);
}
