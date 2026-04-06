package ru.itmo.tpo.basic;

public class Sin {
    private final double epsilon;

    public Sin(double epsilon) {
        this.epsilon = epsilon;
    }

    public double calculate(double x) {
        x = normalizeAngle(x);

        double pi = Math.PI;
        if (Math.abs(x) < epsilon) return 0.0;
        if (Math.abs(Math.abs(x) - pi) < epsilon) return 0.0;
        if (Math.abs(Math.abs(x) - 2*pi) < epsilon) return 0.0;

        double term = x;
        double sum = term;
        int n = 1;

        while (Math.abs(term) > epsilon) {
            term *= -x * x / ((2 * n) * (2 * n + 1));
            sum += term;
            n++;
        }

        return sum;
    }

    private double normalizeAngle(double x) {
        double twoPi = 2 * Math.PI;
        while (x > twoPi) x -= twoPi;
        while (x < -twoPi) x += twoPi;
        return x;
    }
}
