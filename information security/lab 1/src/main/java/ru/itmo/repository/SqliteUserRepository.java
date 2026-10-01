package ru.itmo.repository;

import ru.itmo.config.Database;
import ru.itmo.exception.DataAccessException;
import ru.itmo.model.UserCredentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class SqliteUserRepository implements UserRepository {
    private static final String INSERT_USER = "INSERT INTO users(login, password_hash) VALUES (?, ?)";
    private static final String SELECT_BY_LOGIN = "SELECT login, password_hash FROM users WHERE login = ?";

    private final Database database;

    public SqliteUserRepository(Database database) {
        this.database = database;
    }

    @Override
    public boolean create(String login, String passwordHash) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER)) {
            statement.setString(1, login);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 19) {
                return false;
            }
            throw new DataAccessException("Could not create user", exception);
        }
    }

    @Override
    public Optional<UserCredentials> findByLogin(String login) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_LOGIN)) {
            statement.setString(1, login);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new UserCredentials(
                        result.getString("login"),
                        result.getString("password_hash")
                ));
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not read user", exception);
        }
    }
}
