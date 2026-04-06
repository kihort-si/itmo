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

class CosTest {
    private Cos cos;

    @BeforeEach
    void setUp() {
        cos = new Cos(new Sin(1e-7));
    }

    @ParameterizedTest
    @MethodSource("cosCases")
    void testCosFromCsv(double x, double expected) {
        double actual = cos.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> cosCases() {
        return TestCsvData.fromResource("testdata/cos_cases.csv");
    }
}
