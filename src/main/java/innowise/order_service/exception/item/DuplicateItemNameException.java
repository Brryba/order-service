package innowise.order_service.exception.item;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class DuplicateItemNameException extends StatusCodeException {
    public DuplicateItemNameException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
