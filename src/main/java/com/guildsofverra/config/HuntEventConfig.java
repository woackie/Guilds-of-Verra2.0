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

/** Runtime tuning for player-targeted hunt events. */
public final class HuntEventConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile HuntEventConfig current = defaults();

    public boolean enabled = true;
    public int minimumAdventurerLevel = 15;
    public int checkIntervalSeconds = 30;
    public double triggerChancePerCheck = 0.015;
    public int playerCooldownMinutes = 60;
    public int globalActiveHuntCap = 2;

    public int minimumPackSize = 5;
    public int adventurerLevelsPerAdditionalMob = 15;
    public int maximumPackSize = 10;

    public int minimumSpawnDistance = 40;
    public int maximumSpawnDistance = 64;
    public int spawnAttemptsPerMob = 16;
    public int warningSeconds = 5;
    public int eventDurationSeconds = 300;
    public int retargetIntervalTicks = 20;
    public double pathingSpeed = 1.15;
    public double maximumPursuitDistance = 192.0;

    public boolean announceEvents = true;
    public boolean allowCreativePlayers = false;
    public boolean allowOverworld = true;
    public boolean allowNether = true;
    public boolean allowEnd = true;

    private HuntEventConfig() {}

    public static void initialize() {
        Path path = FabricLoader.getInstance().getConfigDir()
            .resolve(GuildsOfVerra.MOD_ID)
            .resolve("hunt_events.json");
        current = load(path);
    }

    public static HuntEventConfig current() {
        return current;
    }

    public static HuntEventConfig defaults() {
        return new HuntEventConfig();
    }

    public static HuntEventConfig load(Path path) {
        HuntEventConfig config = defaults();
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    HuntEventConfig loaded = GSON.fromJson(reader, HuntEventConfig.class);
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
                "Failed to load hunt event config from {}. Using defaults.",
                path,
                exception
            );
            config = defaults();
        }
        return config;
    }

    private void normalize() {
        minimumAdventurerLevel = Math.max(0, minimumAdventurerLevel);
        checkIntervalSeconds = Math.max(5, checkIntervalSeconds);
        triggerChancePerCheck = Math.max(0.0, Math.min(1.0, triggerChancePerCheck));
        playerCooldownMinutes = Math.max(1, playerCooldownMinutes);
        globalActiveHuntCap = Math.max(0, globalActiveHuntCap);

        minimumPackSize = Math.max(1, minimumPackSize);
        adventurerLevelsPerAdditionalMob = Math.max(1, adventurerLevelsPerAdditionalMob);
        maximumPackSize = Math.max(minimumPackSize, maximumPackSize);

        minimumSpawnDistance = Math.max(16, minimumSpawnDistance);
        maximumSpawnDistance = Math.max(minimumSpawnDistance, maximumSpawnDistance);
        spawnAttemptsPerMob = Math.max(1, spawnAttemptsPerMob);
        warningSeconds = Math.max(0, warningSeconds);
        eventDurationSeconds = Math.max(30, eventDurationSeconds);
        retargetIntervalTicks = Math.max(5, retargetIntervalTicks);
        pathingSpeed = Math.max(0.1, pathingSpeed);
        maximumPursuitDistance = Math.max(maximumSpawnDistance + 16.0, maximumPursuitDistance);
    }
}
