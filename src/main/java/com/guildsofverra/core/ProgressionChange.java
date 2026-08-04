package com.guildsofverra.core;

import java.util.List;

public record ProgressionChange(PlayerProfile profile, SkillId skill, long awardedXp, int oldLevel,
                                int newLevel, List<Integer> reachedLevels) {
    public boolean leveledUp() { return newLevel > oldLevel; }
}
