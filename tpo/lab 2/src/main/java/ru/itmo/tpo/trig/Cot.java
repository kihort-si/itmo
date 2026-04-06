package ru.itmo.tpo.trig;

import ru.itmo.tpo.basic.Sin;

public class Cot {
    private static final double ZERO_TOL = 1e-8;

    private final Sin sin;
    private final Cos cos;

    public Cot(Sin sin, Cos cos) {
        this.sin = sin;
        this.cos = cos;
    }

    public double calculate(double x) {
        double sinValue = sin.calculate(x);
        if (Math.abs(sinValue) < ZERO_TOL) {
            return Double.NaN;
        }
        return cos.calculate(x) / sinValue;
    }
}
