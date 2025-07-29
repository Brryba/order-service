package innowise.order_service.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
