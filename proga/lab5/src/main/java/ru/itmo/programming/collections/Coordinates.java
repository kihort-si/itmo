package ru.itmo.programming.collections;

import ru.itmo.programming.utils.Verification;

import java.util.Objects;

/**
 * @author Nikita Vasilev
 */
public class Coordinates implements Verification {
    private float x;
    private Float y; //Поле не может быть null

    public Coordinates(float x, Float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean verificate() {
        return y != null;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Coordinates that = (Coordinates) object;
        return Float.compare(x, that.x) == 0 && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
