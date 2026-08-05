package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PassiveRuntimeMathTest {
    @Test
    void appliesPositiveAndNegativeDamageMultipliers() {
        assertEquals(106.0F, PassiveRuntimeMath.multiplyDamage(100.0F, 0.06), 0.0001F);
        assertEquals(80.0F, PassiveRuntimeMath.multiplyDamage(100.0F, -0.20), 0.0001F);
        assertEquals(0.0F, PassiveRuntimeMath.multiplyDamage(100.0F, -2.0), 0.0001F);
    }

    @Test
    void scalesEffectAndUseDurationsWithSafeMinimum() {
        assertEquals(120, PassiveRuntimeMath.scaleDuration(100, 0.20));
        assertEquals(70, PassiveRuntimeMath.scaleDuration(100, -0.30));
        assertEquals(1, PassiveRuntimeMath.scaleDuration(1, -0.99));
    }

    @Test
    void rollsDurabilityPreservationPerPointOfDamage() {
        double[] rolls = {0.01, 0.50, 0.04, 0.90};
        AtomicInteger index = new AtomicInteger();
        assertEquals(2, PassiveRuntimeMath.preservedDurability(
            4,
            0.05,
            () -> rolls[index.getAndIncrement()]
        ));
    }

    @Test
    void convertsWaitReductionToEquivalentExtraTickChance() {
        assertEquals(0.25, PassiveRuntimeMath.extraFishingTickChance(-0.20), 0.000001);
        assertEquals(0.0, PassiveRuntimeMath.extraFishingTickChance(0.10), 0.000001);
    }
}
