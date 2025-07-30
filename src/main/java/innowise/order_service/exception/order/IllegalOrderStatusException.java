package innowise.order_service.exception.order;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;

public class IllegalOrderStatusException extends StatusCodeException {
    public IllegalOrderStatusException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
