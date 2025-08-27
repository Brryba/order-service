package innowise.order_service.exception.order;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class IllegalStatusChangeException extends StatusCodeException {
    public IllegalStatusChangeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
