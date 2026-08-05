package com.guildsofverra.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JournalSummaryServiceTest {
    @Test
    void aggregatesOnlyPurchasedImplementedBonusesForTheRequestedSkill() {
        SkillTreeDefinition tree = new SkillTreeDefinition(
            SkillId.MINING,
            100,
            1,
            List.of(
                node("ore_one", "ore_xp", 0.10),
                node("ore_two", "ore_xp", 0.15),
                node("future_fortune", "drop_chance", 0.05),
                new SkillNodeDefinition(
                    "informational",
                    1,
                    1,
                    "core",
                    "No numeric bonus",
                    List.of(),
                    Map.of("type", "ore_xp", "value", "not-a-number")
                )
            )
        );
        PlayerProfile profile = new PlayerProfile(
            PlayerProfile.SCHEMA_VERSION,
            Map.of(),
            Set.of(
                "mining:ore_one",
                "mining:ore_two",
                "mining:future_fortune",
                "mining:informational",
                "combat:ore_one"
            ),
            Set.of(),
            Set.of(),
            ""
        );

        assertEquals(
            Map.of("ore_xp", 0.25),
            JournalSummaryService.passiveBonuses(profile, SkillId.MINING, tree)
        );
    }

    @Test
    void includesImplementedFishingAndCookingBonusTypes() {
        SkillTreeDefinition fishing = new SkillTreeDefinition(
            SkillId.FISHING,
            100,
            1,
            List.of(node("extra", "extra_fish", 0.10))
        );
        SkillTreeDefinition cooking = new SkillTreeDefinition(
            SkillId.COOKING,
            100,
            1,
            List.of(node("extra", "extra_cooked_output", 0.20))
        );
        PlayerProfile profile = new PlayerProfile(
            PlayerProfile.SCHEMA_VERSION,
            Map.of(),
            Set.of("fishing:extra", "cooking:extra"),
            Set.of(),
            Set.of(),
            ""
        );

        assertEquals(
            Map.of("extra_fish", 0.10),
            JournalSummaryService.passiveBonuses(profile, SkillId.FISHING, fishing)
        );
        assertEquals(
            Map.of("extra_cooked_output", 0.20),
            JournalSummaryService.passiveBonuses(profile, SkillId.COOKING, cooking)
        );
    }

    private static SkillNodeDefinition node(String id, String type, double value) {
        return new SkillNodeDefinition(
            id,
            1,
            1,
            "core",
            id,
            List.of(),
            Map.of("type", type, "value", value)
        );
    }
}
