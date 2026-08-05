package com.guildsofverra.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guildsofverra.GuildsOfVerra;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

/** Runtime progression tuning stored in config/guildsofverra/progression.json. */
public final class ProgressionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile ProgressionConfig current = defaults();

    public int explorationCellSize = 128;
    public int explorationCellXp = 125;
    public double combatDamageXpMultiplier = 3.0;
    public double combatKillHealthXpMultiplier = 2.0;

    public int fishingFishXp = 35;
    public int fishingTreasureXp = 100;
    public int fishingJunkXp = 12;
    public int fishingFallbackXp = 25;
    public int fishingClassificationGraceSeconds = 3;

    public boolean showActivityXpMessages = true;
    public boolean showLevelUpMessages = true;
    public int minimumActivityXpMessage = 1;

    public Map<String, Integer> miningXp = defaultMiningXp();
    public Map<String, Integer> cookingXp = defaultCookingXp();

    private ProgressionConfig() {}

    public static void initialize() {
        Path path = FabricLoader.getInstance().getConfigDir()
            .resolve(GuildsOfVerra.MOD_ID)
            .resolve("progression.json");
        current = load(path);
    }

    public static ProgressionConfig current() {
        return current;
    }

    public static ProgressionConfig defaults() {
        return new ProgressionConfig();
    }

    public static ProgressionConfig load(Path path) {
        ProgressionConfig config = defaults();
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    ProgressionConfig loaded = GSON.fromJson(reader, ProgressionConfig.class);
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
            GuildsOfVerra.LOGGER.error("Failed to load progression config from {}. Using defaults.", path, exception);
            config = defaults();
        }
        return config;
    }

    public int miningXp(String blockId) {
        return Math.max(0, miningXp.getOrDefault(blockId, 0));
    }

    public int cookingXp(String itemId) {
        return Math.max(0, cookingXp.getOrDefault(itemId, 0));
    }

    private void normalize() {
        explorationCellSize = Math.max(32, explorationCellSize);
        explorationCellXp = Math.max(0, explorationCellXp);
        combatDamageXpMultiplier = Math.max(0.0, combatDamageXpMultiplier);
        combatKillHealthXpMultiplier = Math.max(0.0, combatKillHealthXpMultiplier);
        fishingFishXp = Math.max(0, fishingFishXp);
        fishingTreasureXp = Math.max(0, fishingTreasureXp);
        fishingJunkXp = Math.max(0, fishingJunkXp);
        fishingFallbackXp = Math.max(0, fishingFallbackXp);
        fishingClassificationGraceSeconds = Math.max(1, fishingClassificationGraceSeconds);
        minimumActivityXpMessage = Math.max(1, minimumActivityXpMessage);
        if (miningXp == null) miningXp = defaultMiningXp();
        if (cookingXp == null) cookingXp = defaultCookingXp();
    }

    private static Map<String, Integer> defaultMiningXp() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("minecraft:stone", 1);
        map.put("minecraft:deepslate", 1);
        map.put("minecraft:coal_ore", 8);
        map.put("minecraft:deepslate_coal_ore", 8);
        map.put("minecraft:copper_ore", 10);
        map.put("minecraft:deepslate_copper_ore", 10);
        map.put("minecraft:iron_ore", 25);
        map.put("minecraft:deepslate_iron_ore", 25);
        map.put("minecraft:gold_ore", 35);
        map.put("minecraft:deepslate_gold_ore", 35);
        map.put("minecraft:redstone_ore", 18);
        map.put("minecraft:deepslate_redstone_ore", 18);
        map.put("minecraft:lapis_ore", 18);
        map.put("minecraft:deepslate_lapis_ore", 18);
        map.put("minecraft:diamond_ore", 120);
        map.put("minecraft:deepslate_diamond_ore", 120);
        map.put("minecraft:emerald_ore", 150);
        map.put("minecraft:deepslate_emerald_ore", 150);
        map.put("minecraft:ancient_debris", 300);
        return map;
    }

    private static Map<String, Integer> defaultCookingXp() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("minecraft:cooked_cod", 18);
        map.put("minecraft:cooked_salmon", 22);
        map.put("minecraft:cooked_chicken", 22);
        map.put("minecraft:cooked_porkchop", 28);
        map.put("minecraft:cooked_beef", 30);
        map.put("minecraft:cooked_mutton", 26);
        map.put("minecraft:cooked_rabbit", 28);
        map.put("minecraft:baked_potato", 16);
        map.put("minecraft:dried_kelp", 8);
        map.put("minecraft:bread", 18);
        map.put("minecraft:cookie", 10);
        map.put("minecraft:pumpkin_pie", 35);
        map.put("minecraft:cake", 75);
        map.put("minecraft:mushroom_stew", 30);
        map.put("minecraft:rabbit_stew", 55);
        map.put("minecraft:beetroot_soup", 30);
        return map;
    }
}
