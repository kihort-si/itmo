package ru.itmo.se.is.cw.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.se.is.cw.dto.ClientUpdateRequestDto;
import ru.itmo.se.is.cw.dto.CurrentUserResponseDto;
import ru.itmo.se.is.cw.dto.ChangePasswordRequestDto;
import ru.itmo.se.is.cw.service.CurrentUserService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Операции с профилем пользователя")
public class CurrentUserController {
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(currentUserService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(@RequestBody ClientUpdateRequestDto request) {
        currentUserService.updateCurrentUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDto request) {
        currentUserService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
