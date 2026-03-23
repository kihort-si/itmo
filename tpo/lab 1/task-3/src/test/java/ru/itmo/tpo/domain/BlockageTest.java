package ru.itmo.tpo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BlockageTest {

    @Test
    void shouldDecreaseRemainingMassOnClear() {
        Blockage blockage = new Blockage(BlockageMaterial.RUBBLE, 10.0);

        double removed = blockage.clear(3.0);

        assertEquals(3.0, removed, 1e-9);
        assertEquals(7.0, blockage.remainingMass(), 1e-9);
        assertEquals(0.3, blockage.progress(), 1e-9);
    }

    @Test
    void shouldNotGoBelowZeroRemainingMass() {
        Blockage blockage = new Blockage(BlockageMaterial.RUBBLE, 2.0);

        double removed = blockage.clear(10.0);

        assertEquals(2.0, removed, 1e-9);
        assertEquals(0.0, blockage.remainingMass(), 1e-9);
        assertTrue(blockage.isCleared());
    }

    @Test
    void shouldApplyMaterialResistance() {
        Blockage blockage = new Blockage(BlockageMaterial.ENTRAILS, 10.0);

        double removed = blockage.clear(1.3);

        assertEquals(1.0, removed, 1e-9);
        assertEquals(9.0, blockage.remainingMass(), 1e-9);
    }

    @Test
    void shouldRejectInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new Blockage(null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new Blockage(BlockageMaterial.RUBBLE, 0));

        Blockage blockage = new Blockage(BlockageMaterial.RUBBLE, 1.0);
        assertThrows(IllegalArgumentException.class, () -> blockage.clear(-1.0));
    }
}
