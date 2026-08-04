package com.guildsofverra.restriction;

import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.data.ProfileManager;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class RestrictionEvents {
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
                && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                && !currentStack.isEmpty()
                && !enforce(player, currentStack, RestrictionService::canEquip)) {
                ItemStack copy = currentStack.copy();
                entity.setItemSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
            }
        });
    }

    private static boolean enforce(
        ServerPlayer player,
        ItemStack stack,
        BiFunction<com.guildsofverra.core.PlayerProfile, ItemStack, RequirementResult> check
    ) {
        RequirementResult result = check.apply(ProfileManager.get(player), stack);
        if (!result.allowed()) {
            player.sendSystemMessage(Component.literal("Locked: " + result.reason()));
        }
        return result.allowed();
    }
}
