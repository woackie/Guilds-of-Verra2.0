package com.guildsofverra.event;

import com.guildsofverra.api.GuildsOfVerraApi;
import com.guildsofverra.core.ProgressionChange;
import com.guildsofverra.core.SkillId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ProgressionEvents {
    private static final Map<String, Integer> MINING_XP = defaultMiningXp();
    private static final Map<UUID, Long> LAST_EXPLORATION_CELL = new HashMap<>();

    private ProgressionEvents() {}

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                int xp = MINING_XP.getOrDefault(id, 0);
                if (xp > 0) {
                    notifyChange(serverPlayer, GuildsOfVerraApi.awardXp(serverPlayer, SkillId.MINING, xp));
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (damageTaken > 0 && source.getEntity() instanceof ServerPlayer player && entity != player) {
                long xp = Math.max(1L, Math.round(damageTaken * 3.0));
                notifyChange(player, GuildsOfVerraApi.awardXp(player, SkillId.COMBAT, xp));
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (source.getEntity() instanceof ServerPlayer player && entity != player) {
                long bonus = Math.max(1L, Math.round(entity.getMaxHealth() * 2.0));
                notifyChange(player, GuildsOfVerraApi.awardXp(player, SkillId.COMBAT, bonus));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                long cellX = Math.floorDiv(player.getBlockX(), 128);
                long cellZ = Math.floorDiv(player.getBlockZ(), 128);
                long key = (cellX & 0xffffffffL) << 32 | (cellZ & 0xffffffffL);
                Long previous = LAST_EXPLORATION_CELL.put(player.getUUID(), key);
                if (previous != null && previous.longValue() != key) {
                    notifyChange(player, GuildsOfVerraApi.awardXp(player, SkillId.EXPLORATION, 125));
                }
            }
        });
    }

    private static void notifyChange(ServerPlayer player, ProgressionChange change) {
        if (change.leveledUp()) {
            player.sendSystemMessage(Component.literal(
                change.skill().serializedName() + " reached level " + change.newLevel() + "!"
            ));
        }
    }

    private static Map<String, Integer> defaultMiningXp() {
        Map<String, Integer> map = new HashMap<>();
        map.put("minecraft:stone", 1);
        map.put("minecraft:deepslate", 1);
        map.put("minecraft:coal_ore", 8);
        map.put("minecraft:deepslate_coal_ore", 8);
        map.put("minecraft:copper_ore", 10);
        map.put("minecraft:deepslate_copper_ore", 10);
        map.put("minecraft:iron_ore", 25);
        map.put("minecraft:deepslate_iron_ore", 25);
        map.put("minecraft:gold_ore", 35);
        map.put("minecraft:deepslate_gold_ore", 35);
        map.put("minecraft:redstone_ore", 18);
        map.put("minecraft:deepslate_redstone_ore", 18);
        map.put("minecraft:lapis_ore", 18);
        map.put("minecraft:deepslate_lapis_ore", 18);
        map.put("minecraft:diamond_ore", 120);
        map.put("minecraft:deepslate_diamond_ore", 120);
        map.put("minecraft:emerald_ore", 150);
        map.put("minecraft:deepslate_emerald_ore", 150);
        map.put("minecraft:ancient_debris", 300);
        return Map.copyOf(map);
    }
}
