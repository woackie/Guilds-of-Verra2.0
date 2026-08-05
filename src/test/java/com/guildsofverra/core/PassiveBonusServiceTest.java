package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PassiveBonusServiceTest {
    @Test
    void sumsOnlyPurchasedBonusesOfRequestedType() {
        SkillTreeDefinition tree = new SkillTreeDefinition(
            SkillId.MINING,
            100,
            1,
            List.of(
                node("ore_1", "ore_xp", 0.05),
                node("ore_2", "ore_xp", 0.05),
                node("speed", "mining_speed", 0.20)
            )
        );
        PlayerProfile profile = PlayerProfile.empty()
            .withPurchasedNode("mining:ore_1")
            .withPurchasedNode("mining:ore_2")
            .withPurchasedNode("mining:speed");

        assertEquals(0.10, PassiveBonusService.total(profile, SkillId.MINING, tree, "ore_xp"), 0.00001);
    }

    @Test
    void appliesPositiveMultiplierSafely() {
        assertEquals(120L, PassiveBonusService.applyPositiveMultiplier(100L, 0.20));
        assertEquals(1L, PassiveBonusService.applyPositiveMultiplier(1L, -0.99));
        assertEquals(0L, PassiveBonusService.applyPositiveMultiplier(0L, 0.50));
    }

    private static SkillNodeDefinition node(String id, String type, double value) {
        return new SkillNodeDefinition(
            id,
            0,
            1,
            "test",
            "test",
            List.of(),
            Map.of("type", type, "value", value)
        );
    }
}
