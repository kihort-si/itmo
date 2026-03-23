package ru.itmo.tpo.domain;

public class Flashlight implements LightSource {
    private final double luminance;

    public Flashlight(double luminance) {
        if (luminance < 0) {
            throw new IllegalArgumentException("luminance must be >= 0");
        }
        this.luminance = luminance;
    }

    @Override
    public double effectiveLight(double dustLevel) {
        double clampedDust = Math.max(0.0, Math.min(1.0, dustLevel));
        return luminance * (1.0 - 0.8 * clampedDust);
    }

    public double luminance() {
        return luminance;
    }
}
