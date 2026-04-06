package ru.itmo.tpo.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.util.TestCsvData;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Log10Test {
    private Log10 log10;

    @BeforeEach
    void setUp() {
        log10 = new Log10(new Ln(1e-7));
    }

    @ParameterizedTest
    @MethodSource("log10Cases")
    void testLog10FromCsv(double x, double expected) {
        double actual = log10.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> log10Cases() {
        return TestCsvData.fromResource("testdata/log10_cases.csv");
    }
}
