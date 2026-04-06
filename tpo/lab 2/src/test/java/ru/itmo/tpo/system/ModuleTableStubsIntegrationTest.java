package ru.itmo.tpo.system;

import org.junit.jupiter.api.Test;
import ru.itmo.tpo.basic.Ln;
import ru.itmo.tpo.basic.Sin;
import ru.itmo.tpo.log.Log10;
import ru.itmo.tpo.log.Log2;
import ru.itmo.tpo.log.Log5;
import ru.itmo.tpo.trig.Cos;
import ru.itmo.tpo.trig.Cot;
import ru.itmo.tpo.trig.Sec;
import ru.itmo.tpo.trig.Tan;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModuleTableStubsIntegrationTest {

    @Test
    void testCosWithSinTableStub() {
        Sin sinStub = mock(Sin.class);
        Map<Double, Double> sinTable = Map.of(
                Math.PI / 2, 1.0,
                Math.PI / 4, 0.707106781
        );
        sinTable.forEach((x, y) -> when(sinStub.calculate(x)).thenReturn(y));

        Cos cos = new Cos(sinStub);
        assertEquals(1.0, cos.calculate(0.0), 1e-6);
        assertEquals(0.707106781, cos.calculate(Math.PI / 4), 1e-6);
    }

    @Test
    void testTanWithTableStubs() {
        Sin sinStub = mock(Sin.class);
        Cos cosStub = mock(Cos.class);

        double x = Math.PI / 4;
        when(sinStub.calculate(x)).thenReturn(0.707106781);
        when(cosStub.calculate(x)).thenReturn(0.707106781);

        Tan tan = new Tan(sinStub, cosStub);
        assertEquals(1.0, tan.calculate(x), 1e-6);
    }

    @Test
    void testCotWithTableStubs() {
        Sin sinStub = mock(Sin.class);
        Cos cosStub = mock(Cos.class);

        double x = Math.PI / 6;
        when(sinStub.calculate(x)).thenReturn(0.5);
        when(cosStub.calculate(x)).thenReturn(0.866025404);

        Cot cot = new Cot(sinStub, cosStub);
        assertEquals(1.732050808, cot.calculate(x), 1e-6);
    }

    @Test
    void testSecWithTableStubs() {
        Cos cosStub = mock(Cos.class);
        double x = Math.PI / 3;
        when(cosStub.calculate(x)).thenReturn(0.5);

        Sec sec = new Sec(cosStub);
        assertEquals(2.0, sec.calculate(x), 1e-6);
    }

    @Test
    void testLog2WithLnTableStub() {
        Ln lnStub = mock(Ln.class);
        when(lnStub.calculate(8.0)).thenReturn(Math.log(8.0));
        when(lnStub.calculate(2.0)).thenReturn(Math.log(2.0));

        Log2 log2 = new Log2(lnStub);
        assertEquals(3.0, log2.calculate(8.0), 1e-6);
    }

    @Test
    void testLog5WithLnTableStub() {
        Ln lnStub = mock(Ln.class);
        when(lnStub.calculate(25.0)).thenReturn(Math.log(25.0));
        when(lnStub.calculate(5.0)).thenReturn(Math.log(5.0));

        Log5 log5 = new Log5(lnStub);
        assertEquals(2.0, log5.calculate(25.0), 1e-6);
    }

    @Test
    void testLog10WithLnTableStub() {
        Ln lnStub = mock(Ln.class);
        when(lnStub.calculate(1000.0)).thenReturn(Math.log(1000.0));
        when(lnStub.calculate(10.0)).thenReturn(Math.log(10.0));

        Log10 log10 = new Log10(lnStub);
        assertEquals(3.0, log10.calculate(1000.0), 1e-6);
    }

    @Test
    void testModuleDomainPointsViaTableStubs() {
        Sin sinStub = mock(Sin.class);
        Cos cosStub = mock(Cos.class);
        Ln lnStub = mock(Ln.class);

        when(sinStub.calculate(0.0)).thenReturn(0.0);
        when(cosStub.calculate(Math.PI / 2)).thenReturn(0.0);
        when(lnStub.calculate(0.0)).thenReturn(Double.NaN);

        Cot cot = new Cot(sinStub, cosStub);
        Sec sec = new Sec(cosStub);
        Log2 log2 = new Log2(lnStub);

        assertTrue(Double.isNaN(cot.calculate(0.0)));
        assertTrue(Double.isNaN(sec.calculate(Math.PI / 2)));
        assertTrue(Double.isNaN(log2.calculate(0.0)));
    }
}
