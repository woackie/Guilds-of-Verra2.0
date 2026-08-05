package com.guildsofverra.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorldEventConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void defaultsKeepMajorEventsSeparatedByOneHour() {
        WorldEventConfig config = WorldEventConfig.defaults();
        assertEquals(30, config.initialDelayMinutes);
        assertEquals(60, config.minimumMinutesBetweenEvents);
        assertEquals(7, java.util.Arrays.stream(
            com.guildsofverra.event.WorldEventType.values()
        ).count());
    }

    @Test
    void normalizesUnsafeValuesAndRewritesConfiguration() throws Exception {
        Path path = tempDir.resolve("world_events.json");
        Files.writeString(path, """
            {
              "checkIntervalSeconds": 0,
              "triggerChancePerCheck": 5.0,
              "minimumMinutesBetweenEvents": 0,
              "warningSeconds": -10,
              "minimumAdventurerLevel": -5,
              "bloodMoonDurationSeconds": 1,
              "caveHazardsPerPulse": -4,
              "restlessReviveChance": -1.0,
              "eventSpawnMinimumDistance": 2,
              "eventSpawnMaximumDistance": 1,
              "eventMobCapPerPlayer": 0
            }
            """);

        WorldEventConfig config = WorldEventConfig.load(path);

        assertEquals(10, config.checkIntervalSeconds);
        assertEquals(1.0, config.triggerChancePerCheck);
        assertEquals(1, config.minimumMinutesBetweenEvents);
        assertEquals(0, config.warningSeconds);
        assertEquals(0, config.minimumAdventurerLevel);
        assertEquals(30, config.bloodMoonDurationSeconds);
        assertEquals(0, config.caveHazardsPerPulse);
        assertEquals(0.0, config.restlessReviveChance);
        assertEquals(16, config.eventSpawnMinimumDistance);
        assertEquals(16, config.eventSpawnMaximumDistance);
        assertEquals(1, config.eventMobCapPerPlayer);
        assertTrue(Files.readString(path).contains("\"minimumMinutesBetweenEvents\": 1"));
    }
}
