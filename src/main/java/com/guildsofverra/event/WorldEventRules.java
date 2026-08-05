package com.guildsofverra.event;

import java.util.List;
import java.util.function.ToIntFunction;

public final class WorldEventRules {
    private WorldEventRules() {}

    public static boolean eligiblePlayer(
        int adventurerLevel,
        int minimumAdventurerLevel,
        boolean alive,
        boolean spectator,
        boolean creative,
        boolean allowCreative
    ) {
        return adventurerLevel >= Math.max(0, minimumAdventurerLevel)
            && alive
            && !spectator
            && (allowCreative || !creative);
    }

    public static boolean shouldSchedule(
        double randomRoll,
        double triggerChance,
        long currentTick,
        long nextAllowedTick,
        boolean pendingOrActive
    ) {
        double chance = Math.max(0.0, Math.min(1.0, triggerChance));
        return !pendingOrActive
            && currentTick >= nextAllowedTick
            && randomRoll >= 0.0
            && randomRoll < chance;
    }

    public static long durationTicks(int seconds) {
        return Math.max(30L, seconds) * 20L;
    }

    public static long cooldownTicks(int minutes) {
        return Math.max(1L, minutes) * 60L * 20L;
    }

    public static int totalWeight(
        List<WorldEventType> candidates,
        ToIntFunction<WorldEventType> weight
    ) {
        return candidates.stream()
            .mapToInt(type -> Math.max(0, weight.applyAsInt(type)))
            .sum();
    }

    public static WorldEventType pickWeighted(
        List<WorldEventType> candidates,
        ToIntFunction<WorldEventType> weight,
        int roll
    ) {
        int total = totalWeight(candidates, weight);
        if (total <= 0) {
            return null;
        }

        int remaining = Math.floorMod(roll, total);
        for (WorldEventType type : candidates) {
            int currentWeight = Math.max(0, weight.applyAsInt(type));
            if (remaining < currentWeight) {
                return type;
            }
            remaining -= currentWeight;
        }
        return null;
    }

    public static int cappedSpawnCount(int requested, int existing, int cap) {
        int safeCap = Math.max(0, cap);
        int room = Math.max(0, safeCap - Math.max(0, existing));
        return Math.min(Math.max(0, requested), room);
    }
}
