package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Фильтр по клиентам.")
public class ClientFilter {

    @Schema(description = "Email клиента", example = "ivanov@example.com")
    private String email;

    @Schema(description = "Имя клиента", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия клиента", example = "Иванов")
    private String lastName;

    @Schema(description = "Логин (username) клиента", example = "ivanov")
    private String username;

    @Schema(description = "Номер телефона", example = "+79991234567")
    private String phoneNumber;
}
