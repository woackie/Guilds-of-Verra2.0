package com.guildsofverra.core;

import java.util.function.DoubleSupplier;

/** Pure, clamped calculations shared by the passive gameplay hooks. */
public final class PassiveRuntimeMath {
    private PassiveRuntimeMath() {}

    public static double multiplier(double additiveBonus) {
        return Math.max(0.0, 1.0 + additiveBonus);
    }

    public static float multiplyDamage(float amount, double additiveBonus) {
        if (amount <= 0.0F) return 0.0F;
        return (float) Math.max(0.0, amount * multiplier(additiveBonus));
    }

    public static int scaleDuration(int ticks, double additiveBonus) {
        if (ticks <= 0) return 0;
        return Math.max(1, (int) Math.round(ticks * multiplier(additiveBonus)));
    }

    public static int preservedDurability(
        int requestedDamage,
        double preservationChance,
        DoubleSupplier random
    ) {
        int preserved = ProgressionRewardMath.bonusRolls(
            requestedDamage,
            preservationChance,
            random
        );
        return Math.max(0, requestedDamage - preserved);
    }

    /**
     * Converts a desired fractional wait reduction into an extra-decrement chance.
     * One normal decrement plus this probabilistic decrement produces the requested
     * average duration: for example, a 20% reduction becomes a 25% extra-tick chance.
     */
    public static double extraFishingTickChance(double waitTimeBonus) {
        double reduction = Math.max(0.0, Math.min(0.95, -waitTimeBonus));
        return Math.min(1.0, reduction / (1.0 - reduction));
    }
}
