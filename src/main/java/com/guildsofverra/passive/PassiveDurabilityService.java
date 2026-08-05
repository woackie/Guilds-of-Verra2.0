package com.guildsofverra.passive;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.restriction.GvTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Reduces vanilla durability requests before Unbreaking and break handling run. */
public final class PassiveDurabilityService {
    private PassiveDurabilityService() {}

    public static int modify(ItemStack stack, int amount, LivingEntity entity) {
        if (amount <= 0 || !(entity instanceof ServerPlayer player)) {
            return amount;
        }

        double chance = 0.0;
        if (stack.is(Items.FISHING_ROD)) {
            chance = PassiveValues.chance(
                player,
                SkillId.FISHING,
                "fishing_durability"
            );
        } else if (stack.is(GvTags.SHIELDS)) {
            chance = Math.max(0.0, -PassiveValues.total(
                player,
                SkillId.COMBAT,
                "shield_durability"
            ));
        } else if (isMiningTool(stack)) {
            chance = PassiveValues.chance(
                player,
                SkillId.MINING,
                "tool_durability"
            );
        }
        return PassiveRuntimeMath.preservedDurability(
            amount,
            chance,
            player.getRandom()::nextDouble
        );
    }

    private static boolean isMiningTool(ItemStack stack) {
        return stack.is(ItemTags.PICKAXES)
            || stack.is(ItemTags.AXES)
            || stack.is(ItemTags.SHOVELS)
            || stack.is(ItemTags.HOES);
    }
}
