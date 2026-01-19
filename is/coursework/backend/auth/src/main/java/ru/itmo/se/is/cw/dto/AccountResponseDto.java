package ru.itmo.se.is.cw.dto;

import lombok.Data;

@Data
public class AccountResponseDto {
    private Long accountId;
    private String username;
    private Boolean enabled;
}
