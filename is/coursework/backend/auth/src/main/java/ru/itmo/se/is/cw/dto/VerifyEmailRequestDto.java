package ru.itmo.se.is.cw.dto;

import lombok.Data;

@Data
public class VerifyEmailRequestDto {
    private String token;
}
