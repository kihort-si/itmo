package ru.itmo.se.is.cw.dto;

import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    String currentPassword;
    String newPassword;
}
