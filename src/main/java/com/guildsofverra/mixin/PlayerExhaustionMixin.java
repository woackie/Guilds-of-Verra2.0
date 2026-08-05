package com.guildsofverra.mixin;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.passive.PassiveValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerExhaustionMixin {
    @ModifyVariable(
        method = "causeFoodExhaustion",
        at = @At("HEAD"),
        argsOnly = true,
        name = "exhaustion"
    )
    private float guildsofverra$reduceTravelExhaustion(float exhaustion) {
        if (!((Object) this instanceof ServerPlayer player)) return exhaustion;
        return PassiveRuntimeMath.multiplyDamage(
            exhaustion,
            PassiveValues.total(player, SkillId.EXPLORATION, "travel_exhaustion")
        );
    }
}
