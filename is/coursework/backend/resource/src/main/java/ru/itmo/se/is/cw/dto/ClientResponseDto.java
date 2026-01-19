package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Информация о клиенте")
public class ClientResponseDto {
    @Schema(description = "Уникальный идентификатор клиента", example = "456")
    private Long id;

    @Schema(description = "Email клиента", example = "client@example.com")
    private String email;

    @Schema(description = "Номер телефона клиента", example = "+71234567890")
    private String phoneNumber;

    @Schema(description = "Персональные данные клиента")
    private PersonResponseDto person;

    @Schema(description = "Уникальный идентификатор аккаунта клиента", example = "789")
    private Long accountId;
}
