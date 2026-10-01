package ru.itmo.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
