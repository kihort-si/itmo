package ru.itmo.tpo;

import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.csv.CsvWriter;
import ru.itmo.tpo.log.Log2;
import ru.itmo.tpo.log.Log5;
import ru.itmo.tpo.log.Log10;
import ru.itmo.tpo.plot.PlotWriter;
import ru.itmo.tpo.system.FunctionSystem;
import ru.itmo.tpo.trig.Cos;
import ru.itmo.tpo.trig.Cot;
import ru.itmo.tpo.trig.Sec;
import ru.itmo.tpo.trig.Tan;

import java.io.IOException;

public class Main {
    private static final String OUTPUT_DIR = "out/csv";
    private static final String IMAGE_DIR = "out/img";
    private static final double STEP = 0.01;

    public static void main(String[] args) {
        double epsilon = 1e-7;
        FunctionSystem system = new FunctionSystem(epsilon);

        Sin sin = new Sin(epsilon);
        Ln ln = new Ln(epsilon);
        Cos cos = new Cos(sin);
        Tan tan = new Tan(sin, cos);
        Cot cot = new Cot(sin, cos);
        Sec sec = new Sec(cos);
        Log2 log2 = new Log2(ln);
        Log5 log5 = new Log5(ln);
        Log10 log10 = new Log10(ln);

        try {
            CsvWriter.write(OUTPUT_DIR + "/sin.csv", -2 * Math.PI, 2 * Math.PI, STEP, sin::calculate, "sin");
            CsvWriter.write(OUTPUT_DIR + "/cos.csv", -2 * Math.PI, 2 * Math.PI, STEP, cos::calculate, "cos");
            CsvWriter.write(OUTPUT_DIR + "/tan.csv", -2 * Math.PI, 2 * Math.PI, STEP, tan::calculate, "tan");
            CsvWriter.write(OUTPUT_DIR + "/cot.csv", -2 * Math.PI, 2 * Math.PI, STEP, cot::calculate, "cot");
            CsvWriter.write(OUTPUT_DIR + "/sec.csv", -2 * Math.PI, 2 * Math.PI, STEP, sec::calculate, "sec");

            CsvWriter.write(OUTPUT_DIR + "/ln.csv", 0.1, 10, STEP, ln::calculate, "ln");
            CsvWriter.write(OUTPUT_DIR + "/log2.csv", 0.1, 10, STEP, log2::calculate, "log2");
            CsvWriter.write(OUTPUT_DIR + "/log5.csv", 0.1, 10, STEP, log5::calculate, "log5");
            CsvWriter.write(OUTPUT_DIR + "/log10.csv", 0.1, 10, STEP, log10::calculate, "log10");

            CsvWriter.write(OUTPUT_DIR + "/system_trig.csv", -2 * Math.PI, 0, STEP, system::calculate, "system_trig");
            CsvWriter.write(OUTPUT_DIR + "/system_log.csv", 0.1, 10, STEP, system::calculate, "system_log");
            CsvWriter.write(OUTPUT_DIR + "/system.csv", -2 * Math.PI, 10, STEP, system::calculate, "system");

            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/sin.csv", IMAGE_DIR + "/sin.png", "sin(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/cos.csv", IMAGE_DIR + "/cos.png", "cos(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/tan.csv", IMAGE_DIR + "/tan.png", "tan(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/cot.csv", IMAGE_DIR + "/cot.png", "cot(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/sec.csv", IMAGE_DIR + "/sec.png", "sec(x)");

            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/ln.csv", IMAGE_DIR + "/ln.png", "ln(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/log2.csv", IMAGE_DIR + "/log2.png", "log2(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/log5.csv", IMAGE_DIR + "/log5.png", "log5(x)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/log10.csv", IMAGE_DIR + "/log10.png", "log10(x)");

            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/system_trig.csv", IMAGE_DIR + "/system_trig.png", "system (x <= 0)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/system_log.csv", IMAGE_DIR + "/system_log.png", "system (x > 0)");
            PlotWriter.writePlotFromCsv(OUTPUT_DIR + "/system.csv", IMAGE_DIR + "/system.png", "system(x)");

            System.out.println("CSV файлы успешно созданы:");
            System.out.println("Папка: " + OUTPUT_DIR);
            System.out.println("  - sin.csv, cos.csv, tan.csv, cot.csv, sec.csv");
            System.out.println("  - ln.csv, log2.csv, log5.csv, log10.csv");
            System.out.println("  - system.csv, system_trig.csv, system_log.csv");
            System.out.println("PNG графики успешно созданы:");
            System.out.println("Папка: " + IMAGE_DIR);
            System.out.println("  - sin.png, cos.png, tan.png, cot.png, sec.png");
            System.out.println("  - ln.png, log2.png, log5.png, log10.png");
            System.out.println("  - system.png, system_trig.png, system_log.png");

        } catch (IOException e) {
            System.err.println("Ошибка при записи CSV: " + e.getMessage());
        }
    }
}
