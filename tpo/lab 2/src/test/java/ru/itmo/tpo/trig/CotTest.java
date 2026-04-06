package ru.itmo.tpo.trig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.util.TestCsvData;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CotTest {
    private Cot cot;

    @BeforeEach
    void setUp() {
        Sin sin = new Sin(1e-7);
        Cos cos = new Cos(sin);
        cot = new Cot(sin, cos);
    }

    @ParameterizedTest
    @MethodSource("cotCases")
    void testCotFromCsv(double x, double expected) {
        double actual = cot.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> cotCases() {
        return TestCsvData.fromResource("testdata/cot_cases.csv");
    }
}
