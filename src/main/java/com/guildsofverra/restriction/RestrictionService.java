package com.guildsofverra.restriction;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.core.RequirementService;
import net.minecraft.world.item.ItemStack;

/**
 * Context-aware hard-lock checks. Axes are deliberately split: their block-breaking
 * function belongs to Mining, while attacking with them belongs to Combat.
 */
public final class RestrictionService {
    private RestrictionService() {}

    public static RequirementResult canUseAsTool(PlayerProfile profile, ItemStack stack) {
        if (stack.isEmpty()) return RequirementResult.allow();
        if (stack.is(GvTags.IRON_TOOLS)) return RequirementService.requireNode(profile, "mining:iron_tool_mastery");
        if (stack.is(GvTags.DIAMOND_TOOLS)) return RequirementService.requireNode(profile, "mining:diamond_tool_mastery");
        if (stack.is(GvTags.NETHERITE_TOOLS)) return RequirementService.requireNode(profile, "mining:netherite_tool_mastery");
        return RequirementResult.allow();
    }

    public static RequirementResult canAttack(PlayerProfile profile, ItemStack stack) {
        if (stack.isEmpty()) return RequirementResult.allow();
        if (stack.is(GvTags.IRON_SWORDS)) return RequirementService.requireNode(profile, "combat:iron_blade_training");
        if (stack.is(GvTags.DIAMOND_SWORDS)) return RequirementService.requireNode(profile, "combat:diamond_blade_training");
        if (stack.is(GvTags.NETHERITE_SWORDS)) return RequirementService.requireNode(profile, "combat:netherite_blade_training");
        if (stack.is(GvTags.IRON_AXES)) return RequirementService.requireNode(profile, "combat:iron_axe_combat");
        if (stack.is(GvTags.DIAMOND_AXES)) return RequirementService.requireNode(profile, "combat:diamond_axe_combat");
        if (stack.is(GvTags.NETHERITE_AXES)) return RequirementService.requireNode(profile, "combat:netherite_axe_combat");
        return RequirementResult.allow();
    }

    public static RequirementResult canActivate(PlayerProfile profile, ItemStack stack) {
        if (stack.isEmpty()) return RequirementResult.allow();
        if (stack.is(GvTags.BOWS)) return RequirementService.requireNode(profile, "combat:bow_training");
        if (stack.is(GvTags.CROSSBOWS)) return RequirementService.requireNode(profile, "combat:crossbow_training");
        if (stack.is(GvTags.SHIELDS)) return RequirementService.requireNode(profile, "combat:shield_training");
        return RequirementResult.allow();
    }

    public static RequirementResult canEquip(PlayerProfile profile, ItemStack stack) {
        if (stack.isEmpty()) return RequirementResult.allow();
        if (stack.is(GvTags.IRON_ARMOR)) return RequirementService.requireNode(profile, "combat:iron_armour_training");
        if (stack.is(GvTags.DIAMOND_ARMOR)) return RequirementService.requireNode(profile, "combat:diamond_armour_training");
        if (stack.is(GvTags.NETHERITE_ARMOR)) return RequirementService.requireNode(profile, "combat:netherite_armour_training");
        if (stack.is(GvTags.ELYTRA)) return RequirementService.requireNode(profile, "exploration:elytra_certification");
        return RequirementResult.allow();
    }
}
