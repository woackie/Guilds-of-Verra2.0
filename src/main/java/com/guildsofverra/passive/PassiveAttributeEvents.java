package com.guildsofverra.passive;

import com.guildsofverra.GuildsOfVerra;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.restriction.GvTags;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/** Applies purchased player-attribute passives without changing vanilla base values. */
public final class PassiveAttributeEvents {
    private static final Identifier MAX_HEALTH_ID = id("max_health");
    private static final Identifier MOVEMENT_SPEED_ID = id("movement_speed");
    private static final Identifier MINING_SPEED_ID = id("mining_speed");
    private static final Identifier SWIM_SPEED_ID = id("swim_speed");
    private static final Identifier SHIELD_KNOCKBACK_ID = id("shield_knockback");
    private static final Identifier SWEEPING_DAMAGE_ID = id("sweeping_damage");
    private static final Map<UUID, Snapshot> APPLIED = new HashMap<>();

    private PassiveAttributeEvents() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Set<UUID> online = new HashSet<>();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                online.add(player.getUUID());
                apply(player);
            }
            APPLIED.keySet().retainAll(online);
        });
    }

    private static void apply(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        double maxHealth = PassiveValues.total(player, SkillId.COMBAT, "max_health");
        double movementSpeed = isOnFoot(player)
            ? PassiveValues.total(player, SkillId.EXPLORATION, "movement_speed")
            : 0.0;
        double miningSpeed = isMiningTool(mainHand)
            ? PassiveValues.total(player, SkillId.MINING, "mining_speed")
            : 0.0;
        double swimSpeed = PassiveValues.total(player, SkillId.EXPLORATION, "swim_speed");
        double shieldKnockback = player.isBlocking() && player.getUseItem().is(GvTags.SHIELDS)
            ? Math.max(0.0, -PassiveValues.total(
                player,
                SkillId.COMBAT,
                "shield_durability"
            ) * 0.8)
            : 0.0;
        double sweepingDamage = mainHand.is(ItemTags.SWORDS)
            ? Math.max(0.0, PassiveValues.total(
                player,
                SkillId.COMBAT,
                "sword_damage"
            ) * 2.5)
            : 0.0;

        Snapshot next = new Snapshot(
            player.getId(),
            maxHealth,
            movementSpeed,
            miningSpeed,
            swimSpeed,
            shieldKnockback,
            sweepingDamage
        );
        Snapshot previous = APPLIED.put(player.getUUID(), next);
        if (next.equals(previous)) {
            return;
        }

        replace(
            player,
            Attributes.MAX_HEALTH,
            MAX_HEALTH_ID,
            maxHealth,
            AttributeModifier.Operation.ADD_VALUE
        );
        replace(
            player,
            Attributes.MOVEMENT_SPEED,
            MOVEMENT_SPEED_ID,
            movementSpeed,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        replace(
            player,
            Attributes.BLOCK_BREAK_SPEED,
            MINING_SPEED_ID,
            miningSpeed,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        replace(
            player,
            Attributes.WATER_MOVEMENT_EFFICIENCY,
            SWIM_SPEED_ID,
            swimSpeed,
            AttributeModifier.Operation.ADD_VALUE
        );
        replace(
            player,
            Attributes.KNOCKBACK_RESISTANCE,
            SHIELD_KNOCKBACK_ID,
            shieldKnockback,
            AttributeModifier.Operation.ADD_VALUE
        );
        replace(
            player,
            Attributes.SWEEPING_DAMAGE_RATIO,
            SWEEPING_DAMAGE_ID,
            sweepingDamage,
            AttributeModifier.Operation.ADD_VALUE
        );
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isOnFoot(ServerPlayer player) {
        return !player.isPassenger() && !player.isFallFlying() && !player.isInWater();
    }

    private static boolean isMiningTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES)
            || stack.is(ItemTags.AXES)
            || stack.is(ItemTags.SHOVELS)
            || stack.is(ItemTags.HOES);
    }

    private static void replace(
        ServerPlayer player,
        Holder<Attribute> attribute,
        Identifier id,
        double amount,
        AttributeModifier.Operation operation
    ) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (Math.abs(amount) > 0.0000001) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static Identifier id(String path) {
        return GuildsOfVerra.id("passive/" + path);
    }

    private record Snapshot(
        int entityId,
        double maxHealth,
        double movementSpeed,
        double miningSpeed,
        double swimSpeed,
        double shieldKnockback,
        double sweepingDamage
    ) {}
}
