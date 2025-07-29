package innowise.order_service.mapper;

import innowise.order_service.dto.order.OrderRequestDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ERROR, uses = {OrderItemsMapper.class})
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    Order toOrder(OrderRequestDto orderRequestDto);

    OrderResponseDto toOrderResponseDto(Order order);
}
