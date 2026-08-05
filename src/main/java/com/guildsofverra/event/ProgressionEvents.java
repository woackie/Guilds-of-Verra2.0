package com.guildsofverra.event;

import com.guildsofverra.api.GuildsOfVerraApi;
import com.guildsofverra.config.EliteConfig;
import com.guildsofverra.config.ProgressionConfig;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PassiveBonusService;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.ProgressionChange;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.data.ProfileManager;
import com.guildsofverra.elite.EliteMobService;
import com.guildsofverra.elite.EliteVariantDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ProgressionEvents {
    private static final Map<UUID, Long> LAST_EXPLORATION_CELL = new HashMap<>();

    private ProgressionEvents() {}

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            ProgressionConfig config = ProgressionConfig.current();
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            long xp = config.miningXp(id);
            if (xp <= 0) return;

            if (isOre(id)) {
                PlayerProfile profile = ProfileManager.get(serverPlayer);
                double oreBonus = PassiveBonusService.total(
                    profile,
                    SkillId.MINING,
                    GvContent.tree(SkillId.MINING),
                    "ore_xp"
                );
                xp = PassiveBonusService.applyPositiveMultiplier(xp, oreBonus);
            }
            awardAndNotify(serverPlayer, SkillId.MINING, xp, "Mined " + id, false);
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (damageTaken <= 0 || !(source.getEntity() instanceof ServerPlayer player) || entity == player) return;
            long xp = Math.max(1L, Math.round(
                damageTaken * ProgressionConfig.current().combatDamageXpMultiplier
            ));
            awardAndNotify(player, SkillId.COMBAT, xp, "Combat damage", false);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getEntity() instanceof ServerPlayer player) || entity == player) return;

            boolean elite = EliteMobService.isElite(entity);
            if (elite) {
                unlockEliteDiscovery(player, entity);
            }

            double rewardAdjustment = EliteMobService.combatRewardAdjustment(entity);
            long bonus = Math.max(1L, Math.round(
                entity.getMaxHealth()
                    * ProgressionConfig.current().combatKillHealthXpMultiplier
                    * rewardAdjustment
            ));
            awardAndNotify(
                player,
                SkillId.COMBAT,
                bonus,
                elite ? "Elite defeated" : "Enemy defeated",
                false
            );
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            ProgressionConfig config = ProgressionConfig.current();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                long cellX = Math.floorDiv(player.getBlockX(), config.explorationCellSize);
                long cellZ = Math.floorDiv(player.getBlockZ(), config.explorationCellSize);
                long key = (cellX & 0xffffffffL) << 32 | (cellZ & 0xffffffffL);
                Long previous = LAST_EXPLORATION_CELL.put(player.getUUID(), key);
                if (previous != null && previous.longValue() != key && config.explorationCellXp > 0) {
                    awardAndNotify(
                        player,
                        SkillId.EXPLORATION,
                        config.explorationCellXp,
                        "New exploration area",
                        false
                    );
                }
            }
            StatProgressionTracker.tick(server.getPlayerList().getPlayers());
        });
    }

    private static void unlockEliteDiscovery(ServerPlayer player, LivingEntity elite) {
        String variantId = EliteMobService.variantId(elite);
        String discoveryId = EliteMobService.discoveryId(variantId);
        PlayerProfile profile = ProfileManager.get(player);
        if (discoveryId.isBlank() || profile.discoveries().contains(discoveryId)) {
            return;
        }

        EliteVariantDefinition variant = EliteMobService.variant(variantId);
        String displayName = variant == null ? variantId : variant.displayName();
        ProfileManager.update(player, profile -> profile.withDiscovery(discoveryId));

        EliteConfig config = EliteConfig.current();
        if (config.announceFirstDiscovery) {
            player.sendSystemMessage(Component.literal(
                "Bestiary discovery — " + displayName
            ));
        }
        if (config.firstDiscoveryExplorationXp > 0) {
            double discoveryBonus = PassiveBonusService.total(
                profile,
                SkillId.EXPLORATION,
                GvContent.tree(SkillId.EXPLORATION),
                "discovery_xp"
            );
            awardAndNotify(
                player,
                SkillId.EXPLORATION,
                PassiveBonusService.applyPositiveMultiplier(
                    config.firstDiscoveryExplorationXp,
                    discoveryBonus
                ),
                "First elite encounter: " + displayName,
                true
            );
        }
    }

    static ProgressionChange awardAndNotify(
        ServerPlayer player,
        SkillId skill,
        long xp,
        String detail,
        boolean activityMessage
    ) {
        ProgressionChange change = GuildsOfVerraApi.awardXp(player, skill, xp);
        ProgressionConfig config = ProgressionConfig.current();
        if (activityMessage
            && config.showActivityXpMessages
            && change.awardedXp() >= config.minimumActivityXpMessage) {
            player.sendSystemMessage(Component.literal(
                "+" + change.awardedXp() + " " + displayName(skill) + " XP — " + detail
            ));
        }
        if (change.leveledUp() && config.showLevelUpMessages) {
            player.sendSystemMessage(Component.literal(
                displayName(change.skill()) + " reached level " + change.newLevel() + "!"
            ));
        }
        return change;
    }

    private static boolean isOre(String blockId) {
        return blockId.endsWith("_ore") || blockId.contains("deepslate_") || blockId.equals("minecraft:ancient_debris");
    }

    private static String displayName(SkillId skill) {
        String raw = skill.serializedName();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
