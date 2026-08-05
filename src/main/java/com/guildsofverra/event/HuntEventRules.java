package com.guildsofverra.event;

/** Pure calculations for hunt-event eligibility, scaling and triggering. */
public final class HuntEventRules {
    private HuntEventRules() {}

    public static boolean eligible(
        int adventurerLevel,
        int minimumAdventurerLevel,
        boolean alive,
        boolean spectator,
        boolean creative,
        boolean allowCreative,
        boolean alreadyHunted,
        long currentTick,
        long cooldownUntilTick,
        int activeHunts,
        int activeHuntCap
    ) {
        return alive
            && !spectator
            && (allowCreative || !creative)
            && !alreadyHunted
            && adventurerLevel >= minimumAdventurerLevel
            && currentTick >= cooldownUntilTick
            && activeHuntCap > 0
            && activeHunts < activeHuntCap;
    }

    public static int packSize(
        int adventurerLevel,
        int minimumAdventurerLevel,
        int minimumPackSize,
        int levelsPerAdditionalMob,
        int maximumPackSize
    ) {
        int safeMinimum = Math.max(1, minimumPackSize);
        int safeMaximum = Math.max(safeMinimum, maximumPackSize);
        int safeStep = Math.max(1, levelsPerAdditionalMob);
        int extraLevels = Math.max(0, adventurerLevel - Math.max(0, minimumAdventurerLevel));
        int scaled = safeMinimum + extraLevels / safeStep;
        return Math.min(safeMaximum, scaled);
    }

    public static boolean shouldTrigger(double randomRoll, double chance) {
        double normalizedChance = Math.max(0.0, Math.min(1.0, chance));
        return randomRoll >= 0.0 && randomRoll < normalizedChance;
    }

    public static long cooldownTicks(int cooldownMinutes) {
        return Math.max(1L, cooldownMinutes) * 60L * 20L;
    }

    public static long durationTicks(int durationSeconds) {
        return Math.max(1L, durationSeconds) * 20L;
    }
}
