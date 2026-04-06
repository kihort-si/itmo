package ru.itmo.tpo.system;

import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.log.Log2;
import ru.itmo.tpo.log.Log5;
import ru.itmo.tpo.log.Log10;
import ru.itmo.tpo.trig.Cos;
import ru.itmo.tpo.trig.Cot;
import ru.itmo.tpo.trig.Sec;
import ru.itmo.tpo.trig.Tan;

public class FunctionSystem {
    private static final double ZERO_TOL = 1e-8;

    private final Sin sin;
    private final Cos cos;
    private final Tan tan;
    private final Cot cot;
    private final Sec sec;
    private final Ln ln;
    private final Log2 log2;
    private final Log5 log5;
    private final Log10 log10;

    public FunctionSystem(double epsilon) {
        this.sin = new Sin(epsilon);
        this.cos = new Cos(sin);
        this.tan = new Tan(sin, cos);
        this.cot = new Cot(sin, cos);
        this.sec = new Sec(cos);
        this.ln = new Ln(epsilon);
        this.log2 = new Log2(ln);
        this.log5 = new Log5(ln);
        this.log10 = new Log10(ln);
    }

    public FunctionSystem(Sin sin, Cos cos, Tan tan, Cot cot, Sec sec,
                          Ln ln, Log2 log2, Log5 log5, Log10 log10) {
        this.sin = sin;
        this.cos = cos;
        this.tan = tan;
        this.cot = cot;
        this.sec = sec;
        this.ln = ln;
        this.log2 = log2;
        this.log5 = log5;
        this.log10 = log10;
    }

    public double calculate(double x) {
        if (x <= 0) {
            return calculateTrig(x);
        } else {
            return calculateLog(x);
        }
    }

    public double calculateTrig(double x) {
        double cotX = cot.calculate(x);
        double secX = sec.calculate(x);
        double tanX = tan.calculate(x);
        double sinX = sin.calculate(x);
        double cosX = cos.calculate(x);

        if (Double.isNaN(cotX) || Double.isNaN(secX) || Double.isNaN(tanX) ||
                Double.isNaN(sinX) || Double.isNaN(cosX)) {
            return Double.NaN;
        }

        if (Math.abs(cosX) < ZERO_TOL) {
            return Double.NaN;
        }

        double denominator = (secX + tanX) * (sinX / cosX);

        if (Math.abs(denominator) < ZERO_TOL) {
            return Double.NaN;
        }

        return (((cotX * cotX) - secX) - secX) / denominator + cotX;
    }

    public double calculateLog(double x) {
        double log2X = log2.calculate(x);
        double log5X = log5.calculate(x);
        double log10X = log10.calculate(x);
        double lnX = ln.calculate(x);

        if (Double.isNaN(log2X) || Double.isNaN(log5X) || Double.isNaN(log10X) || Double.isNaN(lnX)) {
            return Double.NaN;
        }

        return ((((log2X * log2X) * log5X) * (log5X * lnX)) - (log10X + log10X)) * lnX;
    }

    public Sin getSin() { return sin; }
    public Cos getCos() { return cos; }
    public Tan getTan() { return tan; }
    public Cot getCot() { return cot; }
    public Sec getSec() { return sec; }
    public Ln getLn() { return ln; }
    public Log2 getLog2() { return log2; }
    public Log5 getLog5() { return log5; }
    public Log10 getLog10() { return log10; }
}
