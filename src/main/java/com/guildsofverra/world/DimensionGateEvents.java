package com.guildsofverra.world;

import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.data.ProfileManager;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.minecraft.network.chat.Component;

public final class DimensionGateEvents {
    private DimensionGateEvents() {}

    public static void initialize() {
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            RequirementResult result = DimensionGateService.canEnter(ProfileManager.get(player), destination.dimension());
            if (!result.allowed()) {
                player.sendSystemMessage(Component.literal("Dimension locked: " + result.reason()));
                player.teleportTo(
                    origin,
                    player.getX(),
                    Math.max(origin.getMinY() + 2, player.getY()),
                    player.getZ(),
                    Set.of(),
                    player.getYRot(),
                    player.getXRot(),
                    true
                );
            }
        });
    }
}
