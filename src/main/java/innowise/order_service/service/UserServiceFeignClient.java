package innowise.order_service.service;

import innowise.order_service.dto.user.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceFeignClient {
    @GetMapping("/api/user/me")
    UserResponseDto getUserById(@RequestHeader("X-User-Id") Long userId);
}
