package com.guildsofverra.elite;

import com.guildsofverra.data.ProfileManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import java.util.Comparator;

public final class EliteMobService {
    private static final String ELITE_TAG = "guildsofverra_elite";
    private EliteMobService() {}
    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LivingEntity living) || living instanceof ServerPlayer || entity.getTags().contains(ELITE_TAG) || entity.tickCount > 1) return;
            ServerPlayer nearest = level.players().stream().min(Comparator.comparingDouble(entity::distanceToSqr)).orElse(null);
            if (nearest == null) return;
            int adventurer = ProfileManager.get(nearest).adventurerLevel();
            if (adventurer < 10) return;
            double chance = Math.min(0.05, 0.005 + (0.00045 * adventurer));
            if (level.random.nextDouble() >= chance) return;
            if (living instanceof Zombie && !(living instanceof Drowned)) convert(living, "Tank Zombie", 1.18, 2.25, 0.78, 8.0, 0.65, 1.25);
            else if (living instanceof Drowned) convert(living, "Bulwark Drowned", 1.12, 2.0, 0.85, 7.0, 0.50, 1.15);
            else if (living instanceof Skeleton) convert(living, "Armoured Skeleton", 1.05, 1.75, 0.90, 8.0, 0.25, 1.10);
            else if (living instanceof Spider) convert(living, "Brute Spider", 1.20, 2.0, 0.90, 2.0, 0.35, 1.35);
            else if (living instanceof Creeper) convert(living, "Volatile Creeper", 1.10, 1.50, 0.95, 2.0, 0.20, 1.00);
        });
    }

    private static void convert(LivingEntity entity, String name, double scale, double health, double speed, double armor, double knockback, double damage) {
        entity.addTag(ELITE_TAG); entity.setCustomName(Component.literal(name)); entity.setCustomNameVisible(true);
        multiply(entity, Attributes.MAX_HEALTH, health); multiply(entity, Attributes.MOVEMENT_SPEED, speed); add(entity, Attributes.ARMOR, armor); add(entity, Attributes.KNOCKBACK_RESISTANCE, knockback); multiply(entity, Attributes.ATTACK_DAMAGE, damage); multiply(entity, Attributes.SCALE, scale);
        entity.setHealth(entity.getMaxHealth());
    }
    private static void multiply(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double factor) { var instance = entity.getAttribute(attribute); if (instance != null) instance.setBaseValue(instance.getBaseValue() * factor); }
    private static void add(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) { var instance = entity.getAttribute(attribute); if (instance != null) instance.setBaseValue(instance.getBaseValue() + value); }
}
