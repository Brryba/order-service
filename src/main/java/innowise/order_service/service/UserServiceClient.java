package innowise.order_service.service;

import innowise.order_service.dto.user.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceClient {
    private final RestTemplate restTemplate;

    @Value("${microservices.url.user_service}")
    private String userServiceUrl;

    public UserResponseDto getUserById(Long userId, String token) {
        String url = userServiceUrl + "/api/user/me";

        try {
            log.info("Sending request to user service: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization  ", token);
            HttpEntity<?> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    UserResponseDto.class
            );

            log.info("Received response from user service: {}", response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error calling user service for userId: {}", userId, e);
            //TODO: proper exception handling
            throw new RuntimeException("Failed to fetch user data", e);
        }
    }
}
