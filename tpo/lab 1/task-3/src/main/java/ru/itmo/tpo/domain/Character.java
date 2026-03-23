package ru.itmo.tpo.domain;

public abstract class Character implements Clearer {
    private final String name;
    private final double clearancePerSecond;

    protected Character(String name, double clearancePerSecond) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (clearancePerSecond <= 0) {
            throw new IllegalArgumentException("clearancePerSecond must be > 0");
        }
        this.name = name;
        this.clearancePerSecond = clearancePerSecond;
    }

    @Override
    public double clearancePerSecond() {
        return clearancePerSecond;
    }

    @Override
    public String name() {
        return name;
    }
}
