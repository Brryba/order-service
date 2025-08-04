package innowise.order_service.service;

import innowise.order_service.dto.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableRetry
public class UserServiceClient {
    private final RestTemplate restTemplate;

    private final static int MAX_RETRY_ATTEMPTS = 3;

    @Value("${microservices.url.user_service}")
    private String userServiceUrl;

    @Retryable(retryFor = {RestClientException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public UserResponseDto getUserById(Long userId, String token) {
        String url = userServiceUrl + "/api/user/me";

        log.info("Sending request to user service: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                UserResponseDto.class
        );

        UserResponseDto userResponseDto = response.getBody();

        log.info("Received response from user service");

        return userResponseDto;
    }

    @Recover
    public UserResponseDto recoverSendMockUser(RestClientException e, Long userId, String token) {
        log.warn("Error {} receiving response", e.getMessage());
        log.warn("Unable to get response form user service after {} attempts.", MAX_RETRY_ATTEMPTS);

        return UserResponseDto.builder()
                .id(userId)
                .name("Unable to get user info. Try again later.")
                .build();
    }
}
