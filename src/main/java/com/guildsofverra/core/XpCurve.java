package com.guildsofverra.core;

public final class XpCurve {
    public static final int MAX_LEVEL = 100;
    private XpCurve() {}

    public static long xpToNextLevel(int currentLevel) {
        if (currentLevel < 0 || currentLevel >= MAX_LEVEL) return 0L;
        return Math.round(100.0 + (12.0 * currentLevel) + (1.5 * currentLevel * currentLevel));
    }

    public static long totalXpToLevel(int level) {
        int clamped = Math.max(0, Math.min(MAX_LEVEL, level));
        long total = 0L;
        for (int current = 0; current < clamped; current++) total += xpToNextLevel(current);
        return total;
    }

    public static long totalXpToMax() { return totalXpToLevel(MAX_LEVEL); }
}
