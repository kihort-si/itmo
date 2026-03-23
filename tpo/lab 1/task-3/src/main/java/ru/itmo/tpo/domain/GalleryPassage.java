package ru.itmo.tpo.domain;

import java.time.Duration;

public class GalleryPassage {
    private final String id;
    private final Blockage blockage;
    private final double baseDarkness;
    private double dustLevel;

    public GalleryPassage(String id, Blockage blockage, double dustLevel, double baseDarkness) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (blockage == null) {
            throw new IllegalArgumentException("blockage must not be null");
        }
        this.id = id;
        this.blockage = blockage;
        this.dustLevel = clamp(dustLevel);
        this.baseDarkness = clamp(baseDarkness);
    }

    public double clearBy(Clearer clearer, Duration duration) {
        if (clearer == null) {
            throw new IllegalArgumentException("clearer must not be null");
        }
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be non-negative");
        }
        double seconds = duration.toMillis() / 1000.0;
        return blockage.clear(clearer.clearancePerSecond() * seconds);
    }

    public PassageState state() {
        if (blockage.isCleared()) {
            return PassageState.PASSABLE;
        }
        if (blockage.progress() > 0.0) {
            return PassageState.PARTIALLY_CLEARED;
        }
        return PassageState.BLOCKED;
    }

    public boolean isPassable() {
        return state() == PassageState.PASSABLE;
    }

    public double visibility(LightSource light, AirState airState) {
        if (light == null || airState == null) {
            throw new IllegalArgumentException("light and airState must not be null");
        }
        double effectiveLight = light.effectiveLight(dustLevel);
        double darknessPenalty = baseDarkness * 0.7 + dustLevel * 0.2 + airState.rawness() * 0.1;
        return clamp(effectiveLight - darknessPenalty);
    }

    public String id() {
        return id;
    }

    public Blockage blockage() {
        return blockage;
    }

    public double dustLevel() {
        return dustLevel;
    }

    public void setDustLevel(double dustLevel) {
        this.dustLevel = clamp(dustLevel);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
