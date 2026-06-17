package ru.itmo.blps.app.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.blps.app.security.dto.AuthResponse;
import ru.itmo.blps.app.security.dto.RegisterCourierRequest;
import ru.itmo.blps.app.security.dto.RegisterCustomerRequest;
import ru.itmo.blps.app.security.dto.RegisterShopAssistantRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Регистрация пользователей. Авторизация осуществляется через HTTP Basic.")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    @Operation(
            summary = "Регистрация покупателя",
            description = "Создаёт учётную запись покупателя. Возвращает данные для последующей аутентификации по HTTP Basic.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Покупатель зарегистрирован",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или email уже занят",
                            content = @Content(schema = @Schema(example = "{\"error\": \"email: must be a well-formed email address\"}"))),
            }
    )
    public AuthResponse registerCustomer(@RequestBody @Valid RegisterCustomerRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register/shop-assistant")
    @Operation(
            summary = "Регистрация сотрудника салона",
            description = "Создаёт учётную запись сотрудника салона связи. Требует указания ID существующего салона.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Сотрудник зарегистрирован",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                            content = @Content(schema = @Schema(example = "{\"error\": \"name: must not be blank\"}"))),
                    @ApiResponse(responseCode = "404", description = "Салон с указанным ID не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Салон связи не найден: id=5\"}")))
            }
    )
    public AuthResponse registerShopAssistant(@RequestBody @Valid RegisterShopAssistantRequest request) {
        return authService.registerShopAssistant(request);
    }

    @PostMapping("/register/courier")
    @Operation(
            summary = "Регистрация курьера",
            description = "Создаёт учётную запись курьера. Номер паспорта должен быть уникальным.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Курьер зарегистрирован",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или паспорт уже зарегистрирован",
                            content = @Content(schema = @Schema(example = "{\"error\": \"passportId: must not be blank\"}")))
            }
    )
    public AuthResponse registerCourier(@RequestBody @Valid RegisterCourierRequest request) {
        return authService.registerCourier(request);
    }
}
