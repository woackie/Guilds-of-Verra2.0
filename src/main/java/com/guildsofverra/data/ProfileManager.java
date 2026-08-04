package com.guildsofverra.data;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.network.GvNetworking;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.UnaryOperator;

public final class ProfileManager {
    private ProfileManager() {}
    public static PlayerProfile get(ServerPlayer player) { return player.getAttachedOrCreate(GvAttachments.PROFILE); }
    public static PlayerProfile update(ServerPlayer player, UnaryOperator<PlayerProfile> operation) {
        PlayerProfile next = operation.apply(get(player));
        player.setAttached(GvAttachments.PROFILE, next);
        GvNetworking.sync(player, next);
        return next;
    }
}
