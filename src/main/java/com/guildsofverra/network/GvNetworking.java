package com.guildsofverra.network;

import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.PurchaseResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.ProfileManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GvNetworking {
    private static final long JOURNAL_ACTION_COOLDOWN_MILLIS = 250L;
    private static final int MAX_SKILL_ID_LENGTH = 24;
    private static final int MAX_NODE_ID_LENGTH = 96;
    private static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_]+", Pattern.CASE_INSENSITIVE);
    private static final Map<UUID, Long> LAST_JOURNAL_ACTION = new HashMap<>();

    private GvNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(ProfileSyncPayload.TYPE, ProfileSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PurchaseNodePayload.TYPE, PurchaseNodePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PrestigePayload.TYPE, PrestigePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
            PurchaseNodePayload.TYPE,
            (payload, context) -> purchase(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PrestigePayload.TYPE,
            (payload, context) -> prestige(context.player(), payload)
        );

        ServerPlayConnectionEvents.JOIN.register(
            (listener, sender, server) -> {
                ServerPlayer player = listener.getPlayer();
                LAST_JOURNAL_ACTION.remove(player.getUUID());
                sync(player, ProfileManager.get(player));
            }
        );
    }

    public static void sync(ServerPlayer player, PlayerProfile profile) {
        if (ServerPlayNetworking.canSend(player, ProfileSyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, new ProfileSyncPayload(ProfileJson.toJson(profile)));
        }
    }

    private static void purchase(ServerPlayer player, PurchaseNodePayload payload) {
        if (!allowJournalAction(player)) {
            return;
        }
        if (!validId(payload.skill(), MAX_SKILL_ID_LENGTH)
            || !validId(payload.node(), MAX_NODE_ID_LENGTH)) {
            player.sendSystemMessage(Component.literal("Invalid journal purchase request."));
            return;
        }

        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.purchaseNode(
                ProfileManager.get(player),
                skill,
                payload.node(),
                GvContent.tree(skill)
            );
            if (result.success()) {
                player.setAttached(com.guildsofverra.data.GvAttachments.PROFILE, result.profile());
                sync(player, result.profile());
            }
            player.sendSystemMessage(Component.literal(result.message()));
        }, () -> player.sendSystemMessage(Component.literal("Unknown skill: " + payload.skill())));
    }

    private static void prestige(ServerPlayer player, PrestigePayload payload) {
        if (!allowJournalAction(player)) {
            return;
        }
        if (!validId(payload.skill(), MAX_SKILL_ID_LENGTH)) {
            player.sendSystemMessage(Component.literal("Invalid journal prestige request."));
            return;
        }

        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.prestige(ProfileManager.get(player), skill, 5);
            if (result.success()) {
                player.setAttached(com.guildsofverra.data.GvAttachments.PROFILE, result.profile());
                sync(player, result.profile());
            }
            player.sendSystemMessage(Component.literal(result.message()));
        }, () -> player.sendSystemMessage(Component.literal("Unknown skill: " + payload.skill())));
    }

    private static boolean allowJournalAction(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long previous = LAST_JOURNAL_ACTION.put(player.getUUID(), now);
        return previous == null || now - previous >= JOURNAL_ACTION_COOLDOWN_MILLIS;
    }

    private static boolean validId(String value, int maximumLength) {
        return value != null
            && !value.isBlank()
            && value.length() <= maximumLength
            && SAFE_ID.matcher(value).matches();
    }
}
