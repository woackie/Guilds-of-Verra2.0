package com.guildsofverra.restriction;

import com.guildsofverra.GuildsOfVerra;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class GvTags {
    private GvTags() {}
    private static TagKey<Item> item(String id) { return TagKey.create(Registries.ITEM, GuildsOfVerra.id(id)); }
    public static final TagKey<Item> IRON_TOOLS = item("tools/iron_tier");
    public static final TagKey<Item> DIAMOND_TOOLS = item("tools/diamond_tier");
    public static final TagKey<Item> NETHERITE_TOOLS = item("tools/netherite_tier");
    public static final TagKey<Item> IRON_ARMOR = item("armor/iron_tier");
    public static final TagKey<Item> DIAMOND_ARMOR = item("armor/diamond_tier");
    public static final TagKey<Item> NETHERITE_ARMOR = item("armor/netherite_tier");
    public static final TagKey<Item> IRON_SWORDS = item("weapons/swords/iron_tier");
    public static final TagKey<Item> DIAMOND_SWORDS = item("weapons/swords/diamond_tier");
    public static final TagKey<Item> NETHERITE_SWORDS = item("weapons/swords/netherite_tier");
    public static final TagKey<Item> IRON_AXES = item("weapons/axes/iron_tier");
    public static final TagKey<Item> DIAMOND_AXES = item("weapons/axes/diamond_tier");
    public static final TagKey<Item> NETHERITE_AXES = item("weapons/axes/netherite_tier");
    public static final TagKey<Item> BOWS = item("weapons/bows");
    public static final TagKey<Item> CROSSBOWS = item("weapons/crossbows");
    public static final TagKey<Item> SHIELDS = item("shields/basic");
    public static final TagKey<Item> ELYTRA = item("exploration/elytra");
    public static final TagKey<Item> PREPARED_FOODS = item("foods/prepared");
    public static final TagKey<Item> COMMON_BONUS_RESOURCES = item("mining/common_bonus_resources");
}
