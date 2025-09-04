package innowise.order_service.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import innowise.order_service.controller.ItemController;
import innowise.order_service.dto.item.ItemRequestDto;
import innowise.order_service.dto.item.ItemResponseDto;
import innowise.order_service.exception.item.ItemNotFoundException;
import innowise.order_service.service.ItemService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ItemController.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static ItemRequestDto itemRequestDto;
    private static ItemResponseDto itemResponseDto;
    private static final long ITEM_ID = 1;
    private static final String ITEM_NAME = "Item Name";
    private static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(10);

    @BeforeAll
    static void setup() {
        itemRequestDto = ItemRequestDto.builder()
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();
    }

    @Test
    void testCreateItem_whenValidRequest_201() throws Exception {
        when(itemService.createItem(itemRequestDto)).thenReturn(itemResponseDto);

        mockMvc.perform(post("/api/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.name").value(ITEM_NAME))
                .andExpect(jsonPath("$.price").value(ITEM_PRICE));
    }

    @Test
    void testCreateItem_whenInvalidRequestParams_shouldReturn_400() throws Exception {
        mockMvc.perform(post("/api/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("no params"))).
                andExpect(status().isBadRequest());

        verify(itemService, never()).createItem(itemRequestDto);
    }

    @Test
    void testGetItem_whenExists_shouldReturn200() throws Exception {
        when(itemService.getItemById(ITEM_ID)).thenReturn(itemResponseDto);

        mockMvc.perform(get("/api/item/{id}", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.name").value(ITEM_NAME))
                .andExpect(jsonPath("$.price").value(ITEM_PRICE.doubleValue()));
    }

    @Test
    void testGetItem_whenNotExists_shouldReturn404() throws Exception {
        when(itemService.getItemById(ITEM_ID)).thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(get("/api/item/{id}", ITEM_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateItem_whenValid_shouldReturn200() throws Exception {
        when(itemService.updateItem(itemRequestDto, ITEM_ID)).thenReturn(itemResponseDto);

        mockMvc.perform(put("/api/item/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.name").value(ITEM_NAME))
                .andExpect(jsonPath("$.price").value(ITEM_PRICE.doubleValue()));
    }

    @Test
    void testUpdateItem_whenInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(put("/api/item/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("invalid body")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteItem_whenExists_shouldReturn204() throws Exception {
        doNothing().when(itemService).deleteItem(ITEM_ID);

        mockMvc.perform(delete("/api/item/{id}", ITEM_ID))
                .andExpect(status().isNoContent());

        verify(itemService).deleteItem(ITEM_ID);
    }

    @Test
    void testDeleteItem_whenNotExists_shouldReturn404() throws Exception {
        doThrow(new ItemNotFoundException("Item not found"))
                .when(itemService).deleteItem(ITEM_ID);

        mockMvc.perform(delete("/api/item/{id}", ITEM_ID))
                .andExpect(status().isNotFound());
    }
}
