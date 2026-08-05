package com.guildsofverra.core;

import java.util.ArrayList;
import java.util.List;
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
        List<String> missing = new ArrayList<>();
        addMissing(missing, requireAdventurer(profile, adventurer));
        for (Map.Entry<SkillId, Integer> entry : skillLevels.entrySet()) {
            addMissing(missing, requireSkill(profile, entry.getKey(), entry.getValue()));
        }

        return missing.isEmpty()
            ? RequirementResult.allow()
            : RequirementResult.deny("Requires " + String.join(", ", missing));
    }

    private static void addMissing(List<String> missing, RequirementResult result) {
        if (result.allowed()) {
            return;
        }
        String reason = result.reason();
        missing.add(reason.startsWith("Requires ") ? reason.substring("Requires ".length()) : reason);
    }
}
