package ru.itmo.service;

import org.mindrot.jbcrypt.BCrypt;
import ru.itmo.exception.ApiException;
import ru.itmo.model.UserCredentials;
import ru.itmo.repository.UserRepository;
import ru.itmo.security.JwtService;

import java.util.Optional;
import java.util.regex.Pattern;

public final class AuthService {
    private static final Pattern VALID_LOGIN = Pattern.compile("[A-Za-z0-9_.-]{3,32}");
    private static final int BCRYPT_COST = 12;
    private static final String DUMMY_HASH = "$2a$12$KIXxFLJGh.NnFe.JCf8KROXKj7B3.wjPKmmvY14pgbS6aDmdEVm66";

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public void register(String login, String password) {
        validateRequired(login, password);
        if (!VALID_LOGIN.matcher(login).matches()) {
            throw new ApiException(400, "Login must be 3-32 characters: letters, numbers, '.', '_' or '-'");
        }
        validatePassword(password);

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
        if (!userRepository.create(login, passwordHash)) {
            throw new ApiException(409, "Login is already registered");
        }
    }

    public String login(String login, String password) {
        validateRequired(login, password);
        Optional<UserCredentials> user = userRepository.findByLogin(login);
        String storedHash = user.map(UserCredentials::passwordHash).orElse(DUMMY_HASH);

        if (!BCrypt.checkpw(password, storedHash) || user.isEmpty()) {
            throw new ApiException(401, "Invalid login or password");
        }
        return jwtService.issue(user.get().login());
    }

    private static void validateRequired(String login, String password) {
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            throw new ApiException(400, "Login and password are required");
        }
    }

    private static void validatePassword(String password) {
        if (password.length() < 12 || password.length() > 72
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new ApiException(
                    400,
                    "Password must be 12-72 characters and contain upper-case, lower-case and numeric characters"
            );
        }
    }
}
