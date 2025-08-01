package innowise.order_service.service;

import innowise.order_service.dto.user.UserResponseDto;
import innowise.order_service.exception.microservices.UserServiceCommunicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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

        log.info("Sending request to user service: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        UserResponseDto userResponseDto;
        try {
            ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    UserResponseDto.class
            );
            userResponseDto = response.getBody();
        } catch (RestClientException e) {
            userResponseDto = UserResponseDto.builder()
                    .id(userId)
                    .name("Unable to get user info. Try again later.")
                    .build();
        }

        log.info("Received response from user service: {}", userResponseDto);

        return userResponseDto;
    }
}
