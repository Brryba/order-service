package innowise.order_service.controller;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.dto.order.OrderResponseDto;
import innowise.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDto addOrder(@Valid @RequestBody OrderCreateDto orderRequestDto,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @AuthenticationPrincipal Long userId) {
        return orderService.addOrder(orderRequestDto, token, userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDto getOrder(@PathVariable long id,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                     @AuthenticationPrincipal Long userId) {
        return orderService.getOrderById(id, token, userId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByIds(@RequestParam List<Long> ids,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                 @AuthenticationPrincipal Long userId) {
        return orderService.getOrdersByIds(ids, token, userId);
    }

    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderResponseDto> getOrdersByStatus(@PathVariable String status,
                                                    @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                                    @AuthenticationPrincipal Long userId) {
        return orderService.getOrdersByStatus(status, token, userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponseDto updateOrder(@PathVariable long id,
                                        @RequestBody @Valid OrderUpdateDto orderUpdateDto,
                                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                        @AuthenticationPrincipal Long userId) {
        return orderService.updateOrder(id, orderUpdateDto, token, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable long id,
                            @AuthenticationPrincipal Long userId) {
        orderService.deleteOrder(id, userId);
    }
}