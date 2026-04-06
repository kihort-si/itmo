package ru.itmo.tpo.plot;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlotWriter {
    private static final double Y_ABS_LIMIT = 250.0;
    private static final double Y_JUMP_LIMIT = 30.0;

    public static void writePlotFromCsv(String csvPath, String pngPath, String title) throws IOException {
        List<List<Double>> xSegments = new ArrayList<>();
        List<List<Double>> ySegments = new ArrayList<>();
        List<Double> currentX = new ArrayList<>();
        List<Double> currentY = new ArrayList<>();
        xSegments.add(currentX);
        ySegments.add(currentY);

        Double prevX = null;
        Double prevY = null;

        try (BufferedReader reader = Files.newBufferedReader(Path.of(csvPath))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length < 2) {
                    continue;
                }

                String xToken = parts[0].trim().replace(',', '.');
                String yToken = parts[1].trim().replace(',', '.');

                if ("undefined".equalsIgnoreCase(yToken) || "infinity".equalsIgnoreCase(yToken)) {
                    continue;
                }

                double x = Double.parseDouble(xToken);
                double y = Double.parseDouble(yToken);
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    prevX = null;
                    prevY = null;
                    if (!currentX.isEmpty()) {
                        currentX = new ArrayList<>();
                        currentY = new ArrayList<>();
                        xSegments.add(currentX);
                        ySegments.add(currentY);
                    }
                    continue;
                }

                if (Math.abs(y) > Y_ABS_LIMIT) {
                    prevX = null;
                    prevY = null;
                    if (!currentX.isEmpty()) {
                        currentX = new ArrayList<>();
                        currentY = new ArrayList<>();
                        xSegments.add(currentX);
                        ySegments.add(currentY);
                    }
                    continue;
                }

                if (prevX != null && prevY != null) {
                    double dx = Math.abs(x - prevX);
                    double dy = Math.abs(y - prevY);
                    if (dx > 0.03 || dy > Y_JUMP_LIMIT) {
                        currentX = new ArrayList<>();
                        currentY = new ArrayList<>();
                        xSegments.add(currentX);
                        ySegments.add(currentY);
                    }
                }

                currentX.add(x);
                currentY.add(y);
                prevX = x;
                prevY = y;
            }
        }

        XYChart chart = new XYChartBuilder()
                .width(1280)
                .height(720)
                .title(title)
                .xAxisTitle("X")
                .yAxisTitle("Y")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(2);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotMargin(20);

        int seriesIndex = 0;
        for (int i = 0; i < xSegments.size(); i++) {
            List<Double> xs = xSegments.get(i);
            List<Double> ys = ySegments.get(i);
            if (xs.size() < 2) {
                continue;
            }
            chart.addSeries(title + "_" + seriesIndex, xs, ys);
            seriesIndex++;
        }

        Path outputPath = Path.of(pngPath);
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String outputWithoutExtension = pngPath.endsWith(".png")
                ? pngPath.substring(0, pngPath.length() - 4)
                : pngPath;

        BitmapEncoder.saveBitmap(chart, outputWithoutExtension, BitmapEncoder.BitmapFormat.PNG);
    }
}
