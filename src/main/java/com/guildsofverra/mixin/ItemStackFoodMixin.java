package com.guildsofverra.mixin;

import com.guildsofverra.passive.PassiveFoodContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackFoodMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void guildsofverra$beginPreparedFood(
        Level level,
        LivingEntity entity,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        PassiveFoodContext.begin((ItemStack) (Object) this, entity);
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void guildsofverra$finishPreparedFood(
        Level level,
        LivingEntity entity,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        PassiveFoodContext.finish(entity);
    }

    @Inject(method = "getUseDuration", at = @At("RETURN"), cancellable = true)
    private void guildsofverra$shortenPreparedFoodUse(
        LivingEntity entity,
        CallbackInfoReturnable<Integer> cir
    ) {
        cir.setReturnValue(PassiveFoodContext.eatingDuration(
            (ItemStack) (Object) this,
            entity,
            cir.getReturnValueI()
        ));
    }
}
