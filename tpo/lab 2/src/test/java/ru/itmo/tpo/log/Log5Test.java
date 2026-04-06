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

class Log5Test {
    private Log5 log5;

    @BeforeEach
    void setUp() {
        log5 = new Log5(new Ln(1e-7));
    }

    @ParameterizedTest
    @MethodSource("log5Cases")
    void testLog5FromCsv(double x, double expected) {
        double actual = log5.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> log5Cases() {
        return TestCsvData.fromResource("testdata/log5_cases.csv");
    }
}
