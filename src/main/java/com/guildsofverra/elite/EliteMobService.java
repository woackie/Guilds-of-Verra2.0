package com.guildsofverra.elite;

import com.guildsofverra.data.ProfileManager;
import java.util.Comparator;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class EliteMobService {
    private static final String CHECKED_TAG = "guildsofverra_elite_checked";
    private static final String ELITE_TAG = "guildsofverra_elite";
    private static final Set<String> SUPPORTED_TYPES = Set.of(
        "minecraft:zombie",
        "minecraft:drowned",
        "minecraft:skeleton",
        "minecraft:spider",
        "minecraft:creeper"
    );

    private EliteMobService() {}

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LivingEntity living)
                || living instanceof ServerPlayer
                || entity.tickCount > 1) {
                return;
            }

            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (!SUPPORTED_TYPES.contains(typeId) || !entity.addTag(CHECKED_TAG)) {
                return;
            }

            ServerPlayer nearest = level.players().stream()
                .min(Comparator.comparingDouble(entity::distanceToSqr))
                .orElse(null);
            if (nearest == null) {
                return;
            }

            int adventurer = ProfileManager.get(nearest).adventurerLevel();
            if (adventurer < 10) {
                return;
            }

            double chance = Math.min(0.05, 0.005 + (0.00045 * adventurer));
            if (level.getRandom().nextDouble() >= chance) {
                return;
            }

            switch (typeId) {
                case "minecraft:zombie" -> convert(living, "Tank Zombie", 1.18, 2.25, 0.78, 8.0, 0.65, 1.25);
                case "minecraft:drowned" -> convert(living, "Bulwark Drowned", 1.12, 2.0, 0.85, 7.0, 0.50, 1.15);
                case "minecraft:skeleton" -> convert(living, "Armoured Skeleton", 1.05, 1.75, 0.90, 8.0, 0.25, 1.10);
                case "minecraft:spider" -> convert(living, "Brute Spider", 1.20, 2.0, 0.90, 2.0, 0.35, 1.35);
                case "minecraft:creeper" -> convert(living, "Volatile Creeper", 1.10, 1.50, 0.95, 2.0, 0.20, 1.00);
                default -> { }
            }
        });
    }

    private static void convert(
        LivingEntity entity,
        String name,
        double scale,
        double health,
        double speed,
        double armor,
        double knockback,
        double damage
    ) {
        entity.addTag(ELITE_TAG);
        entity.setCustomName(Component.literal(name));
        entity.setCustomNameVisible(true);
        multiply(entity, Attributes.MAX_HEALTH, health);
        multiply(entity, Attributes.MOVEMENT_SPEED, speed);
        add(entity, Attributes.ARMOR, armor);
        add(entity, Attributes.KNOCKBACK_RESISTANCE, knockback);
        multiply(entity, Attributes.ATTACK_DAMAGE, damage);
        multiply(entity, Attributes.SCALE, scale);
        entity.setHealth(entity.getMaxHealth());
    }

    private static void multiply(
        LivingEntity entity,
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        double factor
    ) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() * factor);
        }
    }

    private static void add(
        LivingEntity entity,
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
        double value
    ) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() + value);
        }
    }
}
