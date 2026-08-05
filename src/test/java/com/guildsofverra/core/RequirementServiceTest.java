package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class RequirementServiceTest {
    @Test
    void reportsEveryMissingDimensionRequirement() {
        Map<SkillId, Integer> requirements = new LinkedHashMap<>();
        requirements.put(SkillId.EXPLORATION, 25);
        requirements.put(SkillId.MINING, 20);
        requirements.put(SkillId.COMBAT, 15);

        RequirementResult result = RequirementService.dimension(
            PlayerProfile.empty(),
            25,
            requirements
        );

        assertFalse(result.allowed());
        assertTrue(result.reason().contains("Adventurer Level 25 (current: 0)"));
        assertTrue(result.reason().contains("Exploration level 25 (current: 0)"));
        assertTrue(result.reason().contains("Mining level 20 (current: 0)"));
        assertTrue(result.reason().contains("Combat level 15 (current: 0)"));
    }

    @Test
    void allowsPurchasedNodes() {
        PlayerProfile profile = PlayerProfile.empty()
            .withPurchasedNode("combat:shield_training");

        assertTrue(RequirementService.requireNode(profile, "combat:shield_training").allowed());
    }
}
