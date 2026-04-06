package ru.itmo.tpo.basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import ru.itmo.tpo.util.TestCsvData;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LnTest {
    private Ln ln;
    private static final double EPSILON = 1e-7;
    private static final double DELTA = 1e-5;

    @BeforeEach
    void setUp() {
        ln = new Ln(EPSILON);
    }

    @ParameterizedTest
    @MethodSource("lnCases")
    void testLn(double x, double expected) {
        double result = ln.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(result));
        } else {
            assertEquals(expected, result, DELTA);
        }
    }

    static Stream<Arguments> lnCases() {
        return TestCsvData.fromResource("testdata/ln_cases.csv");
    }

    @Test
    void testLnProduct() {
        double a = 2, b = 3;
        assertEquals(ln.calculate(a) + ln.calculate(b), ln.calculate(a * b), DELTA);
    }
}
