package com.guildsofverra.mixin;

import com.guildsofverra.passive.PassiveDurabilityService;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
abstract class ItemStackDurabilityMixin {
    @ModifyVariable(
        method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
        at = @At("HEAD"),
        argsOnly = true,
        name = "amount"
    )
    private int guildsofverra$preserveDurability(
        int amount,
        LivingEntity entity,
        EquipmentSlot slot
    ) {
        return PassiveDurabilityService.modify(
            (ItemStack) (Object) this,
            amount,
            entity
        );
    }
}
