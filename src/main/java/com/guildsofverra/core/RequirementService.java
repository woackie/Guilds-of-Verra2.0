package com.guildsofverra.core;

import java.util.Map;

public final class RequirementService {
    private RequirementService() {}

    public static RequirementResult requireNode(PlayerProfile profile, String fullNodeId) {
        return profile.purchasedNodes().contains(fullNodeId)
                ? RequirementResult.allow()
                : RequirementResult.deny("Requires " + fullNodeId);
    }

    public static RequirementResult requireSkill(PlayerProfile profile, SkillId skill, int level) {
        return profile.skill(skill).level() >= level
                ? RequirementResult.allow()
                : RequirementResult.deny("Requires " + skill.serializedName() + " level " + level);
    }

    public static RequirementResult requireAdventurer(PlayerProfile profile, int level) {
        return profile.adventurerLevel() >= level
                ? RequirementResult.allow()
                : RequirementResult.deny("Requires Adventurer Level " + level);
    }

    public static RequirementResult dimension(PlayerProfile profile, int adventurer, Map<SkillId,Integer> skillLevels) {
        RequirementResult overall = requireAdventurer(profile, adventurer);
        if (!overall.allowed()) return overall;
        for (Map.Entry<SkillId,Integer> entry : skillLevels.entrySet()) {
            RequirementResult result = requireSkill(profile, entry.getKey(), entry.getValue());
            if (!result.allowed()) return result;
        }
        return RequirementResult.allow();
    }
}
