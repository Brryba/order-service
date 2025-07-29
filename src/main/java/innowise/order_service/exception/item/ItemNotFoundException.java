package innowise.order_service.exception.item;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends StatusCodeException {
    public ItemNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
