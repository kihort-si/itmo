package ru.itmo.tpo.log;

import ru.itmo.tpo.basic.Ln;

public class Log5 {
    private final Ln ln;

    public Log5(Ln ln) {
        this.ln = ln;
    }

    public double calculate(double x) {
        double lnX = ln.calculate(x);
        double ln5 = ln.calculate(5.0);
        if (Double.isNaN(lnX) || Double.isNaN(ln5) || Math.abs(ln5) < 1e-15) {
            return Double.NaN;
        }
        return lnX / ln5;
    }
}
