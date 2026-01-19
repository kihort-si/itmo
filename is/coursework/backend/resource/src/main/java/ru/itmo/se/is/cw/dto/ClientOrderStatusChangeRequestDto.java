package ru.itmo.se.is.cw.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.ClientOrderStatus;

@Data
@Schema(description = "Запрос на изменение статуса заказа")
public class ClientOrderStatusChangeRequestDto {
    @Schema(description = "Новый статус заказа")
    private ClientOrderStatus status;

    @Schema(description = "Комментарий к изменению статуса", example = "Клиент подтвердил готовность к оплате")
    private String comment;
}
