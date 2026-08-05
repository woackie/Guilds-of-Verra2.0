package com.guildsofverra.elite;

import java.util.List;

/** Central registry for every vanilla-themed elite encounter. */
public final class EliteVariantRegistry {
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
        ),

        new EliteVariantDefinition(
            "plague_husk", "minecraft:husk", "Plague Husk",
            1.08, 1.70, 0.95, 2.0, 0.15, 1.20, 3.2
        ),
        new EliteVariantDefinition(
            "frostbound_stray", "minecraft:stray", "Frostbound Stray",
            1.02, 1.55, 0.95, 3.0, 0.15, 1.20, 3.3
        ),
        new EliteVariantDefinition(
            "hexbinder_witch", "minecraft:witch", "Hexbinder Witch",
            1.05, 1.85, 0.95, 4.0, 0.15, 1.0, 3.8
        ),
        new EliteVariantDefinition(
            "raid_captain_pillager", "minecraft:pillager", "Raid Captain Pillager",
            1.06, 1.65, 1.0, 5.0, 0.15, 1.25, 3.7
        ),
        new EliteVariantDefinition(
            "ironhide_ravager", "minecraft:ravager", "Ironhide Ravager",
            1.18, 2.0, 0.82, 10.0, 0.75, 1.35, 5.0
        ),
        new EliteVariantDefinition(
            "berserker_piglin", "minecraft:piglin_brute", "Berserker Piglin",
            1.08, 1.55, 1.15, 2.0, 0.15, 1.55, 4.0
        ),
        new EliteVariantDefinition(
            "ashen_wither_skeleton", "minecraft:wither_skeleton", "Ashen Wither Skeleton",
            1.08, 1.75, 1.0, 6.0, 0.30, 1.30, 4.1
        ),
        new EliteVariantDefinition(
            "magma_colossus", "minecraft:magma_cube", "Magma Colossus",
            1.35, 2.10, 0.85, 4.0, 0.70, 1.35, 4.3
        ),
        new EliteVariantDefinition(
            "voidstalker_enderman", "minecraft:enderman", "Voidstalker Enderman",
            1.10, 1.75, 1.12, 3.0, 0.35, 1.35, 4.5
        ),

        new EliteVariantDefinition(
            "sporeguard_bogged", "minecraft:bogged", "Sporeguard Bogged",
            1.06, 1.60, 0.92, 4.0, 0.20, 1.15, 3.4
        ),
        new EliteVariantDefinition(
            "cave_stalker", "minecraft:cave_spider", "Cave Stalker",
            1.12, 1.65, 1.12, 1.0, 0.15, 1.30, 3.6
        ),
        new EliteVariantDefinition(
            "tempest_breeze", "minecraft:breeze", "Tempest Breeze",
            1.12, 1.80, 1.10, 3.0, 0.30, 1.25, 4.0
        ),
        new EliteVariantDefinition(
            "cinder_blaze", "minecraft:blaze", "Cinder Blaze",
            1.12, 1.75, 1.0, 5.0, 0.25, 1.25, 4.0
        ),
        new EliteVariantDefinition(
            "soulreaver_ghast", "minecraft:ghast", "Soulreaver Ghast",
            1.18, 1.70, 0.92, 3.0, 0.20, 1.35, 4.4
        ),
        new EliteVariantDefinition(
            "end_sentinel_shulker", "minecraft:shulker", "End Sentinel Shulker",
            1.12, 1.90, 0.90, 8.0, 0.60, 1.20, 4.2
        ),
        new EliteVariantDefinition(
            "swarmheart_silverfish", "minecraft:silverfish", "Swarmheart Silverfish",
            1.28, 2.0, 1.10, 2.0, 0.30, 1.35, 3.8
        ),
        new EliteVariantDefinition(
            "abyssal_guardian", "minecraft:guardian", "Abyssal Guardian",
            1.15, 1.85, 0.92, 7.0, 0.55, 1.30, 4.3
        ),
        new EliteVariantDefinition(
            "dreadwing_phantom", "minecraft:phantom", "Dreadwing Phantom",
            1.22, 1.75, 1.15, 2.0, 0.20, 1.40, 4.0
        )
    );

    private EliteVariantRegistry() {}

    public static List<EliteVariantDefinition> all() {
        return VARIANTS;
    }

    public static List<EliteVariantDefinition> matching(String baseEntity, boolean inEnd) {
        return VARIANTS.stream()
            .filter(variant -> variant.baseEntity().equals(baseEntity))
            .filter(variant -> !variant.id().equals("voidstalker_enderman") || inEnd)
            .toList();
    }

    public static EliteVariantDefinition byId(String id) {
        return VARIANTS.stream()
            .filter(variant -> variant.id().equals(id))
            .findFirst()
            .orElse(null);
    }
}
