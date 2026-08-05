package com.guildsofverra.core;

import java.util.Map;

public final class RequirementService {
    private RequirementService() {}

    public static RequirementResult requireNode(PlayerProfile profile, String fullNodeId) {
        return profile.purchasedNodes().contains(fullNodeId)
            ? RequirementResult.allow()
            : RequirementResult.deny("Requires " + RequirementText.node(fullNodeId));
    }

    public static RequirementResult requireSkill(PlayerProfile profile, SkillId skill, int level) {
        int current = profile.skill(skill).level();
        return current >= level
            ? RequirementResult.allow()
            : RequirementResult.deny(
                "Requires " + RequirementText.skill(skill) + " level " + level
                    + " (current: " + current + ")"
            );
    }

    public static RequirementResult requireAdventurer(PlayerProfile profile, int level) {
        int current = profile.adventurerLevel();
        return current >= level
            ? RequirementResult.allow()
            : RequirementResult.deny(
                "Requires Adventurer Level " + level + " (current: " + current + ")"
            );
    }

    public static RequirementResult dimension(
        PlayerProfile profile,
        int adventurer,
        Map<SkillId, Integer> skillLevels
    ) {
        RequirementResult overall = requireAdventurer(profile, adventurer);
        if (!overall.allowed()) {
            return overall;
        }
        for (Map.Entry<SkillId, Integer> entry : skillLevels.entrySet()) {
            RequirementResult result = requireSkill(profile, entry.getKey(), entry.getValue());
            if (!result.allowed()) {
                return result;
            }
        }
        return RequirementResult.allow();
    }
}
