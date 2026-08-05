package com.guildsofverra.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorldEventRulesTest {
    @Test
    void validatesPlayerEligibility() {
        assertTrue(WorldEventRules.eligiblePlayer(10, 10, true, false, false, false));
        assertFalse(WorldEventRules.eligiblePlayer(9, 10, true, false, false, false));
        assertFalse(WorldEventRules.eligiblePlayer(10, 10, false, false, false, false));
        assertFalse(WorldEventRules.eligiblePlayer(10, 10, true, true, false, false));
        assertFalse(WorldEventRules.eligiblePlayer(10, 10, true, false, true, false));
        assertTrue(WorldEventRules.eligiblePlayer(10, 10, true, false, true, true));
    }

    @Test
    void schedulingRespectsCooldownAndProbability() {
        assertTrue(WorldEventRules.shouldSchedule(0.01, 0.03, 1_000, 900, false));
        assertFalse(WorldEventRules.shouldSchedule(0.03, 0.03, 1_000, 900, false));
        assertFalse(WorldEventRules.shouldSchedule(0.01, 0.03, 899, 900, false));
        assertFalse(WorldEventRules.shouldSchedule(0.01, 0.03, 1_000, 900, true));
    }

    @Test
    void weightedSelectionSkipsDisabledEntries() {
        List<WorldEventType> candidates = List.of(
            WorldEventType.BLOOD_MOON,
            WorldEventType.CAVE_TREMOR,
            WorldEventType.LONG_NIGHT
        );

        assertEquals(
            WorldEventType.BLOOD_MOON,
            WorldEventRules.pickWeighted(candidates, type ->
                type == WorldEventType.CAVE_TREMOR ? 0 : 2, 0)
        );
        assertEquals(
            WorldEventType.LONG_NIGHT,
            WorldEventRules.pickWeighted(candidates, type ->
                type == WorldEventType.CAVE_TREMOR ? 0 : 2, 3)
        );
        assertNull(WorldEventRules.pickWeighted(candidates, type -> 0, 0));
    }

    @Test
    void capsSpawnCountsWithoutGoingNegative() {
        assertEquals(6, WorldEventRules.cappedSpawnCount(6, 0, 16));
        assertEquals(2, WorldEventRules.cappedSpawnCount(6, 14, 16));
        assertEquals(0, WorldEventRules.cappedSpawnCount(6, 20, 16));
        assertEquals(0, WorldEventRules.cappedSpawnCount(-3, 0, 16));
    }

    @Test
    void convertsDurationsToServerTicks() {
        assertEquals(600L, WorldEventRules.durationTicks(1));
        assertEquals(72_000L, WorldEventRules.cooldownTicks(60));
    }

    @Test
    void resolvesCommandFacingEventIds() {
        assertEquals(WorldEventType.BLOOD_MOON, WorldEventType.byId("blood_moon"));
        assertEquals(
            WorldEventType.SEVERE_THUNDERSTORM,
            WorldEventType.byId("SEVERE_THUNDERSTORM")
        );
        assertNull(WorldEventType.byId("not_an_event"));
        assertNull(WorldEventType.byId(null));
    }
}
