package ru.itmo.repository;

import ru.itmo.model.UserCredentials;

import java.util.Optional;

public interface UserRepository {
    boolean create(String login, String passwordHash);

    Optional<UserCredentials> findByLogin(String login);
}
