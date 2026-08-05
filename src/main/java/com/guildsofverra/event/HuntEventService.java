package com.guildsofverra.event;

import com.guildsofverra.config.HuntEventConfig;
import com.guildsofverra.data.ProfileManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * Schedules bounded packs outside a player's immediate area and continuously retargets them
 * so the group actively pursues its selected player.
 */
public final class HuntEventService {
    private static final String HUNT_MEMBER_TAG = "guildsofverra_hunt_member";
    private static final Map<UUID, PendingHunt> PENDING = new HashMap<>();
    private static final Map<UUID, ActiveHunt> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new HashMap<>();
    private static final Set<UUID> SPAWNING_MEMBERS = new HashSet<>();

    private static final List<EntityType<? extends Mob>> OVERWORLD_POOL = List.of(
        EntityTypes.ZOMBIE,
        EntityTypes.SKELETON,
        EntityTypes.SPIDER,
        EntityTypes.HUSK,
        EntityTypes.STRAY,
        EntityTypes.PILLAGER,
        EntityTypes.CAVE_SPIDER
    );
    private static final List<EntityType<? extends Mob>> NETHER_POOL = List.of(
        EntityTypes.WITHER_SKELETON,
        EntityTypes.PIGLIN_BRUTE,
        EntityTypes.MAGMA_CUBE,
        EntityTypes.BLAZE
    );
    private static final List<EntityType<? extends Mob>> END_POOL = List.of(
        EntityTypes.ENDERMAN,
        EntityTypes.ENDERMITE
    );

