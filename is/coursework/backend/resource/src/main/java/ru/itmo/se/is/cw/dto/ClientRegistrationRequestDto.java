package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию клиента")
public class ClientRegistrationRequestDto {

    @Schema(description = "Имя пользователя (логин)", example = "client.user", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Пароль", example = "P@ssw0rd!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "Почта", example = "email@example.com!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Имя", example = "Петр", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Schema(description = "Фамилия", example = "Петров", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Schema(description = "Номер телефона", example = "+70000000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
}