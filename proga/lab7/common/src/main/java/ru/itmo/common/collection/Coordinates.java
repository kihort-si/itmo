package ru.itmo.common.collection;

import ru.itmo.common.utils.Verification;

import java.io.Serializable;
import java.util.Objects;

public class Coordinates implements Verification, Serializable {
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

    public float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Float.compare(x, that.x) == 0 && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
