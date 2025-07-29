package innowise.order_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public abstract class StatusCodeException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;
}
