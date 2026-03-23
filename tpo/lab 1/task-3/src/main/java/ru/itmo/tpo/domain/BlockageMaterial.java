package ru.itmo.tpo.domain;

public enum BlockageMaterial {
    RUBBLE(1.0),
    ENTRAILS(1.3);

    private final double resistance;

    BlockageMaterial(double resistance) {
        this.resistance = resistance;
    }

    public double resistance() {
        return resistance;
    }
}
