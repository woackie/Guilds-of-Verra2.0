package com.guildsofverra.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guildsofverra.GuildsOfVerra;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Runtime tuning for mutually-exclusive server-wide survival events. */
public final class WorldEventConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile WorldEventConfig current = defaults();

    public boolean enabled = true;
    public int minimumAdventurerLevel = 10;
    public int initialDelayMinutes = 30;
    public int checkIntervalSeconds = 60;
    public double triggerChancePerCheck = 0.03;
    public int minimumMinutesBetweenEvents = 60;
    public int warningSeconds = 15;
    public boolean announceEvents = true;
    public boolean allowCreativePlayers = false;

    public boolean bloodMoonEnabled = true;
    public int bloodMoonWeight = 3;
    public int bloodMoonDurationSeconds = 600;
    public int bloodMoonBuffRadius = 64;
    public int bloodMoonBuffRefreshTicks = 100;

    public boolean severeThunderstormEnabled = true;
    public int severeThunderstormWeight = 3;
    public int severeThunderstormDurationSeconds = 480;
    public int thunderLightningIntervalSeconds = 15;
    public double thunderLightningChance = 0.35;
    public int thunderLightningRadius = 24;

    public boolean caveTremorEnabled = true;
    public int caveTremorWeight = 4;
    public int caveTremorDurationSeconds = 360;
    public int caveMaximumY = 48;
    public int cavePulseIntervalSeconds = 20;
    public int caveMiningFatigueTicks = 100;
    public int caveHazardsPerPulse = 2;

    public boolean netherSurgeEnabled = true;
    public int netherSurgeWeight = 3;
    public int netherSurgeDurationSeconds = 420;
    public int netherSpawnIntervalSeconds = 30;
    public int netherPackSize = 3;
    public int netherBuffRefreshTicks = 100;

    public boolean predatorMigrationEnabled = true;
    public int predatorMigrationWeight = 4;
    public int predatorMigrationDurationSeconds = 300;
    public int predatorSpawnIntervalSeconds = 45;
    public int predatorPackSize = 6;
    public double predatorMigrationSpeed = 1.0;

    public boolean longNightEnabled = true;
    public int longNightWeight = 2;
    public int longNightDurationSeconds = 720;

    public boolean restlessDeadEnabled = true;
    public int restlessDeadWeight = 4;
    public int restlessDeadDurationSeconds = 420;
    public double restlessReviveChance = 0.25;
    public int restlessReviveDelaySeconds = 5;
    public int restlessMaximumPendingRevives = 64;

    public int eventSpawnMinimumDistance = 24;
    public int eventSpawnMaximumDistance = 48;
    public int eventSpawnAttempts = 12;
    public int eventMobCapPerPlayer = 16;

    private WorldEventConfig() {}

    public static void initialize() {
        Path path = FabricLoader.getInstance().getConfigDir()
            .resolve(GuildsOfVerra.MOD_ID)
            .resolve("world_events.json");
        current = load(path);
    }

    public static WorldEventConfig current() {
        return current;
    }

    public static WorldEventConfig defaults() {
        return new WorldEventConfig();
    }

    public static WorldEventConfig load(Path path) {
        WorldEventConfig config = defaults();
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    WorldEventConfig loaded = GSON.fromJson(reader, WorldEventConfig.class);
                    if (loaded != null) {
                        config = loaded;
                    }
                }
            }
            config.normalize();
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException | RuntimeException exception) {
            GuildsOfVerra.LOGGER.error(
                "Failed to load world event config from {}. Using defaults.",
                path,
                exception
            );
            config = defaults();
        }
        return config;
    }

    public int durationSeconds(String eventId) {
        return switch (eventId) {
            case "blood_moon" -> bloodMoonDurationSeconds;
            case "severe_thunderstorm" -> severeThunderstormDurationSeconds;
            case "cave_tremor" -> caveTremorDurationSeconds;
            case "nether_surge" -> netherSurgeDurationSeconds;
            case "predator_migration" -> predatorMigrationDurationSeconds;
            case "long_night" -> longNightDurationSeconds;
            case "restless_dead" -> restlessDeadDurationSeconds;
            default -> 300;
        };
    }

    public int weight(String eventId) {
        return switch (eventId) {
            case "blood_moon" -> bloodMoonEnabled ? bloodMoonWeight : 0;
            case "severe_thunderstorm" -> severeThunderstormEnabled ? severeThunderstormWeight : 0;
            case "cave_tremor" -> caveTremorEnabled ? caveTremorWeight : 0;
            case "nether_surge" -> netherSurgeEnabled ? netherSurgeWeight : 0;
            case "predator_migration" -> predatorMigrationEnabled ? predatorMigrationWeight : 0;
            case "long_night" -> longNightEnabled ? longNightWeight : 0;
            case "restless_dead" -> restlessDeadEnabled ? restlessDeadWeight : 0;
            default -> 0;
        };
    }

    private void normalize() {
        minimumAdventurerLevel = Math.max(0, minimumAdventurerLevel);
        initialDelayMinutes = Math.max(0, initialDelayMinutes);
        checkIntervalSeconds = Math.max(10, checkIntervalSeconds);
        triggerChancePerCheck = clampChance(triggerChancePerCheck);
        minimumMinutesBetweenEvents = Math.max(1, minimumMinutesBetweenEvents);
        warningSeconds = Math.max(0, warningSeconds);

        bloodMoonWeight = nonNegative(bloodMoonWeight);
        bloodMoonDurationSeconds = atLeastThirty(bloodMoonDurationSeconds);
        bloodMoonBuffRadius = Math.max(16, bloodMoonBuffRadius);
        bloodMoonBuffRefreshTicks = Math.max(20, bloodMoonBuffRefreshTicks);

        severeThunderstormWeight = nonNegative(severeThunderstormWeight);
        severeThunderstormDurationSeconds = atLeastThirty(severeThunderstormDurationSeconds);
        thunderLightningIntervalSeconds = Math.max(5, thunderLightningIntervalSeconds);
        thunderLightningChance = clampChance(thunderLightningChance);
        thunderLightningRadius = Math.max(8, thunderLightningRadius);

        caveTremorWeight = nonNegative(caveTremorWeight);
        caveTremorDurationSeconds = atLeastThirty(caveTremorDurationSeconds);
        cavePulseIntervalSeconds = Math.max(5, cavePulseIntervalSeconds);
        caveMiningFatigueTicks = nonNegative(caveMiningFatigueTicks);
        caveHazardsPerPulse = nonNegative(caveHazardsPerPulse);

        netherSurgeWeight = nonNegative(netherSurgeWeight);
        netherSurgeDurationSeconds = atLeastThirty(netherSurgeDurationSeconds);
        netherSpawnIntervalSeconds = Math.max(10, netherSpawnIntervalSeconds);
        netherPackSize = Math.max(1, netherPackSize);
        netherBuffRefreshTicks = Math.max(20, netherBuffRefreshTicks);

        predatorMigrationWeight = nonNegative(predatorMigrationWeight);
        predatorMigrationDurationSeconds = atLeastThirty(predatorMigrationDurationSeconds);
        predatorSpawnIntervalSeconds = Math.max(10, predatorSpawnIntervalSeconds);
        predatorPackSize = Math.max(1, predatorPackSize);
        predatorMigrationSpeed = Math.max(0.1, predatorMigrationSpeed);

        longNightWeight = nonNegative(longNightWeight);
        longNightDurationSeconds = atLeastThirty(longNightDurationSeconds);

        restlessDeadWeight = nonNegative(restlessDeadWeight);
        restlessDeadDurationSeconds = atLeastThirty(restlessDeadDurationSeconds);
        restlessReviveChance = clampChance(restlessReviveChance);
        restlessReviveDelaySeconds = Math.max(1, restlessReviveDelaySeconds);
        restlessMaximumPendingRevives = nonNegative(restlessMaximumPendingRevives);

        eventSpawnMinimumDistance = Math.max(16, eventSpawnMinimumDistance);
        eventSpawnMaximumDistance = Math.max(eventSpawnMinimumDistance, eventSpawnMaximumDistance);
        eventSpawnAttempts = Math.max(1, eventSpawnAttempts);
        eventMobCapPerPlayer = Math.max(1, eventMobCapPerPlayer);
    }

    private static int atLeastThirty(int value) {
        return Math.max(30, value);
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    private static double clampChance(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
