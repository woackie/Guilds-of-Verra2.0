package com.guildsofverra.core;

import java.util.function.DoubleSupplier;

public final class ProgressionRewardMath {
    private ProgressionRewardMath() {}

    public static int fishingXp(
        int fish,
        int treasure,
        int junk,
        int fallback,
        int fishXp,
        int treasureXp,
        int junkXp,
        int fallbackXp
    ) {
        return Math.max(0, fish) * Math.max(0, fishXp)
            + Math.max(0, treasure) * Math.max(0, treasureXp)
            + Math.max(0, junk) * Math.max(0, junkXp)
            + Math.max(0, fallback) * Math.max(0, fallbackXp);
    }

    public static int bonusRolls(int opportunities, double chance, DoubleSupplier random) {
        int rolls = 0;
        double normalized = Math.max(0.0, Math.min(1.0, chance));
        for (int i = 0; i < Math.max(0, opportunities); i++) {
            if (random.getAsDouble() < normalized) rolls++;
        }
        return rolls;
    }
}
