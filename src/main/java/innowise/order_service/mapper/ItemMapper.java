package innowise.order_service.mapper;

import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = SPRING, unmappedTargetPolicy = ERROR)
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    Item toItem(ItemRequestDto itemRequestDto);

    ItemResponseDto toItemResponseDto(Item item);

    @Mapping(target = "id", ignore = true)
    void updateItem(@MappingTarget Item item, ItemRequestDto itemRequestDto);
}