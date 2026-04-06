package ru.itmo.tpo.basic;

public class Ln {
    private final double epsilon;

    public Ln(double epsilon) {
        this.epsilon = epsilon;
    }

    public double calculate(double x) {
        if (x <= 0) {
            return Double.NaN;
        }

        double t = (x - 1) / (x + 1);
        double t2 = t * t;
        double term = t;
        double sum = term;
        int n = 1;

        while (Math.abs(term) > epsilon) {
            n++;
            term *= t2;
            sum += term / (2 * n - 1);
        }

        return 2 * sum;
    }
}
