package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Запрос на отправку сообщения")
public class SendMessageRequestDto {
    @Schema(description = "Содержание сообщения", example = "Новый комментарий.")
    private String content;

    @Schema(description = "Список идентификаторов файлов-вложений", example = "[201, 202]")
    private List<Long> attachmentFileIds;
}
