package innowise.order_service.controller;

import innowise.order_service.dto.order.OrderRequestDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto addOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return orderService.addOrder(orderRequestDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDto getOrder(@PathVariable long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByIds(@RequestParam List<Long> ids) {
        return orderService.getOrdersByIds(ids);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByStatus(@PathVariable OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }
}