package innowise.order_service.exception.microservices;

import innowise.order_service.exception.StatusCodeException;
import org.springframework.http.HttpStatus;

public class UserServiceCommunicationException extends StatusCodeException {
    public UserServiceCommunicationException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
