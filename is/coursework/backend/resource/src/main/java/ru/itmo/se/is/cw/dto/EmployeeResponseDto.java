package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.EmployeeRole;


@Data
@Schema(description = "Информация о сотруднике")
public class EmployeeResponseDto {
    @Schema(description = "Уникальный идентификатор сотрудника", example = "101")
    private Long id;

    @Schema(description = "Уникальный идентификатор аккаунта сотрудника", example = "102")
    private Long accountId;

    @Schema(description = "Персональные данные сотрудника")
    private PersonResponseDto person;

    @Schema(description = "Роль сотрудника", implementation = EmployeeRole.class)
    private EmployeeRole role;

    @Schema(description = "Имя пользователя аккаунта", example = "john.doe")
    private String username;

    @Schema(description = "Активен ли аккаунт", example = "true")
    private Boolean enabled;
}
