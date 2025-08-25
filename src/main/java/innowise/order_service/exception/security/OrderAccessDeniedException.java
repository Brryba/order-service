package innowise.order_service.exception.security;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class OrderAccessDeniedException extends StatusCodeException {
    public OrderAccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
