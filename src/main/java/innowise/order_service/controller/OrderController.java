package innowise.order_service.controller;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    private final String USER_ID_TOKEN = "X-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto addOrder(@Valid @RequestBody OrderCreateDto orderRequestDto,
                                     @RequestHeader("X-User-Id") Long userId) {
        return orderService.addOrder(orderRequestDto, userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDto getOrder(@PathVariable long id,
                                     @RequestHeader("X-User-Id") Long userId) {
        return orderService.getOrderById(id, userId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByIds(@RequestParam List<Long> ids,
                                                 @RequestHeader("X-User-Id") Long userId) {
        return orderService.getOrdersByIds(ids, userId);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByStatus(@PathVariable String status,
                                                    @RequestHeader("X-User-Id") Long userId) {
        return orderService.getOrdersByStatus(status, userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDto updateOrder(@PathVariable long id,
                                        @RequestBody @Valid OrderUpdateDto orderUpdateDto,
                                        @RequestHeader("X-User-Id") Long userId) {
        return orderService.updateOrder(id, orderUpdateDto, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable long id,
                            @RequestHeader("X-User-Id") Long userId) {
        orderService.deleteOrder(id, userId);
    }
}