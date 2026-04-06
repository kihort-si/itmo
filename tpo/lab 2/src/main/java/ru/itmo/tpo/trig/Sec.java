package ru.itmo.tpo.trig;

public class Sec {
    private static final double ZERO_TOL = 1e-8;

    private final Cos cos;

    public Sec(Cos cos) {
        this.cos = cos;
    }

    public double calculate(double x) {
        double cosValue = cos.calculate(x);
        if (Math.abs(cosValue) < ZERO_TOL) {
            return Double.NaN;
        }
        return 1.0 / cosValue;
    }
}
