package com.guildsofverra.mixin;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.passive.PassiveDamageService;
import com.guildsofverra.passive.PassiveValues;
import com.guildsofverra.restriction.GvTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityPassiveMixin {
    @ModifyVariable(
        method = "hurtServer",
        at = @At("HEAD"),
        argsOnly = true,
        name = "amount"
    )
    private float guildsofverra$applyPassiveDamage(
        float amount,
        ServerLevel level,
        DamageSource source
    ) {
        return PassiveDamageService.modify((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "getTicksUsingItem", at = @At("RETURN"), cancellable = true)
    private void guildsofverra$accelerateRangedUse(
        CallbackInfoReturnable<Integer> cir
    ) {
        if (!((Object) this instanceof ServerPlayer player)
            || (!player.getUseItem().is(GvTags.BOWS)
                && !player.getUseItem().is(GvTags.CROSSBOWS))) {
            return;
        }
        double rangedSpeed = Math.max(0.0, PassiveValues.total(
            player,
            SkillId.COMBAT,
            "projectile_damage"
        ) * (5.0 / 3.0));
        cir.setReturnValue(PassiveRuntimeMath.scaleDuration(
            cir.getReturnValueI(),
            rangedSpeed
        ));
    }
}
