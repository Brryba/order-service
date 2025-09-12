package innowise.order_service.exception.order;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class IllegalNewOrderException extends StatusCodeException {
    public IllegalNewOrderException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
