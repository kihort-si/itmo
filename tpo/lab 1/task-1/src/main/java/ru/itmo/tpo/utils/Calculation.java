package ru.itmo.tpo.utils;

public final class Calculation {
    private Calculation() {};

    private static final double INV_SQRT2 = 1.0 / Math.sqrt(2.0);

    public static double arcsin(double x, double eps, int maxIter) {
        if (Double.isNaN(x)) return Double.NaN;

        if (!Double.isFinite(eps) || eps <= 0.0) {
            throw new IllegalArgumentException("eps должен быть конечным и > 0");
        }
        if (maxIter <= 0) {
            throw new IllegalArgumentException("maxIter должен быть > 0");
        }

        if (x < -1.0 || x > 1.0) {
            throw new IllegalArgumentException("x должен быть в диапазоне [-1, 1] для вещественного arcsin");
        }

        if (x == 1.0) return Math.PI / 2.0;
        if (x == -1.0) return -Math.PI / 2.0;
        if (x == 0.0) return 0.0;

        double ax = Math.abs(x);

        if (ax > INV_SQRT2) {
            double t = 1.0 - x * x;
            if (t < 0.0) t = 0.0;
            double y = Math.sqrt(t);
            double core = arcsinSeriesCore(y, eps, maxIter);
            double res = (Math.PI / 2.0) - core;
            return Math.copySign(res, x);
        }

        return arcsinSeriesCore(x, eps, maxIter);
    }

    private static double arcsinSeriesCore(double x, double eps, int maxIter) {
        double x2 = x * x;

        double term = x;
        double sum = 0.0;

        double c = 0.0;

        for (int n = 0; n < maxIter; n++) {
            double y = term - c;
            double t = sum + y;
            c = (t - sum) - y;
            sum = t;

            if (Math.abs(term) <= eps) {
                return sum;
            }

            int k = n + 1;
            double a = 2.0 * k - 1.0;
            double ratio = (a * a * x2) / ((2.0 * k) * (2.0 * k + 1.0));
            term *= ratio;

            if (term == 0.0) {
                return sum;
            }
        }

        throw new ArithmeticException(
                "Ряд не сошелся за maxIter=" + maxIter + " итераций (возможно, eps слишком мал или x близок к ±1)"
        );
    }
}
