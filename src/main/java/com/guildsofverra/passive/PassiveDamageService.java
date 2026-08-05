package com.guildsofverra.passive;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

/** Damage multipliers resolved at the authoritative LivingEntity damage entrypoint. */
public final class PassiveDamageService {
    private static final int UNDERGROUND_MAXIMUM_Y = 60;

    private PassiveDamageService() {}

    public static float modify(LivingEntity victim, DamageSource source, float amount) {
        float modified = applyOutgoing(source, amount);
        if (!(victim instanceof ServerPlayer player)) {
            return modified;
        }

        modified = PassiveRuntimeMath.multiplyDamage(
            modified,
            PassiveValues.total(player, SkillId.COMBAT, "incoming_damage")
        );
        if (source.is(DamageTypeTags.IS_FALL)) {
            modified = PassiveRuntimeMath.multiplyDamage(
                modified,
                PassiveValues.total(player, SkillId.EXPLORATION, "fall_damage")
            );
        }
        if (isUnderground(player)
            && (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_EXPLOSION))) {
            modified = PassiveRuntimeMath.multiplyDamage(
                modified,
                PassiveValues.total(player, SkillId.MINING, "underground_damage")
            );
        }
        return modified;
    }

    private static float applyOutgoing(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return amount;
        }

        Entity direct = source.getDirectEntity();
        if (direct instanceof AbstractArrow) {
            return PassiveRuntimeMath.multiplyDamage(
                amount,
                PassiveValues.total(player, SkillId.COMBAT, "projectile_damage")
            );
        }
        if (direct != player) {
            return amount;
        }

        ItemStack weapon = player.getMainHandItem();
        if (weapon.is(ItemTags.SWORDS)) {
            return PassiveRuntimeMath.multiplyDamage(
                amount,
                PassiveValues.total(player, SkillId.COMBAT, "sword_damage")
            );
        }
        if (weapon.is(ItemTags.AXES)) {
            return PassiveRuntimeMath.multiplyDamage(
                amount,
                PassiveValues.total(player, SkillId.COMBAT, "axe_damage")
            );
        }
        return amount;
    }

    private static boolean isUnderground(ServerPlayer player) {
        return player.level() instanceof ServerLevel level
            && player.getBlockY() <= UNDERGROUND_MAXIMUM_Y
            && !level.canSeeSky(player.blockPosition());
    }
}
