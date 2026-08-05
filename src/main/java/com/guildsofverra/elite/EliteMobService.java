package com.guildsofverra.elite;

import com.guildsofverra.config.EliteConfig;
import com.guildsofverra.data.ProfileManager;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EliteMobService {
    private static final String CHECKED_TAG = "guildsofverra_elite_checked";
    private static final String ELITE_TAG = "guildsofverra_elite";
    private static final String VARIANT_TAG_PREFIX = "guildsofverra_elite_variant_";
    private static final String REWARD_TAG_PREFIX = "guildsofverra_elite_reward_";
    private static final int REWARD_TAG_SCALE = 1_000;

    private static final List<EliteVariantDefinition> VARIANTS = List.of(
        new EliteVariantDefinition(
            "tank_zombie", "minecraft:zombie", "Tank Zombie",
            1.18, 2.25, 0.78, 8.0, 0.65, 1.25, 4.0
        ),
        new EliteVariantDefinition(
            "bulwark_drowned", "minecraft:drowned", "Bulwark Drowned",
            1.12, 2.0, 0.85, 7.0, 0.50, 1.15, 3.5
        ),
        new EliteVariantDefinition(
            "armoured_skeleton", "minecraft:skeleton", "Armoured Skeleton",
            1.05, 1.75, 0.90, 8.0, 0.25, 1.10, 3.5
        ),
        new EliteVariantDefinition(
            "marksman_skeleton", "minecraft:skeleton", "Marksman Skeleton",
            1.0, 1.40, 0.95, 2.0, 0.10, 1.30, 3.0
        ),
        new EliteVariantDefinition(
            "brute_spider", "minecraft:spider", "Brute Spider",
            1.20, 2.0, 0.90, 2.0, 0.35, 1.35, 3.5
        ),
        new EliteVariantDefinition(
            "venom_spider", "minecraft:spider", "Venom Spider",
            1.05, 1.50, 1.05, 0.0, 0.10, 1.15, 3.0
        ),
        new EliteVariantDefinition(
            "volatile_creeper", "minecraft:creeper", "Volatile Creeper",
            1.10, 1.50, 0.95, 2.0, 0.20, 1.0, 3.0
        )
    );

    private EliteMobService() {}

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            EliteConfig config = EliteConfig.current();
            if (!config.enabled
                || !(entity instanceof LivingEntity living)
                || living instanceof ServerPlayer
                || entity.tickCount > 1) {
                return;
            }

            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            List<EliteVariantDefinition> matching = VARIANTS.stream()
                .filter(variant -> variant.baseEntity().equals(typeId))
                .toList();
            if (matching.isEmpty() || !entity.addTag(CHECKED_TAG)) {
                return;
            }

            ServerPlayer nearest = level.players().stream()
                .min(Comparator.comparingDouble(entity::distanceToSqr))
                .orElse(null);
            if (nearest == null
                || nearest.distanceToSqr(entity) > config.regionRadius * config.regionRadius) {
                return;
            }

            int adventurer = ProfileManager.get(nearest).adventurerLevel();
            double chance = EliteSpawnRules.spawnChance(
                adventurer,
                config.minimumAdventurerLevel,
                config.baseSpawnChance,
                config.spawnChancePerAdventurerLevel,
                config.maximumSpawnChance
            );
            if (chance <= 0.0 || level.getRandom().nextDouble() >= chance) {
                return;
            }

            int nearbyElites = level.getEntitiesOfClass(
                LivingEntity.class,
                nearest.getBoundingBox().inflate(config.nearbyRadius),
                EliteMobService::isElite
            ).size();
            int regionalElites = level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(config.regionRadius),
                EliteMobService::isElite
            ).size();
            if (!EliteSpawnRules.withinBudgets(
                nearbyElites,
                config.nearbyEliteCap,
                regionalElites,
                config.regionEliteCap
            )) {
                return;
            }

            EliteVariantDefinition variant = matching.get(level.getRandom().nextInt(matching.size()));
            double statScale = EliteSpawnRules.statScale(
                adventurer,
                config.statScalingPerAdventurerLevel,
                config.maximumStatScaling
            );
            convert(living, variant, statScale, config.showEliteNames);
        });
    }

    public static boolean isElite(LivingEntity entity) {
        return entity.getTags().contains(ELITE_TAG);
    }

    public static String variantId(LivingEntity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith(VARIANT_TAG_PREFIX)) {
                return tag.substring(VARIANT_TAG_PREFIX.length());
            }
        }
        return "";
    }

    /**
     * Adjusts reward math because elite max health has already been multiplied.
     * Applying this to max-health-based XP produces the configured total XP multiplier.
     */
    public static double combatRewardAdjustment(LivingEntity entity) {
        for (String tag : entity.getTags()) {
            if (!tag.startsWith(REWARD_TAG_PREFIX)) {
                continue;
            }
            try {
                int scaled = Integer.parseInt(tag.substring(REWARD_TAG_PREFIX.length()));
                return Math.max(1.0, scaled / (double) REWARD_TAG_SCALE);
            } catch (NumberFormatException ignored) {
                return 1.0;
            }
        }
        return 1.0;
    }

    public static List<EliteVariantDefinition> variants() {
        return VARIANTS;
    }

    private static void convert(
        LivingEntity entity,
        EliteVariantDefinition variant,
        double statScale,
        boolean showName
    ) {
        entity.addTag(ELITE_TAG);
        entity.addTag(VARIANT_TAG_PREFIX + variant.id());
        entity.addTag(
            REWARD_TAG_PREFIX
                + Math.max(1, (int) Math.round(variant.rewardAdjustment() * REWARD_TAG_SCALE))
        );

        entity.setCustomName(Component.literal(variant.displayName()));
        entity.setCustomNameVisible(showName);
        multiply(entity, Attributes.MAX_HEALTH, variant.healthMultiplier() * statScale);
        multiply(entity, Attributes.MOVEMENT_SPEED, variant.speedMultiplier());
        add(entity, Attributes.ARMOR, variant.armorBonus());
        add(entity, Attributes.KNOCKBACK_RESISTANCE, variant.knockbackResistance());
        multiply(entity, Attributes.ATTACK_DAMAGE, variant.damageMultiplier() * statScale);
        multiply(entity, Attributes.SCALE, variant.scale());
        applyEquipment(entity, variant.id());
        entity.setHealth(entity.getMaxHealth());
    }

    private static void applyEquipment(LivingEntity entity, String variantId) {
        switch (variantId) {
            case "tank_zombie" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                equip(entity, EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            }
            case "bulwark_drowned" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            }
            case "armoured_skeleton" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                equip(entity, EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            }
            case "marksman_skeleton" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            default -> { }
        }
    }

    private static void equip(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        entity.setItemSlot(slot, stack);
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
