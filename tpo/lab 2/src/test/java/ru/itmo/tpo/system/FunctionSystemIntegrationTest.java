package ru.itmo.tpo.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.log.Log2;
import ru.itmo.tpo.log.Log5;
import ru.itmo.tpo.log.Log10;
import ru.itmo.tpo.trig.Cos;
import ru.itmo.tpo.trig.Cot;
import ru.itmo.tpo.trig.Sec;
import ru.itmo.tpo.trig.Tan;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class FunctionSystemIntegrationTest {

    private Sin sinStub;
    private Cos cosStub;
    private Tan tanStub;
    private Cot cotStub;
    private Sec secStub;

    private Ln lnStub;
    private Log2 log2Stub;
    private Log5 log5Stub;
    private Log10 log10Stub;

    private Map<Double, Double> sinTable;
    private Map<Double, Double> cosTable;
    private Map<Double, Double> tanTable;
    private Map<Double, Double> cotTable;
    private Map<Double, Double> secTable;
    private Map<Double, Double> lnTable;
    private Map<Double, Double> log2Table;
    private Map<Double, Double> log5Table;
    private Map<Double, Double> log10Table;

    @BeforeEach
    void setUp() {
        sinStub = mock(Sin.class);
        cosStub = mock(Cos.class);
        tanStub = mock(Tan.class);
        cotStub = mock(Cot.class);
        secStub = mock(Sec.class);
        lnStub = mock(Ln.class);
        log2Stub = mock(Log2.class);
        log5Stub = mock(Log5.class);
        log10Stub = mock(Log10.class);

        setupTrigTables();
        setupLogTables();

        sinTable.forEach((x, y) -> when(sinStub.calculate(x)).thenReturn(y));
        cosTable.forEach((x, y) -> when(cosStub.calculate(x)).thenReturn(y));
        tanTable.forEach((x, y) -> when(tanStub.calculate(x)).thenReturn(y));
        cotTable.forEach((x, y) -> when(cotStub.calculate(x)).thenReturn(y));
        secTable.forEach((x, y) -> when(secStub.calculate(x)).thenReturn(y));

        lnTable.forEach((x, y) -> when(lnStub.calculate(x)).thenReturn(y));
        log2Table.forEach((x, y) -> when(log2Stub.calculate(x)).thenReturn(y));
        log5Table.forEach((x, y) -> when(log5Stub.calculate(x)).thenReturn(y));
        log10Table.forEach((x, y) -> when(log10Stub.calculate(x)).thenReturn(y));
    }

    private void setupTrigTables() {
        sinTable = new HashMap<>();
        cosTable = new HashMap<>();
        tanTable = new HashMap<>();
        cotTable = new HashMap<>();
        secTable = new HashMap<>();

        // x = -π/2: sin(-π/2) = -1, cos(-π/2) = 0, tan не определён
        double nPi2 = -Math.PI / 2;
        sinTable.put(nPi2, -1.0);
        cosTable.put(nPi2, 0.0);
        tanTable.put(nPi2, Double.NaN);
        cotTable.put(nPi2, 0.0);
        secTable.put(nPi2, Double.NaN);

        // x = -π/4
        double nPi4 = -Math.PI / 4;
        sinTable.put(nPi4, -0.707106781);
        cosTable.put(nPi4, 0.707106781);
        tanTable.put(nPi4, -1.0);
        cotTable.put(nPi4, -1.0);
        secTable.put(nPi4, 1.414213562);

        // x = -π/6
        double nPi6 = -Math.PI / 6;
        sinTable.put(nPi6, -0.5);
        cosTable.put(nPi6, 0.866025404);
        tanTable.put(nPi6, -0.577350269);
        cotTable.put(nPi6, -1.732050808);
        secTable.put(nPi6, 1.154700538);

        // x = -π/3
        double nPi3 = -Math.PI / 3;
        sinTable.put(nPi3, -0.866025404);
        cosTable.put(nPi3, 0.5);
        tanTable.put(nPi3, -1.732050808);
        cotTable.put(nPi3, -0.577350269);
        secTable.put(nPi3, 2.0);

        // x = 0
        sinTable.put(0.0, 0.0);
        cosTable.put(0.0, 1.0);
        tanTable.put(0.0, 0.0);
        cotTable.put(0.0, Double.NaN);
        secTable.put(0.0, 1.0);
    }

    private void setupLogTables() {
        lnTable = new HashMap<>();
        log2Table = new HashMap<>();
        log5Table = new HashMap<>();
        log10Table = new HashMap<>();

        // x = 0.1: ln(0.1) = -2.302585
        double x1 = 0.1;
        lnTable.put(x1, -2.302585093);
        log2Table.put(x1, -3.321928095);
        log5Table.put(x1, -1.430676558);
        log10Table.put(x1, -1.0);

        // x = 0.5: ln(0.5) = -0.693147
        double x2 = 0.5;
        lnTable.put(x2, -0.693147181);
        log2Table.put(x2, -1.0);
        log5Table.put(x2, -0.430676558);
        log10Table.put(x2, -0.301029996);

        // x = 1: все логарифмы = 0
        double x3 = 1.0;
        lnTable.put(x3, 0.0);
        log2Table.put(x3, 0.0);
        log5Table.put(x3, 0.0);
        log10Table.put(x3, 0.0);

        // x = 2: ln(2) = 0.693147
        double x4 = 2.0;
        lnTable.put(x4, 0.693147181);
        log2Table.put(x4, 1.0);
        log5Table.put(x4, 0.430676558);
        log10Table.put(x4, 0.301029996);

        // x = 5: ln(5) = 1.609438
        double x5 = 5.0;
        lnTable.put(x5, 1.609437912);
        log2Table.put(x5, 2.321928095);
        log5Table.put(x5, 1.0);
        log10Table.put(x5, 0.698970004);

        // x = 10: ln(10) = 2.302585
        double x6 = 10.0;
        lnTable.put(x6, 2.302585093);
        log2Table.put(x6, 3.321928095);
        log5Table.put(x6, 1.430676558);
        log10Table.put(x6, 1.0);
    }

    @Test
    void testTrigSystemIntegrationAtNegativePi4() {
        // Тестируем систему при x = -π/4 с заглушками
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        double x = -Math.PI / 4;

        double cotX = cotStub.calculate(x);
        double secX = secStub.calculate(x);
        double tanX = tanStub.calculate(x);

        double numerator = cotX * cotX - 2 * secX;
        double denominator = (secX + tanX) * tanX;
        double expected = numerator / denominator + cotX;

        double result = system.calculateTrig(x);

        assertEquals(expected, result, 1e-6);

        verify(cotStub, times(2)).calculate(x);
        verify(secStub, times(2)).calculate(x);
        verify(tanStub, times(2)).calculate(x);
    }

    @Test
    void testTrigSystemIntegrationAtNegativePi3() {
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        double x = -Math.PI / 3;

        double cotX = cotStub.calculate(x);
        double secX = secStub.calculate(x);
        double tanX = tanStub.calculate(x);

        double numerator = cotX * cotX - 2 * secX;
        double denominator = (secX + tanX) * tanX;
        double expected = numerator / denominator + cotX;

        double result = system.calculateTrig(x);

        assertEquals(expected, result, 1e-6);
    }

    @Test
    void testTrigSystemUndefinedAtZero() {
        // При x = 0 cot(0) не определён
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        when(cotStub.calculate(0.0)).thenReturn(Double.NaN);

        double result = system.calculateTrig(0.0);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testLogSystemIntegrationAtX2() {
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        double x = 2.0;

        double log2X = log2Stub.calculate(x);
        double log5X = log5Stub.calculate(x);
        double log10X = log10Stub.calculate(x);
        double lnX = lnStub.calculate(x);

        double part1 = log2X * log2X * log5X;
        double part2 = log5X * lnX;
        double part3 = 2 * log10X;
        double expected = (part1 * part2 - part3) * lnX;

        double result = system.calculateLog(x);

        assertEquals(expected, result, 1e-6);

        verify(log2Stub, times(2)).calculate(x);
        verify(log5Stub, times(2)).calculate(x);
        verify(log10Stub, times(2)).calculate(x);
        verify(lnStub, times(2)).calculate(x);
    }

    @Test
    void testLogSystemIntegrationAtX5() {
        // Тестируем логарифмическую часть при x = 5
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        double x = 5.0;

        double log2X = log2Stub.calculate(x);
        double log5X = log5Stub.calculate(x);
        double log10X = log10Stub.calculate(x);
        double lnX = lnStub.calculate(x);

        double part1 = log2X * log2X * log5X;
        double part2 = log5X * lnX;
        double part3 = 2 * log10X;
        double expected = (part1 * part2 - part3) * lnX;

        double result = system.calculateLog(x);

        assertEquals(expected, result, 1e-6);
    }

    @Test
    void testLogSystemAtX1() {
        // При x = 1 все логарифмы = 0, результат должен быть 0
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        double x = 1.0;
        double result = system.calculateLog(x);

        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void testLogSystemUndefinedForX0() {
        // При x = 0 ln(0) не определён
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        when(lnStub.calculate(0.0)).thenReturn(Double.NaN);

        double result = system.calculateLog(0.0);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testFullSystemSwitchesBranch() {
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        // При x = -π/4 должна использоваться тригонометрическая ветвь
        double trigResult = system.calculate(-Math.PI / 4);
        assertFalse(Double.isNaN(trigResult));

        // При x = 2 должна использоваться логарифмическая ветвь
        double logResult = system.calculate(2.0);
        assertFalse(Double.isNaN(logResult));
    }

    @Test
    void testDomainX0() {
        // x = 0 - граничная точка между ветвями
        // При x <= 0 должна использоваться тригонометрическая ветвь
        FunctionSystem system = new FunctionSystem(sinStub, cosStub, tanStub, cotStub, secStub,
                lnStub, log2Stub, log5Stub, log10Stub);

        when(cotStub.calculate(0.0)).thenReturn(Double.NaN);
        when(secStub.calculate(0.0)).thenReturn(1.0);
        when(tanStub.calculate(0.0)).thenReturn(0.0);

        double result = system.calculate(0.0);

        // При x=0: cot(0) не определён
        assertTrue(Double.isNaN(result));
    }
}
