package ru.itmo.tpo.trig;

import ru.itmo.tpo.basic.Sin;

public class Tan {
    private static final double ZERO_TOL = 1e-8;

    private final Sin sin;
    private final Cos cos;

    public Tan(Sin sin, Cos cos) {
        this.sin = sin;
        this.cos = cos;
    }

    public double calculate(double x) {
        double cosValue = cos.calculate(x);
        if (Math.abs(cosValue) < ZERO_TOL) {
            return Double.NaN;
        }
        return sin.calculate(x) / cosValue;
    }
}
