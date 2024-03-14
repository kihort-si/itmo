package ru.itmo.programming.collections;

import ru.itmo.programming.utils.Verification;

import java.util.Objects;

/**
 * @author Nikita Vasilev
 */
public class Location implements Verification {
    private Float x; //Поле не может быть null
    private Long y; //Поле не может быть null
    private int z;

    public Location(Float x, Long y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return collection element X location coordinates
     */
    public Float getX() {
        return x;
    }

    /**
     * @return collection element Y location coordinates
     */
    public Long getY() {
        return y;
    }

    /**
     * @return collection element Z location coordinates
     */
    public int getZ() {
        return z;
    }

    @Override
    public boolean verificate() {
        return x != null && y != null;
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Location location = (Location) object;
        return z == location.z && Objects.equals(x, location.x) && Objects.equals(y, location.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
