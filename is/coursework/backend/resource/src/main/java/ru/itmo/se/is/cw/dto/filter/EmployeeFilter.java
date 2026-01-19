package ru.itmo.se.is.cw.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.itmo.se.is.cw.model.value.EmployeeRole;

@Data
@Schema(description = "Фильтр по сотрудникам")
public class EmployeeFilter {
    @Schema(description = "Фильтровать по роли сотрудника")
    private EmployeeRole role;
}
