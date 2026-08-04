package com.guildsofverra.api;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionChange;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.GvAttachments;
import com.guildsofverra.data.ProfileManager;
import com.guildsofverra.network.GvNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Public server-side entry points for integrations and gameplay event hooks. */
public final class GuildsOfVerraApi {
    private GuildsOfVerraApi() {}

    public static ProgressionChange awardXp(ServerPlayer player, SkillId skill, long amount) {
        PlayerProfile current = ProfileManager.get(player);
        ProgressionChange change = ProgressionService.awardXp(current, skill, amount);
        if (change.profile() != current) {
            player.setAttached(GvAttachments.PROFILE, change.profile());
            GvNetworking.sync(player, change.profile());
        }
        return change;
    }
}
