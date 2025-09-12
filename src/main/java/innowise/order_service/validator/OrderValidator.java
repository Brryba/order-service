package innowise.order_service.validator;

import innowise.order_service.dto.order.OrderCreateDto;
import innowise.order_service.dto.order.OrderUpdateDto;
import innowise.order_service.entity.Order;
import innowise.order_service.entity.OrderStatus;
import innowise.order_service.exception.order.IllegalNewOrderException;
import innowise.order_service.exception.order.IllegalStatusChangeException;
import innowise.order_service.exception.security.OrderAccessDeniedException;
import innowise.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidator {
    private final OrderRepository orderRepository;

    public void validateCreateOrderRequest(OrderCreateDto orderCreateDto, Long userId) {
        if (orderCreateDto.getStatus() != OrderStatus.CREATING &&
                orderCreateDto.getStatus() != OrderStatus.NEW) {
            log.warn("Forbidden creating order with status {}", orderCreateDto.getStatus());
            throw new IllegalNewOrderException("You are only allowed to create orders with CREATING or NEW status");
        }

        if (orderCreateDto.getStatus() == OrderStatus.CREATING &&
                orderRepository.existsOrderByStatusAndUserId(OrderStatus.CREATING, userId)) {
            log.warn("Forbidden creating multiple CREATING orders for user {}", userId);
            throw new IllegalNewOrderException("You are only allowed to have one CREATING order");
        }
    }

    public void validateOrderOwner(Order order, Long userId) throws OrderAccessDeniedException {
        if (order.getUserId() != userId) {
            log.warn("User {} can not see the order {}", userId, order.getId());
            throw new OrderAccessDeniedException("You are only allowed to access your own orders");
        }
    }

    public void validateOrderCanBeChanged(Order order) {
        if (order.getStatus() != OrderStatus.CREATING) {
            log.warn("Forbidden order items change for {} status", order.getStatus());
            throw new IllegalStatusChangeException("You are only allowed to change items of CREATING order");
        }
    }

    public void validateOrderStatusChange(Order order, OrderUpdateDto orderUpdateDto) {
        if (!isStatusChangeAllowed(order.getStatus(), orderUpdateDto.getStatus())) {
            log.warn("Forbidden status change from {} to {}", order.getStatus(), orderUpdateDto.getStatus());
            throw new IllegalStatusChangeException("You are not allowed to change status from "
                    + order.getStatus() + " to " + orderUpdateDto.getStatus());
        }
    }

    private boolean isStatusChangeAllowed(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus == newStatus) {
            return true;
        }
        return switch (oldStatus) {
            case CREATING -> newStatus == OrderStatus.NEW || newStatus == OrderStatus.CANCELLED;
            case NEW -> newStatus == OrderStatus.PAYMENT_WAITING || newStatus == OrderStatus.CANCELLED;
            case PAYMENT_FAILED -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.PAYMENT_WAITING;
            case PAYMENT_RECEIVED -> newStatus == OrderStatus.PROCESSING;
            case PROCESSING -> newStatus == OrderStatus.DELIVERED;
            default -> false;
        };
    }
}
