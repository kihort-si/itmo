package ru.itmo.tpo.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.trig.Cos;
import ru.itmo.tpo.trig.Cot;
import ru.itmo.tpo.trig.Sec;
import ru.itmo.tpo.trig.Tan;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    private FunctionSystem system;

    @BeforeEach
    void setUp() {
        system = new FunctionSystem(1e-7);
    }

    @Test
    void testTrigPartUndefinedAtZero() {
        // x = 0: sin(0) = 0, поэтому cot(0) не определён
        assertTrue(Double.isNaN(system.calculate(0.0)));
    }

    @Test
    void testTrigPartUndefinedAtNegativePi() {
        // x = -π: sin(-π) = 0, поэтому cot(-π) не определён
        // Но теперь sin возвращает 0 для этих точек
        double result = system.calculate(-Math.PI);
        assertTrue(Double.isNaN(result) || Math.abs(result) > 1e10);
    }

    @Test
    void testTrigPartUndefinedAtNegative2Pi() {
        // x = -2π: sin(-2π) = 0
        double result = system.calculate(-2 * Math.PI);
        assertTrue(Double.isNaN(result) || Math.abs(result) > 1e10);
    }

    @Test
    void testTrigPartUndefinedAtNegativePi2() {
        // x = -π/2: cos(-π/2) = 0, поэтому sec(-π/2) и tan(-π/2) не определены
        // Также cot(-π/2) = 0, что даёт tan(x) = 0 в знаменателе
        double result = system.calculate(-Math.PI / 2);
        assertTrue(Double.isNaN(result) || Math.abs(result) > 1e10);
    }

    @Test
    void testTrigPartUndefinedAtNegative3Pi2() {
        // x = -3π/2: cos(-3π/2) = 0
        assertTrue(Double.isNaN(system.calculate(-3 * Math.PI / 2)));
    }

    @Test
    void testLogPartUndefinedAtZero() {
        // x = 0: ln(0) не определён
        // Но при x = 0 используется тригонометрическая ветвь
        assertTrue(Double.isNaN(system.calculate(0.0)));
    }

    @Test
    void testLogPartUndefinedAtNegative() {
        // x < 0: логарифмическая часть не используется
        // Тригонометрическая часть может быть определена
        double result = system.calculate(-1.0);
        // -1 не совпадает с точками неопределённости
        assertFalse(Double.isNaN(result));
    }

    @Test
    void testLogPartDefinedAtPositiveValues() {
        // x > 0: все логарифмы определены
        assertFalse(Double.isNaN(system.calculate(0.1)));
        assertFalse(Double.isNaN(system.calculate(1.0)));
        assertFalse(Double.isNaN(system.calculate(2.0)));
        assertFalse(Double.isNaN(system.calculate(10.0)));
    }

    @Test
    void testLogPartAtX1() {
        // x = 1: все логарифмы = 0
        // ((0^2 * 0) * (0 * 0) - (0 + 0)) * 0 = 0
        double result = system.calculate(1.0);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void testTrigPartDefinedAtNonSpecialPoints() {
        // Точки, где функция должна быть определена
        assertFalse(Double.isNaN(system.calculate(-Math.PI / 4)));
        assertFalse(Double.isNaN(system.calculate(-Math.PI / 3)));
        assertFalse(Double.isNaN(system.calculate(-Math.PI / 6)));
        assertFalse(Double.isNaN(system.calculate(-0.5)));
        assertFalse(Double.isNaN(system.calculate(-1.0)));
    }

    @Test
    void testDomainBoundaryAtZero() {
        // x = 0 - граница между ветвями
        // Тригонометрическая ветвь используется при x <= 0
        // При x = 0 функция не определена из-за cot(0)
        assertTrue(Double.isNaN(system.calculate(0.0)));
    }

    @Test
    void testContinuityAtDomain() {
        // Проверяем непрерывность в точках, где функция определена
        double x1 = -Math.PI / 4;
        double x2 = -Math.PI / 4 + 0.001;
        double x3 = -Math.PI / 4 - 0.001;

        double y1 = system.calculate(x1);
        double y2 = system.calculate(x2);
        double y3 = system.calculate(x3);

        assertTrue(Math.abs(y2 - y1) < 0.1);
        assertTrue(Math.abs(y3 - y1) < 0.1);
    }
}
