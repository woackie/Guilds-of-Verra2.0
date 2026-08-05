package com.guildsofverra.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ExpectedDimensionReturnTest {
    @Test
    void matchesOnlyTheQueuedReturnDestination() {
        ExpectedDimensionReturn<String> guard =
            new ExpectedDimensionReturn<>("overworld", 6_000L);

        assertTrue(guard.matches("overworld", 2_000L));
        assertFalse(guard.matches("the_end", 2_000L));
    }

    @Test
    void expiresAtTheDeadline() {
        ExpectedDimensionReturn<String> guard =
            new ExpectedDimensionReturn<>("overworld", 6_000L);

        assertFalse(guard.isExpired(5_999L));
        assertTrue(guard.isExpired(6_000L));
        assertFalse(guard.matches("overworld", 6_000L));
    }
}
