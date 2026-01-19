package ru.itmo.se.is.cw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.se.is.cw.dto.AccountRequestDto;
import ru.itmo.se.is.cw.dto.AccountResponseDto;
import ru.itmo.se.is.cw.dto.ChangePasswordRequestDto;
import ru.itmo.se.is.cw.dto.VerifyEmailRequestDto;
import ru.itmo.se.is.cw.service.AuthService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @RequestBody VerifyEmailRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponseDto> createAccount(
            @RequestBody AccountRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAccount(request));
    }

    @PostMapping("/accounts/{id}/enable")
    public ResponseEntity<Void> enableAccount(@PathVariable Long id) {
        authService.enableAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/{id}/disable")
    public ResponseEntity<Void> disableAccount(@PathVariable Long id) {
        authService.disableAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequestDto request) {
        authService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getAccount(id));
    }
}
