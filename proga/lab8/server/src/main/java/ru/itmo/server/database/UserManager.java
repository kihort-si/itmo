package ru.itmo.server.database;

import ru.itmo.common.user.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

/**
 * @author Nikita Vasilev
 */
public class UserManager {
    private final ConnectionManager connectionManager;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String PEPPER = "9r&ZjqGl";
    private static int currentUser = 0;

    public UserManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * @return
     */
    public static int getCurrentUser() {
        return currentUser;
    }

    /**
     * @param currentUser
     */
    public static void setCurrentUser(int currentUser) {
        UserManager.currentUser = currentUser;
    }

    /**
     * The method that creates an application user and writes information about the user to the database.
     * @param user the user that was created inside the programme and that needs to be written to the database.
     * @return the id that has been assigned to the user.
     * @throws SQLException if an unexpected error occurred while connecting to the database.
     * @throws NoSuchAlgorithmException if the SHA-384 hashing algorithm is not supported.
     */
    public int addUser(User user) throws SQLException, NoSuchAlgorithmException {
        byte[] salt = getSalt();
        String hashedPassword = getHash(user.getPassword(), salt);
        String combinedHash = Base64.getEncoder().encodeToString(salt) + ":" + hashedPassword;

        String insertQuery = "INSERT INTO \"user\" (login, password) VALUES (?, ?) RETURNING id";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, user.getLogin());
            preparedStatement.setString(2, combinedHash);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return -1;
    }

    /**
     * Allows to find out the id of a user by his login.
     * @param login string with the login of the user whose id is to be determined.
     * @return numeric representation of the user id with the given login.
     * @throws SQLException if an unexpected error occurred while connecting to the database.
     */
    public int getUser(String login) throws SQLException {
        Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM \"user\" WHERE login = ?"
        );
        statement.setString(1, login);

        ResultSet result = statement.executeQuery();

        connection.close();

        result.next();

        return result.getInt("id");
    }

    /**
     * Allows to find out the login of a user by his id.
     * @param id int equal to the id of the user whose login is to be determined.
     * @return string representation of the user login with the given id.
     * @throws SQLException if an unexpected error occurred while connecting to the database.
     */
    public String getUser(int id) throws SQLException {
        String query = "SELECT login FROM \"user\" WHERE id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("login");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Creates and generates a 16-byte salt for a password.
     * @return the generated array of bytes representing the salt.
     */
    private static byte[] getSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Generates a hash with SHA-384 algorithm for the user password in the database and adds salt and pepper to it also in hashed format.
     * @param password the user-defined password to be hashed.
     * @param salt the pre-generated salt in the form of a byte array to be added to the password.
     * @return the hashed password along with salt and pepper.
     * @throws NoSuchAlgorithmException if the SHA-384 hashing algorithm is not supported.
     */
    private static String getHash(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        md.update(salt);
        md.update(PEPPER.getBytes());
        byte[] hashBytes = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    /**
     * Checks if a user with this login is registered.
     * @param login string value of the login to check for in the database.
     * @return boolean value whether a user with this login exists in the database.
     * @throws SQLException if an unexpected error occurred while connecting to the database.
     */
    public boolean checkUser(String login) throws SQLException {
        String query = "SELECT * FROM \"user\" WHERE login = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Checks if the user with this login has entered the correct password.
     * @param login string with the login of the user who is trying to authorize.
     * @param password the password entered by the user in the console, which must be checked to see if it matches the user's login.
     * @return boolean value whether the user has entered the correct password.
     * @throws SQLException if an unexpected error occurred while connecting to the database.
     */
    public boolean checkPassword(String login, String password) throws SQLException, NoSuchAlgorithmException {
        Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT password FROM \"user\" WHERE login = ?"
        );

        statement.setString(1, login);

        ResultSet result = statement.executeQuery();

        if (result.next()) {
            String storedPassword = result.getString("password");
            String[] parts = storedPassword.split(":");
            byte[] storedSalt = Base64.getDecoder().decode(parts[0]);
            String storedHash = parts[1];

            String hashedEnteredPassword = getHash(password, storedSalt);
            return hashedEnteredPassword.equals(storedHash);
        }
        return false;
    }
}
