package com.guildsofverra.elite;

import com.guildsofverra.config.EliteConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class EliteAbilityEvents {
    private EliteAbilityEvents() {}

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamageTaken, damageTaken, blocked) -> {
            EliteConfig config = EliteConfig.current();
            if (!config.specialAbilitiesEnabled
                || damageTaken <= 0.0F
                || !(source.getEntity() instanceof LivingEntity attacker)) {
                return;
            }

            switch (EliteMobService.variantId(attacker)) {
                case "venom_spider" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.POISON,
                    config.venomPoisonTicks
                );
                case "marksman_skeleton" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.SLOWNESS,
                    config.marksmanSlownessTicks
                );
                case "bulwark_drowned" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.WEAKNESS,
                    config.bulwarkWeaknessTicks
                );
                case "volatile_creeper" -> {
                    if (config.volatileFireSeconds > 0.0F) {
                        victim.igniteForSeconds(config.volatileFireSeconds);
                    }
                }
                default -> { }
            }
        });
    }

    private static void addEffect(
        LivingEntity victim,
        LivingEntity attacker,
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
        int durationTicks
    ) {
        if (durationTicks > 0) {
            victim.addEffect(new MobEffectInstance(effect, durationTicks, 0), attacker);
        }
    }
}
