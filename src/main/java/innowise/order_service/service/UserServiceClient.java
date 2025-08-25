package innowise.order_service.service;

import feign.FeignException;
import feign.RetryableException;
import innowise.order_service.dto.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableRetry
public class UserServiceClient {

    private final UserServiceFeignClient feignClient;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Retryable(
            retryFor = {RetryableException.class, FeignException.FeignServerException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000, multiplier = 1.5)
    )
    public UserResponseDto getUserById(Long userId) {
        log.info("Calling user-service for user {}", userId);
        UserResponseDto user = feignClient.getUserById(userId);

        log.info("User service returned user {} info", user.getId());
        return user;
    }

    @Recover
    public UserResponseDto recoverSendMockUser(Exception e, Long userId) {
        log.warn("Error {} receiving response for user {}", userId, e.getMessage());
        log.warn("Unable to get response from user service after {} attempts.", MAX_RETRY_ATTEMPTS);

        return UserResponseDto.builder()
                .id(userId)
                .name("Unable to get user info. Try again later.")
                .build();
    }
}
