package com.guildsofverra.elite;

public final class EliteSpawnRules {
    private EliteSpawnRules() {}

    public static double spawnChance(
        int adventurerLevel,
        int minimumLevel,
        double baseChance,
        double chancePerLevel,
        double maximumChance
    ) {
        if (adventurerLevel < minimumLevel) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(maximumChance, baseChance + chancePerLevel * adventurerLevel));
    }

    public static boolean withinBudgets(
        int nearbyElites,
        int nearbyCap,
        int regionalElites,
        int regionalCap
    ) {
        return nearbyCap > 0
            && regionalCap > 0
            && nearbyElites < nearbyCap
            && regionalElites < regionalCap;
    }

    public static double statScale(
        int adventurerLevel,
        double scalingPerLevel,
        double maximumScale
    ) {
        return Math.max(1.0, Math.min(maximumScale, 1.0 + adventurerLevel * scalingPerLevel));
    }

    public static double rewardAdjustment(double healthMultiplier, double xpMultiplier) {
        if (healthMultiplier <= 0.0) {
            return 1.0;
        }
        return Math.max(1.0, xpMultiplier / healthMultiplier);
    }
}
