package com.guildsofverra.restriction;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.data.ProfileManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class RestrictionEvents {
    private static final long DENIAL_COOLDOWN_MILLIS = 1_500L;
    private static final int EQUIPMENT_AUDIT_INTERVAL_TICKS = 10;
    private static final Map<UUID, DenialNotice> LAST_DENIAL = new HashMap<>();
    private static final Set<UUID> PENDING_EQUIPMENT_AUDITS = new HashSet<>();

    private RestrictionEvents() {}

    public static void initialize() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !(player instanceof ServerPlayer serverPlayer)
                || enforce(serverPlayer, player.getMainHandItem(), RestrictionService::canUseAsTool));

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
            player instanceof ServerPlayer serverPlayer
                && !enforce(serverPlayer, player.getItemInHand(hand), RestrictionService::canAttack)
                    ? InteractionResult.FAIL
                    : InteractionResult.PASS);

        ItemEvents.USE.register((level, player, hand) ->
            player instanceof ServerPlayer serverPlayer
                && !enforce(serverPlayer, player.getItemInHand(hand), RestrictionService::canActivate)
                    ? InteractionResult.FAIL
                    : InteractionResult.PASS);

        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previousStack, currentStack) -> {
            if (entity instanceof ServerPlayer player
                && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                PENDING_EQUIPMENT_AUDITS.add(player.getUUID());
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            boolean regularAudit = server.getTickCount() % EQUIPMENT_AUDIT_INTERVAL_TICKS == 0;
            Set<UUID> onlinePlayers = new HashSet<>();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID playerId = player.getUUID();
                onlinePlayers.add(playerId);
                if (regularAudit || PENDING_EQUIPMENT_AUDITS.remove(playerId)) {
                    auditEquipment(player);
                }
            }

            LAST_DENIAL.keySet().retainAll(onlinePlayers);
            PENDING_EQUIPMENT_AUDITS.retainAll(onlinePlayers);
        });
    }

    private static void auditEquipment(ServerPlayer player) {
        PlayerProfile profile = ProfileManager.get(player);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        RequirementResult flightResult = RestrictionService.canEquip(profile, chest);

        if (player.isFallFlying() && !flightResult.allowed()) {
            player.stopFallFlying();
            sendDenial(player, "Elytra flight locked — " + flightResult.reason());
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            enforceEquippedItem(player, slot);
        }
    }

    private static void enforceEquippedItem(ServerPlayer player, EquipmentSlot slot) {
        ItemStack equipped = player.getItemBySlot(slot);
        if (equipped.isEmpty()) {
            return;
        }

        RequirementResult result = RestrictionService.canEquip(ProfileManager.get(player), equipped);
        if (result.allowed()) {
            return;
        }

        ItemStack removed = equipped.copy();
        player.setItemSlot(slot, ItemStack.EMPTY);
        if (!player.getInventory().add(removed)) {
            player.drop(removed, false);
        }
        sendDenial(player, result.reason());
    }

    private static boolean enforce(ServerPlayer player, ItemStack stack, BiFunction<PlayerProfile, ItemStack, RequirementResult> check) {
        RequirementResult result = check.apply(ProfileManager.get(player), stack);
        if (!result.allowed()) {
            sendDenial(player, result.reason());
        }
        return result.allowed();
    }

    private static void sendDenial(ServerPlayer player, String reason) {
        long now = System.currentTimeMillis();
        DenialNotice previous = LAST_DENIAL.get(player.getUUID());
        if (previous != null && previous.reason().equals(reason) && now - previous.timestamp() < DENIAL_COOLDOWN_MILLIS) {
            return;
        }

        LAST_DENIAL.put(player.getUUID(), new DenialNotice(reason, now));
        player.sendSystemMessage(Component.literal("Locked — " + reason));
    }

    private record DenialNotice(String reason, long timestamp) {}
}
