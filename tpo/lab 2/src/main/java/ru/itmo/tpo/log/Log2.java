package ru.itmo.tpo.log;

import ru.itmo.tpo.basic.Ln;

public class Log2 {
    private final Ln ln;

    public Log2(Ln ln) {
        this.ln = ln;
    }

    public double calculate(double x) {
        double lnX = ln.calculate(x);
        double ln2 = ln.calculate(2.0);
        if (Double.isNaN(lnX) || Double.isNaN(ln2) || Math.abs(ln2) < 1e-15) {
            return Double.NaN;
        }
        return lnX / ln2;
    }
}
