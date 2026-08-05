package com.guildsofverra.network;

import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.PurchaseResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.TitleSelectionService;
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
    private static final int MAX_TITLE_ID_LENGTH = 128;
    private static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern SAFE_RESOURCE_ID =
        Pattern.compile("[a-z0-9_:\\-]+", Pattern.CASE_INSENSITIVE);
    private static final Map<UUID, Long> LAST_JOURNAL_ACTION = new HashMap<>();

    private GvNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(ProfileSyncPayload.TYPE, ProfileSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
            JournalActionResultPayload.TYPE,
            JournalActionResultPayload.CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(PurchaseNodePayload.TYPE, PurchaseNodePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PrestigePayload.TYPE, PrestigePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SelectTitlePayload.TYPE, SelectTitlePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
            PurchaseNodePayload.TYPE,
            (payload, context) -> purchase(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PrestigePayload.TYPE,
            (payload, context) -> prestige(context.player(), payload)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SelectTitlePayload.TYPE,
            (payload, context) -> selectTitle(context.player(), payload)
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
        if (!allowJournalAction(player, "purchase")) {
            return;
        }
        if (!validId(payload.skill(), MAX_SKILL_ID_LENGTH)
            || !validId(payload.node(), MAX_NODE_ID_LENGTH)) {
            reject(player, "purchase", "Invalid journal purchase request.");
            return;
        }

        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.purchaseNode(
                ProfileManager.get(player),
                skill,
                payload.node(),
                GvContent.tree(skill)
            );
            applyResult(player, "purchase", result);
        }, () -> reject(player, "purchase", "Unknown skill: " + payload.skill()));
    }

    private static void prestige(ServerPlayer player, PrestigePayload payload) {
        if (!allowJournalAction(player, "prestige")) {
            return;
        }
        if (!validId(payload.skill(), MAX_SKILL_ID_LENGTH)) {
            reject(player, "prestige", "Invalid journal prestige request.");
            return;
        }

        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.prestige(ProfileManager.get(player), skill, 5);
            applyResult(player, "prestige", result);
        }, () -> reject(player, "prestige", "Unknown skill: " + payload.skill()));
    }

    private static void selectTitle(ServerPlayer player, SelectTitlePayload payload) {
        if (!allowJournalAction(player, "title")) {
            return;
        }

        String titleId = payload.titleId() == null ? "" : payload.titleId().trim();
        if (!titleId.isEmpty()
            && (titleId.length() > MAX_TITLE_ID_LENGTH
                || !SAFE_RESOURCE_ID.matcher(titleId).matches())) {
            reject(player, "title", "Invalid journal title request.");
            return;
        }

        PurchaseResult result = TitleSelectionService.select(ProfileManager.get(player), titleId);
        applyResult(player, "title", result);
    }

    private static void applyResult(ServerPlayer player, String action, PurchaseResult result) {
        if (result.success()) {
            player.setAttached(com.guildsofverra.data.GvAttachments.PROFILE, result.profile());
            sync(player, result.profile());
        }
        sendActionResult(player, action, result.success(), result.message());
        player.sendSystemMessage(Component.literal(result.message()));
    }

    private static void reject(ServerPlayer player, String action, String message) {
        sendActionResult(player, action, false, message);
        player.sendSystemMessage(Component.literal(message));
    }

    private static void sendActionResult(
        ServerPlayer player,
        String action,
        boolean success,
        String message
    ) {
        if (ServerPlayNetworking.canSend(player, JournalActionResultPayload.TYPE)) {
            ServerPlayNetworking.send(
                player,
                new JournalActionResultPayload(action, success, message)
            );
        }
    }

    private static boolean allowJournalAction(ServerPlayer player, String action) {
        long now = System.currentTimeMillis();
        Long previous = LAST_JOURNAL_ACTION.put(player.getUUID(), now);
        boolean allowed = previous == null || now - previous >= JOURNAL_ACTION_COOLDOWN_MILLIS;
        if (!allowed) {
            sendActionResult(player, action, false, "Please wait before sending another journal action.");
        }
        return allowed;
    }

    private static boolean validId(String value, int maximumLength) {
        return value != null
            && !value.isBlank()
            && value.length() <= maximumLength
            && SAFE_ID.matcher(value).matches();
    }
}
