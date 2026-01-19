package ru.itmo.se.is.cw.dto;

import lombok.Data;
import ru.itmo.se.is.cw.model.value.AccountRole;

@Data
public class CurrentUserResponseDto {
    private Long accountId;
    private String username;
    private AccountRole role;
    private EmployeeResponseDto employee;
    private ClientResponseDto client;
}
