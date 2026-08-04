package com.guildsofverra.core;

public record SkillProgress(int level, long xp, int highestLevel, int prestige, int earnedPoints) {
    public SkillProgress {
        level = clamp(level, 0, XpCurve.MAX_LEVEL);
        highestLevel = clamp(Math.max(highestLevel, level), 0, XpCurve.MAX_LEVEL);
        prestige = Math.max(0, prestige);
        earnedPoints = clamp(Math.max(earnedPoints, highestLevel), 0, XpCurve.MAX_LEVEL);
        xp = level >= XpCurve.MAX_LEVEL ? 0L : Math.max(0L, Math.min(xp, Math.max(0L, XpCurve.xpToNextLevel(level) - 1L)));
    }

    public static SkillProgress empty() { return new SkillProgress(0, 0L, 0, 0, 0); }
    public SkillProgress withPrestigeReset() { return new SkillProgress(0, 0L, highestLevel, prestige + 1, earnedPoints); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
