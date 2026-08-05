package com.guildsofverra.mixin;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.passive.PassiveValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
abstract class FishingHookPassiveMixin {
    @Shadow private int timeUntilLured;

    @Inject(method = "tick", at = @At("TAIL"))
    private void guildsofverra$accelerateFishingWait(CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        if (timeUntilLured <= 0 || !(hook.getOwner() instanceof ServerPlayer player)) {
            return;
        }
        double extraTickChance = PassiveRuntimeMath.extraFishingTickChance(
            PassiveValues.total(player, SkillId.FISHING, "fishing_wait")
        );
        if (extraTickChance > 0.0 && player.getRandom().nextDouble() < extraTickChance) {
            timeUntilLured--;
        }
    }
}
