package com.guildsofverra.passive;

import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PassiveBonusService;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.ProfileManager;
import net.minecraft.server.level.ServerPlayer;

/** Central server-authoritative lookup for purchased skill-node bonuses. */
public final class PassiveValues {
    private PassiveValues() {}

    public static double total(ServerPlayer player, SkillId skill, String type) {
        return total(ProfileManager.get(player), skill, type);
    }

    public static double total(PlayerProfile profile, SkillId skill, String type) {
        return PassiveBonusService.total(profile, skill, GvContent.tree(skill), type);
    }

    public static double chance(ServerPlayer player, SkillId skill, String type) {
        return PassiveBonusService.chance(total(player, skill, type));
    }
}
