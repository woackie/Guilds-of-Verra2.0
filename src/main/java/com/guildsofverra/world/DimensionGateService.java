package com.guildsofverra.world;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.core.RequirementService;
import com.guildsofverra.core.SkillId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Map;

public final class DimensionGateService {
    private DimensionGateService() {}
    public static RequirementResult canEnter(PlayerProfile profile, ResourceKey<Level> dimension) {
        if (dimension == Level.NETHER) return RequirementService.dimension(profile, 25, Map.of(SkillId.EXPLORATION,25,SkillId.MINING,20,SkillId.COMBAT,15));
        if (dimension == Level.END) return RequirementService.dimension(profile, 50, Map.of(SkillId.EXPLORATION,45,SkillId.MINING,40,SkillId.COMBAT,40));
        return RequirementResult.allow();
    }
}
