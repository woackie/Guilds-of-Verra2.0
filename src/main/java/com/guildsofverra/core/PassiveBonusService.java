package com.guildsofverra.core;

import java.util.Map;

/** Resolves additive values from purchased data-driven skill nodes. */
public final class PassiveBonusService {
    private PassiveBonusService() {}

    public static double total(
        PlayerProfile profile,
        SkillId skill,
        SkillTreeDefinition tree,
        String bonusType
    ) {
        double total = 0.0;
        String prefix = skill.serializedName() + ":";
        for (String purchased : profile.purchasedNodes()) {
            if (!purchased.startsWith(prefix)) continue;
            SkillNodeDefinition node = tree.nodesById().get(purchased.substring(prefix.length()));
            if (node == null) continue;
            Map<String, Object> bonus = node.bonus();
            if (!bonusType.equals(String.valueOf(bonus.get("type")))) continue;
            Object value = bonus.get("value");
            if (value instanceof Number number) total += number.doubleValue();
        }
        return total;
    }

    public static long applyPositiveMultiplier(long base, double additiveBonus) {
        if (base <= 0) return 0L;
        return Math.max(1L, Math.round(base * Math.max(0.0, 1.0 + additiveBonus)));
    }

    public static double chance(double additiveBonus) {
        return Math.max(0.0, Math.min(1.0, additiveBonus));
    }
}
