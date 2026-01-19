package ru.itmo.se.is.cw.dto;

import lombok.Data;

@Data
public class AccountResponseDto {
    Long accountId;
    String username;
    Boolean enabled;
}
