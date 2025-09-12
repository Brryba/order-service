package innowise.order_service.integration;

import innowise.order_service.configuration.TestcontainersConfiguration;
import innowise.order_service.dto.item.ItemPageResponseDto;
import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.entity.Item;
import innowise.order_service.exception.item.DuplicateItemNameException;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.repository.ItemRepository;
import innowise.order_service.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public class ItemIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    private static ItemRequestDto itemRequestDto;

    private static final String ITEM_NAME = "Item Name";
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(10);

    @BeforeEach
    void init() {
        itemRequestDto = ItemRequestDto.builder()
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();
    }

    @Test
    @Transactional
    void createdItemSaved_andCanBeFound() {
        ItemResponseDto createdItem = itemService.createItem(itemRequestDto);

        Optional<Item> foundItem = itemRepository.findById(createdItem.getId());
        assertTrue(foundItem.isPresent());
        assertEquals(createdItem.getName(), foundItem.get().getName());
    }

    @Test
    @Transactional
    void itemPagination_whenItemsPresent_ShouldReturnPage() {
        int PAGE_SIZE = 2;

        itemRequestDto.setName(ITEM_NAME + "0");
        itemService.createItem(itemRequestDto);
        itemRequestDto.setName(ITEM_NAME + "1");
        itemService.createItem(itemRequestDto);
        itemRequestDto.setName(ITEM_NAME);
        itemService.createItem(itemRequestDto);

        ItemPageResponseDto page0 = itemService.getAllItemsOnPage(0, PAGE_SIZE);
        assertAll(() -> {
            assertFalse(page0.getItems().isEmpty());
            assertEquals(0, page0.getPageNumber());
            assertEquals(1, page0.getLastPage());
            assertEquals(PAGE_SIZE, page0.getItems().size());
        });

        ItemPageResponseDto page1 = itemService.getAllItemsOnPage(1, PAGE_SIZE);
        assertAll(() -> {
            assertFalse(page1.getItems().isEmpty());
            assertEquals(1, page1.getPageNumber());
            assertEquals(1, page1.getLastPage());
            assertEquals(1, page1.getItems().size());
        });
    }

    @Test
    @Transactional
    void itemPagination_whenPageNotPresent_ShouldThrowException() {
        int PAGE_SIZE = 2;

        itemRequestDto.setName(ITEM_NAME + "0");
        itemService.createItem(itemRequestDto);
        itemRequestDto.setName(ITEM_NAME + "1");
        itemService.createItem(itemRequestDto);
        itemRequestDto.setName(ITEM_NAME);
        itemService.createItem(itemRequestDto);

        assertThrows(ItemNotFoundException.class, () -> itemService.getAllItemsOnPage(10, 2));
    }

    @Test
    @Transactional
    void updatedItemSaved() {
        ItemResponseDto createdItem = itemService.createItem(itemRequestDto);

        itemRequestDto.setName("Updated Item Name");
        ItemResponseDto updatedItem = itemService.updateItem(itemRequestDto, createdItem.getId());
        Optional<Item> foundItem = itemRepository.findById(createdItem.getId());

        assertEquals("Updated Item Name", updatedItem.getName());
        assertTrue(foundItem.isPresent());
        assertEquals(updatedItem.getName(), foundItem.get().getName());
    }

    @Test
    @Transactional
    void doesNotUpdate_whenNewItemNameIsAlreadyPresent() {
        ItemResponseDto createdItem = itemService.createItem(itemRequestDto);

        itemRequestDto.setName("Updated Item Name");
        itemService.createItem(itemRequestDto);

        assertThrows(DuplicateItemNameException.class, () -> itemService.updateItem(itemRequestDto, createdItem.getId()));
    }

    @Test
    @Transactional
    void deletedItem_isNotLongerInDatabase() {
        ItemResponseDto createdItem = itemService.createItem(itemRequestDto);

        itemService.deleteItem(createdItem.getId());
        Optional<Item> foundItem = itemRepository.findById(createdItem.getId());
        assertTrue(foundItem.isEmpty());
    }
}
