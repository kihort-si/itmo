package ru.itmo.se.is.cw.dto;

import lombok.Data;
import ru.itmo.se.is.cw.model.value.AccountRole;

@Data
public class AccountRequestDto {
    private String username;
    private String password;
    private AccountRole role;
}
