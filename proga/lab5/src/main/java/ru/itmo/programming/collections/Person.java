package ru.itmo.programming.collections;

import ru.itmo.programming.utils.Verification;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Nikita Vasilev
 */
public class Person implements Comparable<Person>, Verification {
    private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Double height; //Поле не может быть null, Значение поля должно быть больше 0
    private double weight; //Значение поля должно быть больше 0
    private Color eyeColor; //Поле не может быть null
    private Country nationality; //Поле может быть null
    private Location location; //Поле может быть null

    public Person(long id, String name, Coordinates coordinates, ZonedDateTime creationDate, Double height, double weight, Color eyeColor, Country nationality, Location location) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.height = height;
        this.weight = weight;
        this.eyeColor = eyeColor;
        this.nationality = nationality;
        this.location = location;
    }

    /**
     * @return collection element ID
     */
    public long getId() {
        return id;
    }

    /**
     * @return collection element name
     */
    public String getName() {
        return name;
    }

    /**
     * @return collection element coordinates
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @return collection element date of creation
     */
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @return collection element height
     */
    public Double getHeight() {
        return height;
    }

    /**
     * @return collection element weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return collection element eyes color
     */
    public Color getEyeColor() {
        return eyeColor;
    }

    /**
     * @return collection element nationality
     */
    public Country getNationality() {
        return nationality;
    }


    /**
     * @return collection element location coordinates
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean verificate() {
        if (id <= 0) return false;
        if (name == null || name.isEmpty()) return false;
        if (coordinates == null) return false;
        if (creationDate == null) return false;
        if (height == null || height <= 0) return false;
        if (weight <= 0) return false;
        if (eyeColor == null) return false;
        if (nationality == null) return false;
        if (location == null) return false;
        else return true;
    }

    public void update(Person person) {
        this.name = person.getName();
        this.coordinates = person.getCoordinates();
        this.creationDate = person.getCreationDate();
        this.height = person.getHeight();
        this.weight = person.getWeight();
        this.eyeColor = person.getEyeColor();
        this.nationality = person.getNationality();
        this.location = person.getLocation();
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", coordinates=" + getCoordinates() +
                ", creationDate=" + getCreationDate() +
                ", height=" + getHeight() +
                ", weight=" + getWeight() +
                ", eyeColor=" + getEyeColor() +
                ", nationality=" + getNationality() +
                ", location=" + getLocation() +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Person person = (Person) object;
        return id == person.id && Double.compare(weight, person.weight) == 0 && Objects.equals(name, person.name) && Objects.equals(coordinates, person.coordinates) && Objects.equals(creationDate, person.creationDate) && Objects.equals(height, person.height) && eyeColor == person.eyeColor && nationality == person.nationality && Objects.equals(location, person.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, coordinates, creationDate, height, weight, eyeColor, nationality, location);
    }

    /**
     * @param p the object to be compared.
     * @return comparing collection elements to sort by height
     */
    public int compareTo(Person p) {
        return Double.compare(this.height, p.height);
    }
}