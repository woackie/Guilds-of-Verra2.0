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

/** Runtime elite tuning stored in config/guildsofverra/elites.json. */
public final class EliteConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile EliteConfig current = defaults();

    public boolean enabled = true;
    public int minimumAdventurerLevel = 10;
    public double baseSpawnChance = 0.005;
    public double spawnChancePerAdventurerLevel = 0.00045;
    public double maximumSpawnChance = 0.05;

    public int nearbyEliteCap = 3;
    public double nearbyRadius = 48.0;
    public int regionEliteCap = 8;
    public double regionRadius = 128.0;

    public double statScalingPerAdventurerLevel = 0.003;
    public double maximumStatScaling = 1.30;
    public boolean showEliteNames = true;

    public double combatXpRewardScale = 1.0;
    public long firstDiscoveryExplorationXp = 250L;
    public boolean announceFirstDiscovery = true;

    public boolean specialAbilitiesEnabled = true;
    public int venomPoisonTicks = 100;
    public int marksmanSlownessTicks = 60;
    public int bulwarkWeaknessTicks = 80;
    public float volatileFireSeconds = 4.0F;

    private EliteConfig() {}

    public static void initialize() {
        Path path = FabricLoader.getInstance().getConfigDir()
            .resolve(GuildsOfVerra.MOD_ID)
            .resolve("elites.json");
        current = load(path);
    }

    public static EliteConfig current() {
        return current;
    }

    public static EliteConfig defaults() {
        return new EliteConfig();
    }

    public static EliteConfig load(Path path) {
        EliteConfig config = defaults();
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    EliteConfig loaded = GSON.fromJson(reader, EliteConfig.class);
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
                "Failed to load elite config from {}. Using defaults.",
                path,
                exception
            );
            config = defaults();
        }
        return config;
    }

    private void normalize() {
        minimumAdventurerLevel = Math.max(0, minimumAdventurerLevel);
        baseSpawnChance = clampChance(baseSpawnChance);
        spawnChancePerAdventurerLevel = Math.max(0.0, spawnChancePerAdventurerLevel);
        maximumSpawnChance = Math.max(baseSpawnChance, clampChance(maximumSpawnChance));
        nearbyEliteCap = Math.max(0, nearbyEliteCap);
        nearbyRadius = Math.max(8.0, nearbyRadius);
        regionEliteCap = Math.max(nearbyEliteCap, regionEliteCap);
        regionRadius = Math.max(nearbyRadius, regionRadius);
        statScalingPerAdventurerLevel = Math.max(0.0, statScalingPerAdventurerLevel);
        maximumStatScaling = Math.max(1.0, maximumStatScaling);
        combatXpRewardScale = Math.max(0.0, combatXpRewardScale);
        firstDiscoveryExplorationXp = Math.max(0L, firstDiscoveryExplorationXp);
        venomPoisonTicks = Math.max(0, venomPoisonTicks);
        marksmanSlownessTicks = Math.max(0, marksmanSlownessTicks);
        bulwarkWeaknessTicks = Math.max(0, bulwarkWeaknessTicks);
        volatileFireSeconds = Math.max(0.0F, volatileFireSeconds);
    }

    private static double clampChance(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
