package ru.itmo.se.is.cw.dto;

import lombok.Data;
import ru.itmo.se.is.cw.model.value.AccountRole;

@Data
public class AccountRequestDto {
    String username;
    String password;
    AccountRole role;
}
