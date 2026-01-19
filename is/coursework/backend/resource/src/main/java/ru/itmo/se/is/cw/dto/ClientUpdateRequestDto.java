package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление клиента")
public class ClientUpdateRequestDto {
    @Schema(description = "Почта", example = "email@example.com!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Имя", example = "Петр", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Schema(description = "Фамилия", example = "Петров", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Schema(description = "Номер телефона", example = "+70000000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
}
