package ru.itmo.server.database;

import ru.itmo.common.collection.*;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseManger {
    private final ConnectionManager connectionManager;

    public DatabaseManger(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public CopyOnWriteArrayList<Person> readDatabase() throws SQLException {
        String query = "SELECT p.id AS person_id, p.name AS person_name, p.creationDate AS person_creationDate, " +
                "p.height AS person_height, p.weight AS person_weight, p.eyeColor AS person_eyeColor, " +
                "p.nationality AS person_nationality, p.creator AS person_creator, " +
                "c.x AS coordinates_x, c.y AS coordinates_y, " +
                "l.x AS location_x, l.y AS location_y, l.z AS location_z " +
                "FROM person p " +
                "JOIN coordinates c ON p.coordinates = c.id " +
                "JOIN location l ON p.location = l.id";

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
            while (result.next()) {
                long id = result.getInt("person_id");
                String name = result.getString("person_name");
                Float x_coordinate = result.getFloat("coordinates_x");
                Float y_coordinate = result.getFloat("coordinates_y");
                Timestamp creationTimestamp = result.getTimestamp("person_creationDate");
                ZonedDateTime creationDate = creationTimestamp.toInstant().atZone(ZoneId.systemDefault());
                Double height = result.getDouble("person_height");
                Double weight = result.getDouble("person_weight");
                Color eyeColor = Color.valueOf(result.getString("person_eyeColor"));
                Country nationality = Country.valueOf(result.getString("person_nationality"));
                Float x_location = result.getFloat("location_x");
                Long y_location = result.getLong("location_y");
                int z_location = result.getInt("location_z");
                int creator = result.getInt("person_creator");

                Person person = new Person(
                        id,
                        name,
                        new Coordinates(x_coordinate, y_coordinate),
                        creationDate,
                        height,
                        weight,
                        eyeColor,
                        nationality,
                        new Location(x_location, y_location, z_location),
                        creator
                );
                people.add(person);
            }
            return people;
        }
    }


    public Long addPerson(Person person) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement coordinatesStatement = connection.prepareStatement(
                    "INSERT INTO coordinates(x, y) VALUES (?, ?) RETURNING id"
            );
            coordinatesStatement.setFloat(1, person.getCoordinates().getX());
            coordinatesStatement.setFloat(2, person.getCoordinates().getY());

            ResultSet result = coordinatesStatement.executeQuery();
            result.next();
            int coordinatesId = result.getInt("id");
            result.close();
            coordinatesStatement.close();

            PreparedStatement locationStatement = connection.prepareStatement(
                    "INSERT INTO location(x, y, z) VALUES (?, ?, ?) RETURNING id"
            );
            locationStatement.setFloat(1, person.getLocation().getX());
            locationStatement.setDouble(2, person.getLocation().getY());
            locationStatement.setInt(3, person.getLocation().getZ());

            result = locationStatement.executeQuery();
            result.next();
            int locationId = result.getInt("id");
            result.close();
            locationStatement.close();

            PreparedStatement personStatement = connection.prepareStatement(
                    "INSERT INTO person(name, coordinates, height, weight, eyeColor, nationality, location, creator) " +
                            "VALUES (?, ?, ?, ?, ?::eyeColor, ?::nationality, ?, ?) RETURNING id"
            );
            personStatement.setString(1, person.getName());
            personStatement.setInt(2, coordinatesId);
            personStatement.setDouble(3, person.getHeight());
            personStatement.setDouble(4, person.getWeight());
            personStatement.setString(5, String.valueOf(person.getEyeColor()));
            personStatement.setString(6, String.valueOf(person.getNationality()));
            personStatement.setInt(7, locationId);
            personStatement.setInt(8, UserManager.getCurrentUser());

            result = personStatement.executeQuery();
            result.next();
            Long personId = (long) result.getInt("id");
            result.close();
            personStatement.close();

            connection.commit();
            connection.close();

            return personId;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }


    public void updatePerson(Person person) throws SQLException {
        Connection connection = connectionManager.getConnection();
        connection.setAutoCommit(false);
        PreparedStatement coordinatesStatement = connection.prepareStatement(
                "SELECT coordinates FROM person WHERE id = ?"
        );
        coordinatesStatement.setInt(1, (int) person.getId());
        ResultSet coordinatesResult = coordinatesStatement.executeQuery();


        PreparedStatement locationStatement = connection.prepareStatement(
                "SELECT location FROM person WHERE id = ?"
        );
        locationStatement.setInt(1, (int) person.getId());
        ResultSet locationResult = locationStatement.executeQuery();

        if (coordinatesResult.next() && locationResult.next()) {
            int coordinatesId = coordinatesResult.getInt("coordinates");
            int locationId = locationResult.getInt("location");

            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE coordinates SET x = ?, y = ? WHERE id = ?;" +
                        "UPDATE location SET x = ?, y = ?, z = ? WHERE id = ?;" +
                        "UPDATE person SET name = ?, height = ?, weight = ?, eyecolor = ?::eyecolor, nationality = ?::nationality WHERE id = ?;"
            );

            statement.setFloat(1, person.getCoordinates().getX());
            statement.setFloat(2, person.getCoordinates().getY());
            statement.setInt(3, coordinatesId);
            statement.setFloat(4, person.getLocation().getX());
            statement.setDouble(5, person.getLocation().getY());
            statement.setInt(6, person.getLocation().getZ());
            statement.setInt(7, locationId);
            statement.setString(8, person.getName());
            statement.setDouble(9, person.getHeight());
            statement.setDouble(10, person.getWeight());
            statement.setString(11, String.valueOf(person.getEyeColor()));
            statement.setString(12, String.valueOf(person.getNationality()));
            statement.setInt(13, (int) person.getId());

            statement.executeQuery();

            connection.commit();
            connection.close();
        }

    }

    public void removePerson(long id, int creator) throws SQLException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM person WHERE id = ? AND creator = ?"
             )) {

            statement.setLong(1, id);
            statement.setInt(2, creator);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void removeLower(double height, int creator) throws SQLException {
        try {
            Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM person WHERE height < ? AND  creator = ?"
            );

            statement.setDouble(1, height);
            statement.setInt(2, creator);

            statement.executeUpdate();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void clear() throws SQLException {
        Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM person WHERE creator = ?"
        );

        statement.setInt(1, UserManager.getCurrentUser());

        statement.executeUpdate();

        connection.close();
    }
}
