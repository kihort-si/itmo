package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Фото товара")
public class ProductPhotoDto {
    @Schema(description = "Уникальный идентификатор фото", example = "701")
    private Long id;

    @Schema(description = "Уникальный идентификатор связанного файла", example = "201")
    private Long fileId;
}
