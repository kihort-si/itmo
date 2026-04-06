package ru.itmo.tpo.log;

import ru.itmo.tpo.basic.Ln;

public class Log10 {
    private final Ln ln;

    public Log10(Ln ln) {
        this.ln = ln;
    }

    public double calculate(double x) {
        double lnX = ln.calculate(x);
        double ln10 = ln.calculate(10.0);
        if (Double.isNaN(lnX) || Double.isNaN(ln10) || Math.abs(ln10) < 1e-15) {
            return Double.NaN;
        }
        return lnX / ln10;
    }
}
