package innowise.order_service.service;

import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.exception.item.DuplicateItemNameException;
import innowise.order_service.mapper.ItemMapper;
import innowise.order_service.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        validateItemNameUnique(itemRequestDto.getName());

        Item item = itemMapper.toItem(itemRequestDto);
        itemRepository.save(item);

        log.info("Item {} created", item.getName());

        return itemMapper.toItemResponseDto(item);
    }

    void validateItemNameUnique(String itemName) throws DuplicateItemNameException {
        if (itemRepository.existsByName(itemName)) {
            log.warn("Item {} already exists", itemName);
            throw new DuplicateItemNameException("Item " + itemName + " already exists");
        }
    }
}
