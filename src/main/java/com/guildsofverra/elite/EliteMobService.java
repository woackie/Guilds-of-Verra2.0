package com.guildsofverra.elite;

import com.guildsofverra.config.EliteConfig;
import com.guildsofverra.data.ProfileManager;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class EliteMobService {
    private static final String CHECKED_TAG = "guildsofverra_elite_checked";
    private static final String ELITE_TAG = "guildsofverra_elite";
    private static final String VARIANT_TAG_PREFIX = "guildsofverra_elite_variant_";

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
            List<EliteVariantDefinition> matching = EliteVariantRegistry.matching(
                typeId,
                Level.END.equals(level.dimension())
            );
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
        return hasTag(entity, ELITE_TAG);
    }

    public static String variantId(LivingEntity entity) {
        for (EliteVariantDefinition variant : EliteVariantRegistry.all()) {
            if (hasTag(entity, VARIANT_TAG_PREFIX + variant.id())) {
                return variant.id();
            }
        }
        return "";
    }

    public static EliteVariantDefinition variant(String id) {
        return EliteVariantRegistry.byId(id);
    }

    public static String discoveryId(String variantId) {
        return variantId == null || variantId.isBlank()
            ? ""
            : "guildsofverra:" + variantId;
    }

    /**
     * Adjusts reward math because elite max health has already been multiplied.
     * Applying this to max-health-based XP produces the configured total XP multiplier.
     */
    public static double combatRewardAdjustment(LivingEntity entity) {
        EliteVariantDefinition variant = variant(variantId(entity));
        if (variant == null) {
            return 1.0;
        }
        return variant.rewardAdjustment() * EliteConfig.current().combatXpRewardScale;
    }

    public static List<EliteVariantDefinition> variants() {
        return EliteVariantRegistry.all();
    }

    private static boolean hasTag(Entity entity, String tag) {
        if (!entity.addTag(tag)) {
            return true;
        }
        entity.removeTag(tag);
        return false;
    }

    private static void convert(
        LivingEntity entity,
        EliteVariantDefinition variant,
        double statScale,
        boolean showName
    ) {
        entity.addTag(ELITE_TAG);
        entity.addTag(VARIANT_TAG_PREFIX + variant.id());
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
            case "plague_husk" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                equip(entity, EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            }
            case "frostbound_stray" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            case "raid_captain_pillager" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                equip(entity, EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
            }
            case "berserker_piglin" -> {
                equip(entity, EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
            }
            case "ashen_wither_skeleton" -> {
                equip(entity, EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                equip(entity, EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
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
