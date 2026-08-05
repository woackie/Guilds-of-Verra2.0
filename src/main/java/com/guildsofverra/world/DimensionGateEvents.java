package com.guildsofverra.world;

import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.data.ProfileManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class DimensionGateEvents {
    private static final int POSITION_HISTORY_TICKS = 60;
    private static final int SAFE_RETURN_LOOKBACK_TICKS = 20;
    private static final long RETURN_GUARD_MILLIS = 5_000L;
    private static final long NOTICE_COOLDOWN_MILLIS = 2_000L;

    private static final SafeReturnHistory<ResourceKey<Level>> RETURN_HISTORY =
        new SafeReturnHistory<>(POSITION_HISTORY_TICKS, SAFE_RETURN_LOOKBACK_TICKS);
    private static final Map<UUID, Long> RETURN_GUARD_UNTIL = new HashMap<>();
    private static final Map<UUID, GateNotice> LAST_NOTICE = new HashMap<>();

    private DimensionGateEvents() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            Set<UUID> onlinePlayers = new HashSet<>();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                onlinePlayers.add(playerId);

                Long guardUntil = RETURN_GUARD_UNTIL.get(playerId);
                if (guardUntil != null && guardUntil > now) {
                    continue;
                }
                RETURN_GUARD_UNTIL.remove(playerId);

                RETURN_HISTORY.record(
                    playerId,
                    player.serverLevel().dimension(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
                );
            }

            RETURN_HISTORY.retainPlayers(onlinePlayers);
            RETURN_GUARD_UNTIL.keySet().retainAll(onlinePlayers);
            LAST_NOTICE.keySet().retainAll(onlinePlayers);
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            UUID playerId = player.getUUID();
            long now = System.currentTimeMillis();
            Long guardUntil = RETURN_GUARD_UNTIL.get(playerId);
            if (guardUntil != null && guardUntil > now) {
                RETURN_GUARD_UNTIL.remove(playerId);
                return;
            }

            RequirementResult result = DimensionGateService.canEnter(
                ProfileManager.get(player),
                destination.dimension()
            );
            if (result.allowed()) {
                return;
            }

            sendGateNotice(player, result.reason(), now);
            SafeReturnHistory.Snapshot<ResourceKey<Level>> returnPoint =
                RETURN_HISTORY.select(playerId, origin.dimension());

            double x = returnPoint == null ? player.getX() : returnPoint.x();
            double y = returnPoint == null
                ? Math.max(origin.getMinY() + 2, player.getY())
                : Math.max(origin.getMinY() + 2, returnPoint.y());
            double z = returnPoint == null ? player.getZ() : returnPoint.z();
            float yaw = returnPoint == null ? player.getYRot() : returnPoint.yaw();
            float pitch = returnPoint == null ? player.getXRot() : returnPoint.pitch();

            RETURN_GUARD_UNTIL.put(playerId, now + RETURN_GUARD_MILLIS);
            player.teleportTo(origin, x, y, z, Set.of(), yaw, pitch, true);
        });
    }

    private static void sendGateNotice(ServerPlayer player, String reason, long now) {
        GateNotice previous = LAST_NOTICE.get(player.getUUID());
        if (previous != null
            && previous.reason().equals(reason)
            && now - previous.timestamp() < NOTICE_COOLDOWN_MILLIS) {
            return;
        }

        LAST_NOTICE.put(player.getUUID(), new GateNotice(reason, now));
        player.sendSystemMessage(Component.literal("Dimension locked — " + reason));
    }

    private record GateNotice(String reason, long timestamp) {}
}
