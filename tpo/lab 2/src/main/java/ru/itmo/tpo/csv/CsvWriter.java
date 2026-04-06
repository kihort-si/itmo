package ru.itmo.tpo.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Function;

public class CsvWriter {
    public static void write(String filename, double start, double end, double step,
                             Function<Double, Double> function, String moduleName) throws IOException {
        Path outputPath = Path.of(filename);
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("X;" + moduleName + "(X)");

            for (double x = start; x <= end; x += step) {
                double result = function.apply(x);
                if (Double.isNaN(result)) {
                    writer.printf(Locale.US, "%.10f;undefined%n", x);
                } else if (Double.isInfinite(result)) {
                    writer.printf(Locale.US, "%.10f;infinity%n", x);
                } else {
                    writer.printf(Locale.US, "%.10f;%.10f%n", x, result);
                }
            }
        }
    }
}
