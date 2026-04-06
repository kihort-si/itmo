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

class SecTest {
    private Sec sec;

    @BeforeEach
    void setUp() {
        Cos cos = new Cos(new Sin(1e-7));
        sec = new Sec(cos);
    }

    @ParameterizedTest
    @MethodSource("secCases")
    void testSecFromCsv(double x, double expected) {
        double actual = sec.calculate(x);
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual, 1e-5);
        }
    }

    static Stream<Arguments> secCases() {
        return TestCsvData.fromResource("testdata/sec_cases.csv");
    }
}
