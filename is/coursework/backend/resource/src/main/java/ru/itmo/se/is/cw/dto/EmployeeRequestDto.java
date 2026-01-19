package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.EmployeeRole;

@Data
@Schema(description = "Запрос на создание сотрудника")
public class EmployeeRequestDto {
    @Schema(description = "Имя пользователя", example = "petrov")
    private String username;

    @Schema(description = "Пароль", example = "password123")
    private String password;

    @Schema(description = "Имя сотрудника", example = "Петр")
    private String firstName;

    @Schema(description = "Фамилия сотрудника", example = "Петров")
    private String lastName;

    @Schema(description = "Роль сотрудника")
    private EmployeeRole role;
}
