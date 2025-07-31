package innowise.order_service.controller;

import innowise.order_service.dto.error.ErrorDto;
import innowise.order_service.dto.error.ValidationErrorDto;
import innowise.order_service.exception.StatusCodeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            errors.add(error.getDefaultMessage());
        }

        ValidationErrorDto validationError = ValidationErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("400 - Bad request")
                .validationErrors(errors)
                .message("Validation Error")
                .path(request.getRequestURI())
                .requestType(request.getMethod())
                .build();

        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StatusCodeException.class)
    public ResponseEntity<ErrorDto> handleHttpStatusCodeException(StatusCodeException ex, HttpServletRequest request) {
        ErrorDto errorDto = ErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getHttpStatus().value())
                .error(ex.getHttpStatus().value() + " " + ex.getHttpStatus().getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .requestType(request.getMethod())
                .build();

        return new ResponseEntity<>(errorDto, HttpStatus.valueOf(ex.getHttpStatus().value()));
    }
}
