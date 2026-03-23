package ru.itmo.tpo.domain;

public class AirState {
    private double rawness;
    private double flowFromDepths;

    public AirState(double rawness, double flowFromDepths) {
        this.rawness = clamp(rawness);
        this.flowFromDepths = clamp(flowFromDepths);
    }

    public void intensifyFromDepths(double delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be >= 0");
        }
        flowFromDepths = clamp(flowFromDepths + delta);
        rawness = clamp(rawness + delta * 0.5);
    }

    public double rawness() {
        return rawness;
    }

    public double flowFromDepths() {
        return flowFromDepths;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
