package innowise.order_service.unit.service;

import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.exception.item.DuplicateItemNameException;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.mapper.ItemMapperImpl;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.service.ItemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ItemMapperImpl.class, ItemService.class})
class ItemServiceUnitTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    private static ItemRequestDto itemRequestDto;
    private static ItemResponseDto itemResponseDto;
    private static Item item;
    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Test Item";
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(99.99);

    @BeforeAll
    static void setUpAll() {
        itemRequestDto = ItemRequestDto.builder()
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        item = Item.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();
    }

    @Test
    void createItem_WhenValidRequest_ShouldCreateAndReturnItem() {
        when(itemRepository.existsByName(ITEM_NAME)).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.createItem(itemRequestDto);

        assertThat(result).isEqualTo(itemResponseDto);
        verify(itemRepository).existsByName(ITEM_NAME);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_WhenDuplicateName_ShouldThrowDuplicateItemNameException() {
        when(itemRepository.existsByName(ITEM_NAME)).thenReturn(true);

        assertThatThrownBy(() -> itemService.createItem(itemRequestDto))
                .isInstanceOf(DuplicateItemNameException.class)
                .hasMessage("Item " + ITEM_NAME + " already exists");

        verify(itemRepository).existsByName(ITEM_NAME);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        ItemResponseDto result = itemService.getItemById(ITEM_ID);

        assertThat(result).isEqualTo(itemResponseDto);
        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void getItemById_WhenItemNotFound_ShouldThrowItemNotFoundException() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(ITEM_ID))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Item with id " + ITEM_ID + " not found");

        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void updateItem_WhenValidRequestSameName_ShouldUpdateItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        ItemResponseDto result = itemService.updateItem(itemRequestDto, ITEM_ID);

        assertThat(result).isEqualTo(itemResponseDto);
        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository, never()).existsByName(any());
    }

    @Test
    void updateItem_WhenValidRequestDifferentName_ShouldUpdateItem() {
        String newItemName = "Updated Item Name";
        ItemRequestDto updateRequest = ItemRequestDto.builder()
                .name(newItemName)
                .price(ITEM_PRICE)
                .build();

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.existsByName(newItemName)).thenReturn(false);

        ItemResponseDto result = itemService.updateItem(updateRequest, ITEM_ID);

        assertThat(result).
                usingRecursiveComparison().
                ignoringFields("name")
                .isEqualTo(itemResponseDto);
        assertEquals(newItemName, result.getName());
        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository).existsByName(newItemName);
    }

    @Test
    void updateItem_WhenItemNotFound_ShouldThrowItemNotFoundException() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(itemRequestDto, ITEM_ID))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Item with id " + ITEM_ID + " not found");

        verify(itemRepository).findById(ITEM_ID);
    }

    @Test
    void updateItem_WhenDuplicateNameForDifferentItem_ShouldThrowDuplicateItemNameException() {
        String duplicateName = "Duplicate Item";
        ItemRequestDto updateRequest = ItemRequestDto.builder()
                .name(duplicateName)
                .price(ITEM_PRICE)
                .build();

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.existsByName(duplicateName)).thenReturn(true);

        assertThatThrownBy(() -> itemService.updateItem(updateRequest, ITEM_ID))
                .isInstanceOf(DuplicateItemNameException.class)
                .hasMessage("Item " + duplicateName + " already exists");

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository).existsByName(duplicateName);
    }

    @Test
    void deleteItem_WhenItemExists_ShouldDeleteItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        itemService.deleteItem(ITEM_ID);

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_WhenItemNotFound_ShouldThrowItemNotFoundException() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(ITEM_ID))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Item with id " + ITEM_ID + " not found");

        verify(itemRepository).findById(ITEM_ID);
        verify(itemRepository, never()).delete(any());
    }
}