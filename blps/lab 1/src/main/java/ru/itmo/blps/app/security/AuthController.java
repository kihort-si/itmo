package ru.itmo.blps.app.security;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.LoginRequest;
import ru.itmo.blps.app.security.dto.RegisterCourierRequest;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    public AuthResponse registerCustomer(@RequestBody @Valid RegisterCustomerRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register/shop-assistant")
    public AuthResponse registerShopAssistant(@RequestBody @Valid RegisterShopAssistantRequest request) {
        return authService.registerShopAssistant(request);
    }

    @PostMapping("/register/courier")
    public AuthResponse registerCourier(@RequestBody @Valid RegisterCourierRequest request) {
        return authService.registerCourier(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            authService.logout(authorizationHeader.substring(7));
        }
        return Map.of("message", "Токен нужно удалить на клиенте");
    }
}
