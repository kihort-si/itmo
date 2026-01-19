package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о человеке")
public class PersonResponseDto {
    @Schema(description = "Уникальный идентификатор человека", example = "123")
    private Long id;

    @Schema(description = "Имя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия", example = "Иванов")
    private String lastName;
}
