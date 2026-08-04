package com.guildsofverra.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProgressionService {
    private ProgressionService() {}

    public static ProgressionChange awardXp(PlayerProfile profile, SkillId skill, long rawXp) {
        if (rawXp <= 0) return new ProgressionChange(profile, skill, 0L, profile.skill(skill).level(), profile.skill(skill).level(), List.of());
        SkillProgress old = profile.skill(skill);
        if (old.level() >= XpCurve.MAX_LEVEL) return new ProgressionChange(profile, skill, 0L, old.level(), old.level(), List.of());
        double prestigeMultiplier = 1.0 + (0.15 * old.prestige());
        long remaining = Math.max(0L, Math.round(rawXp / prestigeMultiplier));
        long xp = old.xp();
        int level = old.level();
        List<Integer> reached = new ArrayList<>();
        while (remaining > 0 && level < XpCurve.MAX_LEVEL) {
            long required = XpCurve.xpToNextLevel(level);
            long needed = required - xp;
            if (remaining >= needed) {
                remaining -= needed; xp = 0L; level++; reached.add(level);
            } else { xp += remaining; remaining = 0L; }
        }
        int highest = Math.max(old.highestLevel(), level);
        int earnedPoints = Math.max(old.earnedPoints(), highest);
        SkillProgress updated = new SkillProgress(level, xp, highest, old.prestige(), earnedPoints);
        return new ProgressionChange(profile.withSkill(skill, updated), skill, rawXp, old.level(), level, List.copyOf(reached));
    }

    public static PurchaseResult purchaseNode(PlayerProfile profile, SkillId skill, String nodeId, SkillTreeDefinition tree) {
        SkillNodeDefinition node = tree.nodesById().get(nodeId);
        if (node == null) return PurchaseResult.failure(profile, "Unknown node: " + nodeId);
        String fullId = skill.serializedName() + ":" + nodeId;
        if (profile.purchasedNodes().contains(fullId)) return PurchaseResult.failure(profile, "Node already purchased");
        SkillProgress progress = profile.skill(skill);
        if (progress.level() < node.minLevel()) return PurchaseResult.failure(profile, "Requires " + skill.serializedName() + " level " + node.minLevel());
        for (String prerequisite : node.prerequisites()) {
            if (!profile.purchasedNodes().contains(skill.serializedName() + ":" + prerequisite)) return PurchaseResult.failure(profile, "Missing prerequisite: " + prerequisite);
        }
        int available = progress.earnedPoints() - profile.spentPoints(skill, tree);
        if (available < node.cost()) return PurchaseResult.failure(profile, "Requires " + node.cost() + " points; " + available + " available");
        return PurchaseResult.success(profile.withPurchasedNode(fullId), "Purchased " + fullId);
    }

    public static PurchaseResult prestige(PlayerProfile profile, SkillId skill, int maxPrestige) {
        SkillProgress current = profile.skill(skill);
        if (current.level() < XpCurve.MAX_LEVEL) return PurchaseResult.failure(profile, "Skill must be level 100");
        if (current.prestige() >= maxPrestige) return PurchaseResult.failure(profile, "Maximum prestige reached");
        return PurchaseResult.success(profile.withSkill(skill, current.withPrestigeReset()), "Prestiged " + skill.serializedName());
    }
}
