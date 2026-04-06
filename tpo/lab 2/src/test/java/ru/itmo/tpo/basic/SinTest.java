package ru.itmo.tpo.basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import ru.itmo.tpo.util.TestCsvData;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SinTest {
    private Sin sin;
    private static final double EPSILON = 1e-7;
    private static final double DELTA = 1e-5;

    @BeforeEach
    void setUp() {
        sin = new Sin(EPSILON);
    }

    @ParameterizedTest
    @MethodSource("sinCases")
    void testSin(double x, double expected) {
        double result = sin.calculate(x);
        assertEquals(expected, result, DELTA);
    }

    static Stream<Arguments> sinCases() {
        return TestCsvData.fromResource("testdata/sin_cases.csv");
    }

    @Test
    void testSinPeriodicity() {
        assertEquals(sin.calculate(0.5), sin.calculate(0.5 + 2 * Math.PI), DELTA);
        assertEquals(sin.calculate(1.0), sin.calculate(1.0 - 2 * Math.PI), DELTA);
    }

    @Test
    void testSinOdd() {
        assertEquals(-sin.calculate(1.5), sin.calculate(-1.5), DELTA);
    }
}
