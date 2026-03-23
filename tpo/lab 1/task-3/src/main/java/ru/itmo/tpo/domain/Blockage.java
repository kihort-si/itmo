package ru.itmo.tpo.domain;

public class Blockage {
    private final BlockageMaterial material;
    private final double totalMass;
    private double remainingMass;

    public Blockage(BlockageMaterial material, double totalMass) {
        if (material == null) {
            throw new IllegalArgumentException("material must not be null");
        }
        if (totalMass <= 0) {
            throw new IllegalArgumentException("totalMass must be > 0");
        }
        this.material = material;
        this.totalMass = totalMass;
        this.remainingMass = totalMass;
    }

    public double clear(double rawAmount) {
        if (rawAmount < 0) {
            throw new IllegalArgumentException("clear amount must be >= 0");
        }
        double effectiveAmount = rawAmount / material.resistance();
        double removed = Math.min(remainingMass, effectiveAmount);
        remainingMass -= removed;
        return removed;
    }

    public boolean isCleared() {
        return remainingMass <= 1e-9;
    }

    public double progress() {
        return (totalMass - remainingMass) / totalMass;
    }

    public BlockageMaterial material() {
        return material;
    }

    public double totalMass() {
        return totalMass;
    }

    public double remainingMass() {
        return remainingMass;
    }
}
