package com.guildsofverra.world;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.core.RequirementService;
import com.guildsofverra.core.SkillId;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class DimensionGateService {
    private static final GateRequirement NETHER_REQUIREMENT = new GateRequirement(
        25,
        Map.of(
            SkillId.EXPLORATION, 25,
            SkillId.MINING, 20,
            SkillId.COMBAT, 15
        )
    );
    private static final GateRequirement END_REQUIREMENT = new GateRequirement(
        50,
        Map.of(
            SkillId.EXPLORATION, 45,
            SkillId.MINING, 40,
            SkillId.COMBAT, 40
        )
    );

    private DimensionGateService() {}

    public static RequirementResult canEnter(PlayerProfile profile, ResourceKey<Level> dimension) {
        GateRequirement requirement = requirement(dimension);
        return requirement == null
            ? RequirementResult.allow()
            : RequirementService.dimension(
                profile,
                requirement.adventurerLevel(),
                requirement.skillLevels()
            );
    }

    public static GateRequirement requirement(ResourceKey<Level> dimension) {
        if (dimension == Level.NETHER) {
            return NETHER_REQUIREMENT;
        }
        if (dimension == Level.END) {
            return END_REQUIREMENT;
        }
        return null;
    }

    public record GateRequirement(int adventurerLevel, Map<SkillId, Integer> skillLevels) {
        public GateRequirement {
            adventurerLevel = Math.max(0, adventurerLevel);
            skillLevels = Map.copyOf(skillLevels);
        }
    }
}
