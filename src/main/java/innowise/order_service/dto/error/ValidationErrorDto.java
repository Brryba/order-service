package innowise.order_service.dto.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
public class ValidationErrorDto extends ErrorDto {
    private List<String> validationErrors;
}
