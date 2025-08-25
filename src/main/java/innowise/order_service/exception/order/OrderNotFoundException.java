package innowise.order_service.exception.order;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends StatusCodeException {
    public OrderNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
