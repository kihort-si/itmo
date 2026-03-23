package ru.itmo.tpo.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculation.arcsin — степенной ряд")
class CalculationTest {

    private static final double EPS = 1e-10;
    private static final int MAX_ITER = 1_000_000;
    private static final double TOLERANCE = 1e-9;

    @Nested
    @DisplayName("1. Специальные и граничные значения")
    class SpecialValues {

        @ParameterizedTest(name = "{2}: arcsin({0}) = {1}")
        @CsvFileSource(resources = "/arcsin-special-values.csv", numLinesToSkip = 1)
        void specialValues(double x, double expected, double tolerance, String description) {
            assertEquals(expected, Calculation.arcsin(x, EPS, MAX_ITER), tolerance, description);
        }

        @Test
        @DisplayName("arcsin(NaN) = NaN")
        void arcsinNaN() {
            assertTrue(Double.isNaN(Calculation.arcsin(Double.NaN, EPS, MAX_ITER)));
        }
    }

    @Nested
    @DisplayName("2. Табличные значения")
    class TableValues {
        @ParameterizedTest(name = "{3}: arcsin({0}) = {1}")
        @CsvFileSource(resources = "/arcsin-table-values.csv", numLinesToSkip = 1)
        void tableValues(double x, double expected, double tolerance, String description) {
            assertEquals(expected, Calculation.arcsin(x, EPS, MAX_ITER), tolerance, description);
        }
    }

    @Nested
    @DisplayName("3. Симметрия (нечётность)")
    class Symmetry {

        @ParameterizedTest(name = "arcsin({0}) = {1}, arcsin(-{0}) = {2}")
        @CsvFileSource(resources = "/arcsin-symmetry.csv", numLinesToSkip = 1)
        void oddFunction(double x, double expectedPositive, double expectedNegative, double tolerance, String description) {
            double pos = Calculation.arcsin(x, EPS, MAX_ITER);
            double neg = Calculation.arcsin(-x, EPS, MAX_ITER);
            assertAll(description,
                    () -> assertEquals(expectedPositive, pos, tolerance,
                            "arcsin(x) не совпадает с ожидаемым значением для x=" + x),
                    () -> assertEquals(expectedNegative, neg, tolerance,
                            "arcsin(-x) не совпадает с ожидаемым значением для x=" + x),
                    () -> assertEquals(-pos, neg, TOLERANCE,
                            "arcsin должна быть нечётной функцией для x=" + x)
            );
        }
    }

    @Nested
    @DisplayName("4. Зависимость точности от eps")
    class EpsPrecision {

        @ParameterizedTest(name = "x={0}, eps={1}, expected={2}")
        @CsvFileSource(resources = "/arcsin-eps-precision.csv", numLinesToSkip = 1)
        void epsPrecisionVaries(double x, double eps, double expected, double tolerance, String description) {
            double result = Calculation.arcsin(x, eps, MAX_ITER);
            assertEquals(expected, result, tolerance, description);
        }
    }

    @Nested
    @DisplayName("5. Область определения")
    class Domain {

        @ParameterizedTest(name = "x={0} -> {1}")
        @CsvFileSource(resources = "/arcsin-domain-invalid.csv", numLinesToSkip = 1)
        void outsideDomainThrows(double x, String expectedException, String description) {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(x, EPS, MAX_ITER),
                    "Ожидается " + expectedException + " для " + description);
        }

