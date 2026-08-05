package com.guildsofverra.passive;

import com.guildsofverra.core.PassiveRuntimeMath;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.mixin.accessor.FoodDataAccessor;
import com.guildsofverra.mixin.accessor.MobEffectInstanceAccessor;
import com.guildsofverra.restriction.GvTags;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

/** Tracks the narrow synchronous window in which a prepared food applies its effects. */
public final class PassiveFoodContext {
    private static final ThreadLocal<State> ACTIVE = new ThreadLocal<>();

    private PassiveFoodContext() {}

    public static void initialize() {
        ServerMobEffectEvents.BEFORE_ADD.register(
            (effect, entity, context) -> adjustEffect(effect, entity)
        );
    }

    public static void begin(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !stack.is(GvTags.PREPARED_FOODS)) {
            return;
        }
        ACTIVE.set(new State(
            player,
            player.getFoodData().getSaturationLevel(),
            PassiveValues.total(player, SkillId.COOKING, "meal_saturation"),
            PassiveValues.total(player, SkillId.COOKING, "positive_meal_duration"),
            PassiveValues.total(player, SkillId.COOKING, "negative_food_duration")
        ));
    }

    public static void finish(LivingEntity entity) {
        State state = ACTIVE.get();
        if (state == null || state.player() != entity) return;
        try {
            FoodData food = state.player().getFoodData();
            float after = food.getSaturationLevel();
            float gained = Math.max(0.0F, after - state.saturationBefore());
            if (gained > 0.0F && state.saturationBonus() > 0.0) {
                float adjusted = Math.min(
                    food.getFoodLevel(),
                    after + (float) (gained * state.saturationBonus())
                );
                ((FoodDataAccessor) food).guildsofverra$setSaturationLevel(adjusted);
            }
        } finally {
            ACTIVE.remove();
        }
    }

    public static int eatingDuration(ItemStack stack, LivingEntity entity, int original) {
        if (!(entity instanceof ServerPlayer player) || !stack.is(GvTags.PREPARED_FOODS)) {
            return original;
        }
        return PassiveRuntimeMath.scaleDuration(
            original,
            PassiveValues.total(player, SkillId.COOKING, "eating_duration")
        );
    }

    private static void adjustEffect(MobEffectInstance effect, LivingEntity entity) {
        State state = ACTIVE.get();
        if (state == null || state.player() != entity || effect.getDuration() <= 1) {
            return;
        }
        double bonus = effect.getEffect().value().isBeneficial()
            ? state.positiveDurationBonus()
            : state.negativeDurationBonus();
        if (Math.abs(bonus) <= 0.0000001) return;
        ((MobEffectInstanceAccessor) (Object) effect).guildsofverra$setDuration(
            PassiveRuntimeMath.scaleDuration(effect.getDuration(), bonus)
        );
    }

    private record State(
        ServerPlayer player,
        float saturationBefore,
        double saturationBonus,
        double positiveDurationBonus,
        double negativeDurationBonus
    ) {}
}
