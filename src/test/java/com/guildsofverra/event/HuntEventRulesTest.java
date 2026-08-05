package com.guildsofverra.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HuntEventRulesTest {
    @Test
    void requiresAnEligiblePlayerCooldownAndFreeGlobalSlot() {
        assertTrue(HuntEventRules.eligible(
            30, 15, true, false, false, false, false,
            5_000L, 4_000L, 1, 2
        ));
        assertFalse(HuntEventRules.eligible(
            14, 15, true, false, false, false, false,
            5_000L, 4_000L, 1, 2
        ));
        assertFalse(HuntEventRules.eligible(
            30, 15, true, false, false, false, false,
            3_000L, 4_000L, 1, 2
        ));
        assertFalse(HuntEventRules.eligible(
            30, 15, true, false, false, false, true,
            5_000L, 4_000L, 1, 2
        ));
        assertFalse(HuntEventRules.eligible(
            30, 15, true, false, false, false, false,
            5_000L, 4_000L, 2, 2
        ));
    }

    @Test
    void excludesSpectatorsAndCreativePlayersByDefault() {
        assertFalse(HuntEventRules.eligible(
            30, 15, true, true, false, false, false,
            5_000L, 0L, 0, 2
        ));
        assertFalse(HuntEventRules.eligible(
            30, 15, true, false, true, false, false,
            5_000L, 0L, 0, 2
        ));
        assertTrue(HuntEventRules.eligible(
            30, 15, true, false, true, true, false,
            5_000L, 0L, 0, 2
        ));
    }

    @Test
    void scalesPackSizeWithoutExceedingTheConfiguredMaximum() {
        assertEquals(5, HuntEventRules.packSize(15, 15, 5, 15, 10));
        assertEquals(6, HuntEventRules.packSize(30, 15, 5, 15, 10));
        assertEquals(10, HuntEventRules.packSize(100, 15, 5, 15, 10));
    }

    @Test
    void usesStrictProbabilityAndStableTickConversions() {
        assertTrue(HuntEventRules.shouldTrigger(0.0149, 0.015));
        assertFalse(HuntEventRules.shouldTrigger(0.015, 0.015));
        assertEquals(54_000L, HuntEventRules.cooldownTicks(45));
        assertEquals(6_000L, HuntEventRules.durationTicks(300));
    }
}
