package innowise.order_service.service;

import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.exception.item.DuplicateItemNameException;
import innowise.order_service.exception.item.ItemNotFoundException;
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
        item = itemRepository.save(item);

        log.info("Item {} created", item.getName());

        return itemMapper.toItemResponseDto(item);
    }

    public ItemResponseDto getItemById(Long id) {
        Item item = loadItemFromDatabase(id);

        return itemMapper.toItemResponseDto(item);
    }

    @Transactional
    public ItemResponseDto updateItem(ItemRequestDto itemRequestDto, long id) {
        Item item = loadItemFromDatabase(id);

        if (!item.getName().equals(itemRequestDto.getName())) {
            validateItemNameUnique(itemRequestDto.getName());
        }

        itemMapper.updateItem(item, itemRequestDto);

        log.info("Item {} updated", item.getId());

        return itemMapper.toItemResponseDto(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = loadItemFromDatabase(id);

        itemRepository.delete(item);

        log.info("Item {} deleted", item.getId());
    }

    private Item loadItemFromDatabase(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.warn("Item with id {} not found", id);
            return new ItemNotFoundException("Item with id " + id + " not found");
        });

        log.info("Item {} loaded from database", item.getId());

        return item;
    }

    private void validateItemNameUnique(String itemName) throws DuplicateItemNameException {
        if (itemRepository.existsByName(itemName)) {
            log.warn("Item {} already exists", itemName);
            throw new DuplicateItemNameException("Item " + itemName + " already exists");
        }
    }
}