    private HuntEventService() {}

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!isHuntMember(entity)) {
                return;
            }
            if (SPAWNING_MEMBERS.remove(entity.getUUID())) {
                return;
            }
            if (!isTrackedMember(entity.getUUID())) {
                entity.discard();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(HuntEventService::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(HuntEventService::clearAll);
    }

    public static boolean isHuntMember(Entity entity) {
        return hasTag(entity, HUNT_MEMBER_TAG);
    }

    /** Schedules a targeted hunt while bypassing natural chance, level and cooldown checks. */
    public static boolean startNow(ServerPlayer player) {
        HuntEventConfig config = HuntEventConfig.current();
        UUID playerId = player.getUUID();
        if (!config.enabled
            || !player.isAlive()
            || player.isSpectator()
            || PENDING.containsKey(playerId)
            || ACTIVE.containsKey(playerId)
            || !(player.level() instanceof ServerLevel level)
            || pool(level.dimension()).isEmpty()) {
            return false;
        }

        int packSize = HuntEventRules.packSize(
            ProfileManager.get(player).adventurerLevel(),
            config.minimumAdventurerLevel,
            config.minimumPackSize,
            config.adventurerLevelsPerAdditionalMob,
            config.maximumPackSize
        );
        long spawnAt = level.getServer().getTickCount()
            + (long) config.warningSeconds * 20L;
        PENDING.put(
            playerId,
            new PendingHunt(playerId, level.dimension(), spawnAt, packSize, true)
        );

        if (config.announceEvents) {
            player.sendSystemMessage(Component.literal(
                "A hostile pack has caught your trail. Something is gathering nearby..."
            ));
        }
        return true;
    }

    /** Stops a pending or active hunt for one player and removes its remaining members. */
    public static boolean stopNow(MinecraftServer server, ServerPlayer player) {
        UUID playerId = player.getUUID();
        boolean stopped = PENDING.remove(playerId) != null;
        ActiveHunt hunt = ACTIVE.remove(playerId);
        if (hunt != null) {
            discardMembers(server.getLevel(hunt.dimension()), hunt.members());
            stopped = true;
        }
        return stopped;
    }

    public static String status(MinecraftServer server, ServerPlayer player) {
        PendingHunt pending = PENDING.get(player.getUUID());
        if (pending != null) {
            long seconds = Math.max(
                0L,
                (pending.spawnAtTick() - server.getTickCount() + 19L) / 20L
            );
            return "Hunt pending for " + player.getName().getString()
                + " (pack arrives in " + seconds + "s)";
        }

        ActiveHunt hunt = ACTIVE.get(player.getUUID());
        if (hunt != null) {
            long seconds = Math.max(
                0L,
                (hunt.expiresAtTick() - server.getTickCount() + 19L) / 20L
            );
            return "Hunt active for " + player.getName().getString()
                + " (" + hunt.members().size() + " hunters, " + seconds + "s remaining)";
        }
        return "No hunt is pending or active for " + player.getName().getString() + ".";
    }

    private static boolean isTrackedMember(UUID memberId) {
        return ACTIVE.values().stream().anyMatch(hunt -> hunt.members().contains(memberId));
    }

    private static void tick(MinecraftServer server) {
        HuntEventConfig config = HuntEventConfig.current();
        if (!config.enabled) {
            if (!PENDING.isEmpty() || !ACTIVE.isEmpty()) {
                clearAll(server);
            }
            return;
        }

        long tick = server.getTickCount();
        processPending(server, tick, config);

        if (tick % config.retargetIntervalTicks == 0L) {
            maintainActive(server, tick, config);
        }
        long checkIntervalTicks = (long) config.checkIntervalSeconds * 20L;
        if (tick % checkIntervalTicks == 0L) {
            scheduleEligiblePlayers(server, tick, config);
            COOLDOWN_UNTIL.entrySet().removeIf(entry -> entry.getValue() + 1_200L < tick);
        }
    }

    private static void scheduleEligiblePlayers(
        MinecraftServer server,
        long tick,
        HuntEventConfig config
    ) {
        int occupiedSlots = PENDING.size() + ACTIVE.size();
        if (occupiedSlots >= config.globalActiveHuntCap) {
            return;
        }

        List<ServerPlayer> candidates = new ArrayList<>(server.getPlayerList().getPlayers());
        java.util.Collections.shuffle(candidates);
        for (ServerPlayer player : candidates) {
            if (occupiedSlots >= config.globalActiveHuntCap) {
                break;
            }
            if (!(player.level() instanceof ServerLevel level) || !dimensionAllowed(level, config)) {
                continue;
            }

            UUID playerId = player.getUUID();
            int adventurerLevel = ProfileManager.get(player).adventurerLevel();
            boolean eligible = HuntEventRules.eligible(
                adventurerLevel,
                config.minimumAdventurerLevel,
                player.isAlive(),
                player.isSpectator(),
                player.isCreative(),
                config.allowCreativePlayers,
                PENDING.containsKey(playerId) || ACTIVE.containsKey(playerId),
                tick,
                COOLDOWN_UNTIL.getOrDefault(playerId, 0L),
                occupiedSlots,
                config.globalActiveHuntCap
            );
            if (!eligible
                || !HuntEventRules.shouldTrigger(
                    level.getRandom().nextDouble(),
                    config.triggerChancePerCheck
                )) {
                continue;
            }

            int packSize = HuntEventRules.packSize(
                adventurerLevel,
                config.minimumAdventurerLevel,
                config.minimumPackSize,
                config.adventurerLevelsPerAdditionalMob,
                config.maximumPackSize
            );
            long spawnAt = tick + (long) config.warningSeconds * 20L;
            PENDING.put(
                playerId,
                new PendingHunt(playerId, level.dimension(), spawnAt, packSize, false)
            );
            COOLDOWN_UNTIL.put(
                playerId,
                tick + HuntEventRules.cooldownTicks(config.playerCooldownMinutes)
            );
            occupiedSlots++;

            if (config.announceEvents) {
                player.sendSystemMessage(Component.literal(
                    "A hostile pack has caught your trail. Something is gathering nearby..."
                ));
            }
        }
    }

    private static void processPending(
        MinecraftServer server,
        long tick,
        HuntEventConfig config
    ) {
        Iterator<Map.Entry<UUID, PendingHunt>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingHunt> entry = iterator.next();
            PendingHunt pending = entry.getValue();
            if (tick < pending.spawnAtTick()) {
                continue;
            }
            iterator.remove();

            ServerPlayer player = server.getPlayerList().getPlayer(pending.targetPlayer());
            if (player == null
                || !player.isAlive()
                || player.isSpectator()
                || (!pending.forced() && !config.allowCreativePlayers && player.isCreative())
                || !(player.level() instanceof ServerLevel level)
                || !pending.dimension().equals(level.dimension())) {
                continue;
            }

            Set<UUID> members = spawnPack(level, player, pending.packSize(), config);
            if (members.isEmpty()) {
                if (config.announceEvents) {
                    player.sendSystemMessage(Component.literal(
                        "The hostile presence fades before it can find a path to you."
                    ));
                }
                continue;
            }

            ACTIVE.put(
                player.getUUID(),
                new ActiveHunt(
                    player.getUUID(),
                    level.dimension(),
                    members,
                    tick + HuntEventRules.durationTicks(config.eventDurationSeconds),
                    pending.forced()
                )
            );
            if (config.announceEvents) {
                player.sendSystemMessage(Component.literal(
                    "The hunt begins — " + members.size() + " enemies are closing in!"
                ));
            }
        }
    }

    private static Set<UUID> spawnPack(
        ServerLevel level,
        ServerPlayer player,
        int requestedSize,
        HuntEventConfig config
    ) {
        List<EntityType<? extends Mob>> pool = pool(level.dimension());
        if (pool.isEmpty()) {
            return Set.of();
        }

        Set<UUID> members = new HashSet<>();
        for (int index = 0; index < requestedSize; index++) {
            BlockPos spawnPos = findSpawnPosition(level, player, config);
            if (spawnPos == null) {
                continue;
            }

            EntityType<? extends Mob> type = pool.get(level.getRandom().nextInt(pool.size()));
            Mob mob = type.spawn(
                level,
                spawned -> {
                    spawned.addTag(HUNT_MEMBER_TAG);
                    SPAWNING_MEMBERS.add(spawned.getUUID());
                },
                spawnPos,
                EntitySpawnReason.EVENT,
                false,
                false
            );
            if (mob == null) {
                continue;
            }

            SPAWNING_MEMBERS.remove(mob.getUUID());
            mob.setPersistenceRequired();
            mob.setTarget(player);
            mob.getNavigation().moveTo(player, config.pathingSpeed);
            members.add(mob.getUUID());
        }
        return members;
    }

    private static BlockPos findSpawnPosition(
        ServerLevel level,
        ServerPlayer player,
        HuntEventConfig config
    ) {
        for (int attempt = 0; attempt < config.spawnAttemptsPerMob; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            int distance = config.minimumSpawnDistance + level.getRandom().nextInt(
                config.maximumSpawnDistance - config.minimumSpawnDistance + 1
            );
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            for (int y = player.getBlockY() + 10; y >= player.getBlockY() - 14; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.hasChunkAt(pos)) {
                    continue;
                }
                if (isSafeSpawn(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        BlockPos floor = pos.below();
        return level.getBlockState(pos).isAir()
            && level.getBlockState(pos.above()).isAir()
            && !level.getBlockState(floor).getCollisionShape(level, floor).isEmpty();
    }

    private static void maintainActive(
        MinecraftServer server,
        long tick,
        HuntEventConfig config
    ) {
        Iterator<Map.Entry<UUID, ActiveHunt>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ActiveHunt> entry = iterator.next();
            ActiveHunt hunt = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(hunt.targetPlayer());
            ServerLevel level = server.getLevel(hunt.dimension());

            if (player == null
                || level == null
                || !player.isAlive()
                || player.isSpectator()
                || (!hunt.forced() && !config.allowCreativePlayers && player.isCreative())
                || !hunt.dimension().equals(player.level().dimension())) {
                discardMembers(level, hunt.members());
                iterator.remove();
                continue;
            }

            if (tick >= hunt.expiresAtTick()) {
                discardMembers(level, hunt.members());
                iterator.remove();
                if (config.announceEvents) {
                    player.sendSystemMessage(Component.literal(
                        "The remaining hunters lose your trail."
                    ));
                }
                continue;
            }

            hunt.members().removeIf(memberId -> {
                Entity entity = level.getEntity(memberId);
                if (entity == null) {
                    return false;
                }
                if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                    return true;
                }
                if (mob.distanceToSqr(player)
                    > config.maximumPursuitDistance * config.maximumPursuitDistance) {
                    mob.discard();
                    return true;
                }
                mob.setTarget(player);
                mob.getNavigation().moveTo(player, config.pathingSpeed);
                return false;
            });

            if (hunt.members().isEmpty()) {
                iterator.remove();
                if (config.announceEvents) {
                    player.sendSystemMessage(Component.literal(
                        "Hunt survived — the entire hostile pack has been defeated."
                    ));
                }
            }
        }
    }

    private static List<EntityType<? extends Mob>> pool(ResourceKey<Level> dimension) {
        if (Level.NETHER.equals(dimension)) {
            return NETHER_POOL;
        }
        if (Level.END.equals(dimension)) {
            return END_POOL;
        }
        if (Level.OVERWORLD.equals(dimension)) {
            return OVERWORLD_POOL;
        }
        return List.of();
    }

    private static boolean dimensionAllowed(ServerLevel level, HuntEventConfig config) {
        if (Level.NETHER.equals(level.dimension())) {
            return config.allowNether;
        }
        if (Level.END.equals(level.dimension())) {
            return config.allowEnd;
        }
        return Level.OVERWORLD.equals(level.dimension()) && config.allowOverworld;
    }

    private static void discardMembers(ServerLevel level, Set<UUID> members) {
        if (level == null) {
            return;
        }
        for (UUID memberId : members) {
            Entity entity = level.getEntity(memberId);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static void clearAll(MinecraftServer server) {
        for (ActiveHunt hunt : ACTIVE.values()) {
            discardMembers(server.getLevel(hunt.dimension()), hunt.members());
        }
        PENDING.clear();
        ACTIVE.clear();
        COOLDOWN_UNTIL.clear();
        SPAWNING_MEMBERS.clear();
    }

    private static boolean hasTag(Entity entity, String tag) {
        if (!entity.addTag(tag)) {
            return true;
        }
        entity.removeTag(tag);
        return false;
    }

    private record PendingHunt(
        UUID targetPlayer,
        ResourceKey<Level> dimension,
        long spawnAtTick,
        int packSize,
        boolean forced
    ) {}

    private record ActiveHunt(
        UUID targetPlayer,
        ResourceKey<Level> dimension,
        Set<UUID> members,
        long expiresAtTick,
        boolean forced
    ) {}
}