        @Test
        @DisplayName("arcsin(+Infinity) выбрасывает исключение")
        void positiveInfinity() {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(Double.POSITIVE_INFINITY, EPS, MAX_ITER));
        }

        @Test
        @DisplayName("arcsin(-Infinity) выбрасывает исключение")
        void negativeInfinity() {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(Double.NEGATIVE_INFINITY, EPS, MAX_ITER));
        }
    }

    @Nested
    @DisplayName("6. Валидация параметров")
    class ParameterValidation {

        @ParameterizedTest
        @CsvSource({
                "0.5, 0.0,                 0",
                "0.5, -1e-5,               1000000",
                "0.5, Infinity,            1000000"
        })
        @DisplayName("Невалидные eps выбрасывают IllegalArgumentException")
        void invalidEpsThrows(double x, String epsStr, int maxIter) {
            double eps = epsStr.equals("Infinity") ? Double.POSITIVE_INFINITY : Double.parseDouble(epsStr);
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(x, eps, maxIter));
        }

        @Test
        @DisplayName("eps=NaN выбрасывает IllegalArgumentException")
        void epsNaN() {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(0.5, Double.NaN, MAX_ITER));
        }

        @ParameterizedTest
        @CsvSource({
                "0.5, 1e-10, 0",
                "0.5, 1e-10, -1"
        })
        @DisplayName("Невалидные maxIter выбрасывают IllegalArgumentException")
        void invalidIterThrows(double x, double eps, int maxIter) {
            assertThrows(IllegalArgumentException.class,
                    () -> Calculation.arcsin(x, eps, maxIter));
        }
    }

    @Nested
    @DisplayName("7. Расходящийся ряд (maxIter слишком мал)")
    class Convergence {

        @Test
        @DisplayName("Для x=0.9 и maxIter=1 — ArithmeticException (ряд не успевает сойтись)")
        void tooFewIterations() {
            assertThrows(ArithmeticException.class,
                    () -> Calculation.arcsin(0.9, 1e-10, 1));
        }

        @Test
        @DisplayName("Для x=0.5 достаточно 50 итераций (ряд хорошо сходится)")
        void sufficientIterations() {
            assertDoesNotThrow(() -> Calculation.arcsin(0.5, 1e-10, 50));
        }
    }

    @Nested
    @DisplayName("8. Редукция аргумента (|x| > 1/sqrt2 = 0.7071)")
    class ArgumentReduction {

        @ParameterizedTest(name = "arcsin({0}) = {1} через редукцию")
        @CsvFileSource(resources = "/arcsin-argument-reduction.csv", numLinesToSkip = 1)
        void reducedArgumentMatchesMathAsin(double x, double expected, double tolerance, String description) {
            double result = Calculation.arcsin(x, EPS, MAX_ITER);
            assertEquals(expected, result, tolerance,
                    "arcsin через редукцию не совпадает с Math.asin для x=" + x);
        }
    }

    @Nested
    @DisplayName("9. Тождество: arcsin(x) + arcsin(sqrt(1-x²)) = pi/2 для x (0,1)")
    class IdentityTest {

        @ParameterizedTest(name = "x={0}, expected sum={1}")
        @CsvFileSource(resources = "/arcsin-identity.csv", numLinesToSkip = 1)
        void complementaryIdentity(double x, double expected, double tolerance, String description) {
            double arcsinX = Calculation.arcsin(x, EPS, MAX_ITER);
            double sqrtTerm = Math.sqrt(1.0 - x * x);
            double arcsinComp = Calculation.arcsin(sqrtTerm, EPS, MAX_ITER);
            assertEquals(expected, arcsinX + arcsinComp, tolerance,
                    "Нарушено тождество arcsin(x) + arcsin(sqrt(1-x^2)) = pi/2 для x=" + x);
        }
    }

    @Nested
    @DisplayName("10. Сравнение с Math.asin по всему диапазону [-1, 1]")
    class CompareWithMathAsin {

        @ParameterizedTest(name = "x={0}, expected={1}")
        @CsvFileSource(resources = "/arcsin-uniform-grid.csv", numLinesToSkip = 1)
        void uniformGrid(double x, double expected, double tolerance, String description) {
            double result = Calculation.arcsin(x, EPS, MAX_ITER);
            assertEquals(expected, result, tolerance,
                    description + " при x=" + x);
        }
    }
}
