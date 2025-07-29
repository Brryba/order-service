package innowise.order_service.exception.item;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DuplicateItemNameException extends ResponseStatusException {
    public DuplicateItemNameException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
