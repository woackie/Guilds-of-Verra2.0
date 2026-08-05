package com.guildsofverra.mixin.accessor;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    @Accessor("saturationLevel")
    void guildsofverra$setSaturationLevel(float value);
}
