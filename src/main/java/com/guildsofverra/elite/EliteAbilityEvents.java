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
                case "volatile_creeper" -> ignite(victim, config.volatileFireSeconds);

                case "plague_husk" -> {
                    addEffect(victim, attacker, MobEffects.HUNGER, config.plagueHungerTicks);
                    addEffect(victim, attacker, MobEffects.POISON, config.plaguePoisonTicks);
                }
                case "frostbound_stray" -> {
                    addEffect(
                        victim,
                        attacker,
                        MobEffects.SLOWNESS,
                        config.frostboundSlownessTicks
                    );
                    if (config.frostboundFreezeTicks > 0) {
                        victim.setTicksFrozen(Math.max(
                            victim.getTicksFrozen(),
                            config.frostboundFreezeTicks
                        ));
                    }
                }
                case "hexbinder_witch" -> {
                    addEffect(
                        victim,
                        attacker,
                        MobEffects.WEAKNESS,
                        config.hexbinderWeaknessTicks
                    );
                    addEffect(
                        victim,
                        attacker,
                        MobEffects.DARKNESS,
                        config.hexbinderDarknessTicks
                    );
                }
                case "raid_captain_pillager" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.WEAKNESS,
                    config.raidCaptainWeaknessTicks
                );
                case "ironhide_ravager" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.SLOWNESS,
                    config.ironhideSlownessTicks
                );
                case "ashen_wither_skeleton" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.WITHER,
                    config.ashenWitherTicks
                );
                case "magma_colossus" -> ignite(victim, config.magmaFireSeconds);
                case "voidstalker_enderman" -> addEffect(
                    victim,
                    attacker,
                    MobEffects.BLINDNESS,
                    config.voidstalkerBlindnessTicks
                );
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

    private static void ignite(LivingEntity victim, float seconds) {
        if (seconds > 0.0F) {
            victim.igniteForSeconds(seconds);
        }
    }
}
