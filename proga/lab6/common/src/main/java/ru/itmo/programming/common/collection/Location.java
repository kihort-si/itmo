package ru.itmo.programming.common.collection;

import ru.itmo.programming.common.utils.Verification;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Verification, Serializable {
    private Float x; //Поле не может быть null
    private Long y; //Поле не может быть null
    private int z;

    public Location(Float x, Long y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean verificate() {
        return x != null && y != null;
    }

    public Float getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return z == location.z && Objects.equals(x, location.x) && Objects.equals(y, location.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
