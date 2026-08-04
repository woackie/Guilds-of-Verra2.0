package com.guildsofverra.network;

import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionService;
import com.guildsofverra.core.PurchaseResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.ProfileManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GvNetworking {
    private GvNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.clientboundPlay().register(ProfileSyncPayload.TYPE, ProfileSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PurchaseNodePayload.TYPE, PurchaseNodePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PrestigePayload.TYPE, PrestigePayload.CODEC);
        ServerPlayNetworking.registerReceiver(PurchaseNodePayload.TYPE, (payload, context) -> context.server().execute(() -> purchase(context.player(), payload)));
        ServerPlayNetworking.registerReceiver(PrestigePayload.TYPE, (payload, context) -> context.server().execute(() -> prestige(context.player(), payload)));
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> sync(listener.getPlayer(), ProfileManager.get(listener.getPlayer())));
    }

    public static void sync(ServerPlayer player, PlayerProfile profile) {
        if (ServerPlayNetworking.canSend(player, ProfileSyncPayload.TYPE)) ServerPlayNetworking.send(player, new ProfileSyncPayload(ProfileJson.toJson(profile)));
    }

    private static void purchase(ServerPlayer player, PurchaseNodePayload payload) {
        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.purchaseNode(ProfileManager.get(player), skill, payload.node(), GvContent.tree(skill));
            if (result.success()) {
                player.setAttached(com.guildsofverra.data.GvAttachments.PROFILE, result.profile());
                sync(player, result.profile());
            }
            player.displayClientMessage(Component.literal(result.message()), false);
        }, () -> player.displayClientMessage(Component.literal("Unknown skill: " + payload.skill()), false));
    }

    private static void prestige(ServerPlayer player, PrestigePayload payload) {
        SkillId.parse(payload.skill()).ifPresentOrElse(skill -> {
            PurchaseResult result = ProgressionService.prestige(ProfileManager.get(player), skill, 5);
            if (result.success()) {
                player.setAttached(com.guildsofverra.data.GvAttachments.PROFILE, result.profile());
                sync(player, result.profile());
            }
            player.displayClientMessage(Component.literal(result.message()), false);
        }, () -> player.displayClientMessage(Component.literal("Unknown skill: " + payload.skill()), false));
    }
}
