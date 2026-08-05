package com.guildsofverra.mixin;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.passive.PassiveValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityAirSupplyMixin {
    @Inject(method = "getMaxAirSupply", at = @At("RETURN"), cancellable = true)
    private void guildsofverra$applyAirSupply(
        CallbackInfoReturnable<Integer> cir
    ) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        cir.setReturnValue(PassiveRuntimeMath.scaleDuration(
            cir.getReturnValueI(),
            PassiveValues.total(player, SkillId.EXPLORATION, "air_supply")
        ));
    }
}
