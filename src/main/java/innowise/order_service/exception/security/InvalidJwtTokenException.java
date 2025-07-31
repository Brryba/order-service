package innowise.order_service.exception.security;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class InvalidJwtTokenException extends StatusCodeException {
    public InvalidJwtTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
