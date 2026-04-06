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

class Log2Test {
    private Log2 log2;

    @BeforeEach
    void setUp() {
        log2 = new Log2(new Ln(1e-7));
    }

    @ParameterizedTest
    @MethodSource("log2Cases")
    void testLog2FromCsv(double x, double expected) {
        double actual = log2.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> log2Cases() {
        return TestCsvData.fromResource("testdata/log2_cases.csv");
    }
}
