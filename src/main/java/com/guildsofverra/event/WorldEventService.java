package com.guildsofverra.event;

import com.guildsofverra.config.WorldEventConfig;
import com.guildsofverra.data.ProfileManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Coordinates mutually-exclusive server-wide survival events. */
public final class WorldEventService {
    private static final String EVENT_MOB_TAG = "guildsofverra_world_event_mob";
    private static final String RESTLESS_RISEN_TAG = "guildsofverra_restless_risen";

    private static final List<EntityType<? extends Mob>> CAVE_HAZARDS = List.of(
        EntityTypes.SILVERFISH,
        EntityTypes.CAVE_SPIDER
    );
    private static final List<EntityType<? extends Mob>> NETHER_SURGE_POOL = List.of(
        EntityTypes.BLAZE,
        EntityTypes.MAGMA_CUBE,
        EntityTypes.PIGLIN_BRUTE,
        EntityTypes.WITHER_SKELETON
    );
    private static final List<EntityType<? extends Mob>> PREDATOR_POOL = List.of(
        EntityTypes.SPIDER,
        EntityTypes.CAVE_SPIDER,
        EntityTypes.WOLF
    );

    private static final Set<UUID> SPAWNING_EVENT_MOBS = new HashSet<>();
    private static final List<PendingRevival> REVIVALS = new ArrayList<>();

    private static PendingEvent pending;
    private static ActiveEvent active;
    private static long nextEventAllowedTick;
    private static boolean schedulerInitialized;

    private WorldEventService() {}

    public static void initialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!isEventMob(entity)) {
                return;
            }
            if (SPAWNING_EVENT_MOBS.remove(entity.getUUID())) {
                return;
            }
            if (active == null || !active.members().contains(entity.getUUID())) {
                entity.discard();
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            ActiveEvent current = active;
            WorldEventConfig config = WorldEventConfig.current();
            if (current == null
                || current.type() != WorldEventType.RESTLESS_DEAD
                || REVIVALS.size() >= config.restlessMaximumPendingRevives
                || hasTag(entity, RESTLESS_RISEN_TAG)
                || !(entity.level() instanceof ServerLevel level)
                || !isRestlessCandidate(entity)
                || level.getRandom().nextDouble() >= config.restlessReviveChance) {
                return;
            }

            REVIVALS.add(new PendingRevival(
                entity.getType(),
                level.dimension(),
                entity.blockPosition(),
                level.getServer().getTickCount()
                    + (long) config.restlessReviveDelaySeconds * 20L
            ));
        });

        ServerTickEvents.END_SERVER_TICK.register(WorldEventService::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(WorldEventService::clearAll);
    }

    public static boolean isEventMob(Entity entity) {
        return hasTag(entity, EVENT_MOB_TAG);
    }

    public static WorldEventType activeType() {
        return active == null ? null : active.type();
    }

    private static void tick(MinecraftServer server) {
        WorldEventConfig config = WorldEventConfig.current();
        long tick = server.getTickCount();

        if (!schedulerInitialized) {
            schedulerInitialized = true;
            nextEventAllowedTick = tick
                + WorldEventRules.cooldownTicks(Math.max(1, config.initialDelayMinutes));
        }

        if (!config.enabled) {
            if (pending != null || active != null || !REVIVALS.isEmpty()) {
                clearAll(server);
            }
            return;
        }

        processRevivals(server, tick);

        if (active != null) {
            if (tick >= active.endsAtTick()) {
                endActive(server, tick, config);
            } else {
                runActive(server, tick, config);
            }
        }

        if (pending != null && tick >= pending.startsAtTick()) {
            startPending(server, tick, config);
        }

        long interval = (long) config.checkIntervalSeconds * 20L;
        if (pending == null
            && active == null
            && tick % interval == 0L
            && WorldEventRules.shouldSchedule(
                randomSource(server).nextDouble(),
                config.triggerChancePerCheck,
                tick,
                nextEventAllowedTick,
                false
            )) {
            schedule(server, tick, config);
        }
    }

    private static void schedule(
        MinecraftServer server,
        long tick,
        WorldEventConfig config
    ) {
        List<ServerPlayer> eligible = eligiblePlayers(server, config);
        if (eligible.isEmpty()) {
            return;
        }

        List<WorldEventType> candidates = availableEvents(eligible, config);
        int totalWeight = WorldEventRules.totalWeight(
            candidates,
            type -> config.weight(type.id())
        );
        if (totalWeight <= 0) {
            return;
        }

        WorldEventType selected = WorldEventRules.pickWeighted(
            candidates,
            type -> config.weight(type.id()),
            randomSource(server).nextInt(totalWeight)
        );
        if (selected == null) {
            return;
        }

        pending = new PendingEvent(
            selected,
            tick + (long) config.warningSeconds * 20L
        );
        if (config.announceEvents) {
            announce(server, "§6[World Event] §e" + selected.warningMessage());
        }
    }

    private static void startPending(
        MinecraftServer server,
        long tick,
        WorldEventConfig config
    ) {
        PendingEvent starting = pending;
        pending = null;
        if (starting == null) {
            return;
        }

        active = new ActiveEvent(
            starting.type(),
            tick,
            tick + WorldEventRules.durationTicks(
                config.durationSeconds(starting.type().id())
            ),
            new HashSet<>()
        );

        if (config.announceEvents) {
            announce(server, "§4[World Event] §c" + starting.type().startMessage());
        }

        switch (starting.type()) {
            case BLOOD_MOON, LONG_NIGHT -> maintainNight(server);
            case SEVERE_THUNDERSTORM -> maintainStorm(server);
            case CAVE_TREMOR -> cavePulse(server, config);
            case NETHER_SURGE -> netherSurgePulse(server, config);
            case PREDATOR_MIGRATION -> predatorPulse(server, config);
            case RESTLESS_DEAD -> { }
        }
    }

    private static void runActive(
        MinecraftServer server,
        long tick,
        WorldEventConfig config
    ) {
        switch (active.type()) {
            case BLOOD_MOON -> {
                maintainNight(server);
                if (tick % config.bloodMoonBuffRefreshTicks == 0L) {
                    bloodMoonPulse(server, config);
                }
            }
            case SEVERE_THUNDERSTORM -> {
                if (tick % 100L == 0L) {
                    maintainStorm(server);
                }
                long lightningInterval = (long) config.thunderLightningIntervalSeconds * 20L;
                if (tick % lightningInterval == 0L) {
                    thunderPulse(server, config);
                }
            }
            case CAVE_TREMOR -> {
                long interval = (long) config.cavePulseIntervalSeconds * 20L;
                if (tick % interval == 0L) {
                    cavePulse(server, config);
                }
            }
            case NETHER_SURGE -> {
                if (tick % config.netherBuffRefreshTicks == 0L) {
                    buffNetherMobs(server);
                }
                long interval = (long) config.netherSpawnIntervalSeconds * 20L;
                if (tick % interval == 0L) {
                    netherSurgePulse(server, config);
                }
            }
            case PREDATOR_MIGRATION -> {
                long interval = (long) config.predatorSpawnIntervalSeconds * 20L;
                if (tick % interval == 0L) {
                    predatorPulse(server, config);
                }
            }
            case LONG_NIGHT -> maintainNight(server);
            case RESTLESS_DEAD -> { }
        }
    }

    private static void endActive(
        MinecraftServer server,
        long tick,
        WorldEventConfig config
    ) {
        ActiveEvent ending = active;
        active = null;
        if (ending == null) {
            return;
        }

        if (ending.type() == WorldEventType.SEVERE_THUNDERSTORM) {
            server.setWeatherParameters(6000, 0, false, false);
        }
        if (ending.type() == WorldEventType.BLOOD_MOON
            || ending.type() == WorldEventType.LONG_NIGHT) {
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld != null) {
                long day = overworld.getOverworldClockTime();
                setOverworldClockTime(
                    overworld,
                    day - Math.floorMod(day, 24_000L) + 23_000L
                );
            }
        }

        discardMembers(server, ending.members());
        REVIVALS.clear();
        nextEventAllowedTick = tick
            + WorldEventRules.cooldownTicks(config.minimumMinutesBetweenEvents);

        if (config.announceEvents) {
            announce(server, "§2[World Event] §a" + ending.type().endMessage());
        }
    }

    private static void maintainNight(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        long day = overworld.getOverworldClockTime();
        setOverworldClockTime(
            overworld,
            day - Math.floorMod(day, 24_000L) + 18_000L
        );
    }

    private static void maintainStorm(MinecraftServer server) {
        server.setWeatherParameters(0, 400, true, true);
    }

    private static void setOverworldClockTime(ServerLevel level, long ticks) {
        var clock = level.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        level.clockManager().setTotalTicks(clock, ticks);
    }

    private static void bloodMoonPulse(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (!isEligible(player, config)) {
                continue;
            }
            for (Monster monster : level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(config.bloodMoonBuffRadius),
                Monster::isAlive
            )) {
                monster.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0));
                monster.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 140, 0));
                if (monster.getTarget() == null && monster.distanceToSqr(player) <= 2_304.0) {
                    monster.setTarget(player);
                }
            }
        }
    }

    private static void thunderPulse(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (!isEligible(player, config)
                || level.getRandom().nextDouble() >= config.thunderLightningChance) {
                continue;
            }

            int x = player.getBlockX()
                + level.getRandom().nextInt(config.thunderLightningRadius * 2 + 1)
                - config.thunderLightningRadius;
            int z = player.getBlockZ()
                + level.getRandom().nextInt(config.thunderLightningRadius * 2 + 1)
                - config.thunderLightningRadius;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            Entity lightning = EntityTypes.LIGHTNING_BOLT.create(
                level,
                EntitySpawnReason.EVENT
            );
            if (lightning != null) {
                lightning.setPos(x + 0.5, y, z + 0.5);
                level.addFreshEntity(lightning);
            }
        }
    }

    private static void cavePulse(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        for (ServerPlayer player : eligiblePlayers(server, config)) {
            if (!(player.level() instanceof ServerLevel level)
                || !Level.OVERWORLD.equals(level.dimension())
                || !isUnderground(player, level, config)) {
                continue;
            }

            if (config.caveMiningFatigueTicks > 0) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.MINING_FATIGUE,
                    config.caveMiningFatigueTicks,
                    0
                ));
            }
            spawnHostilePack(
                level,
                player,
                CAVE_HAZARDS,
                config.caveHazardsPerPulse,
                config,
                true
            );
        }
    }

    private static void netherSurgePulse(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        for (ServerPlayer player : eligiblePlayers(server, config)) {
            if (player.level() instanceof ServerLevel level
                && Level.NETHER.equals(level.dimension())) {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
                spawnHostilePack(
                    level,
                    player,
                    NETHER_SURGE_POOL,
                    config.netherPackSize,
                    config,
                    true
                );
            }
        }
    }

    private static void buffNetherMobs(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.NETHER);
        if (level == null) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            for (Monster monster : level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(64.0),
                Monster::isAlive
            )) {
                monster.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0));
                monster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 140, 0));
            }
        }
    }

    private static void predatorPulse(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        for (ServerPlayer player : eligiblePlayers(server, config)) {
            if (!(player.level() instanceof ServerLevel level)
                || !Level.OVERWORLD.equals(level.dimension())) {
                continue;
            }

            int existing = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(96.0),
                WorldEventService::isEventMob
            ).size();
            int count = WorldEventRules.cappedSpawnCount(
                config.predatorPackSize,
                existing,
                config.eventMobCapPerPlayer
            );

            for (int index = 0; index < count; index++) {
                BlockPos spawnPos = findSpawnPosition(level, player, config);
                if (spawnPos == null) {
                    continue;
                }
                EntityType<? extends Mob> type = PREDATOR_POOL.get(
                    level.getRandom().nextInt(PREDATOR_POOL.size())
                );
                Mob mob = spawnEventMob(level, type, spawnPos);
                if (mob == null) {
                    continue;
                }

                double dx = player.getX() - (spawnPos.getX() + 0.5);
                double dz = player.getZ() - (spawnPos.getZ() + 0.5);
                double length = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
                double destinationX = player.getX() + dx / length * 32.0;
                double destinationZ = player.getZ() + dz / length * 32.0;
                mob.getNavigation().moveTo(
                    destinationX,
                    player.getY(),
                    destinationZ,
                    config.predatorMigrationSpeed
                );
            }
        }
    }

    private static void spawnHostilePack(
        ServerLevel level,
        ServerPlayer player,
        List<EntityType<? extends Mob>> pool,
        int requested,
        WorldEventConfig config,
        boolean targetPlayer
    ) {
        int existing = level.getEntitiesOfClass(
            Mob.class,
            player.getBoundingBox().inflate(96.0),
            WorldEventService::isEventMob
        ).size();
        int count = WorldEventRules.cappedSpawnCount(
            requested,
            existing,
            config.eventMobCapPerPlayer
        );

        for (int index = 0; index < count; index++) {
            BlockPos spawnPos = findSpawnPosition(level, player, config);
            if (spawnPos == null) {
                continue;
            }
            EntityType<? extends Mob> type = pool.get(
                level.getRandom().nextInt(pool.size())
            );
            Mob mob = spawnEventMob(level, type, spawnPos);
            if (mob != null && targetPlayer) {
                mob.setTarget(player);
                mob.getNavigation().moveTo(player, 1.1);
            }
        }
    }

    private static Mob spawnEventMob(
        ServerLevel level,
        EntityType<? extends Mob> type,
        BlockPos spawnPos
    ) {
        Mob mob = type.spawn(
            level,
            spawned -> {
                spawned.addTag(EVENT_MOB_TAG);
                SPAWNING_EVENT_MOBS.add(spawned.getUUID());
            },
            spawnPos,
            EntitySpawnReason.EVENT,
            false,
            false
        );
        if (mob == null) {
            return null;
        }

        SPAWNING_EVENT_MOBS.remove(mob.getUUID());
        mob.setPersistenceRequired();
        if (active != null) {
            active.members().add(mob.getUUID());
        }
        return mob;
    }

    private static void processRevivals(MinecraftServer server, long tick) {
        if (REVIVALS.isEmpty()) {
            return;
        }

        REVIVALS.removeIf(revival -> {
            if (tick < revival.reviveAtTick()) {
                return false;
            }

            if (active == null || active.type() != WorldEventType.RESTLESS_DEAD) {
                return true;
            }

            ServerLevel level = server.getLevel(revival.dimension());
            if (level == null || !level.hasChunkAt(revival.position())) {
                return true;
            }

            Entity revived = revival.type().spawn(
                level,
                spawned -> {
                    spawned.addTag(EVENT_MOB_TAG);
                    spawned.addTag(RESTLESS_RISEN_TAG);
                    SPAWNING_EVENT_MOBS.add(spawned.getUUID());
                },
                revival.position(),
                EntitySpawnReason.EVENT,
                false,
                false
            );
            if (revived != null) {
                SPAWNING_EVENT_MOBS.remove(revived.getUUID());
                if (revived instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    ServerPlayer nearest = level.players().stream()
                        .filter(player -> player.isAlive() && !player.isSpectator())
                        .min(java.util.Comparator.comparingDouble(revived::distanceToSqr))
                        .orElse(null);
                    if (nearest != null) {
                        mob.setTarget(nearest);
                    }
                }
                active.members().add(revived.getUUID());
            }
            return true;
        });
    }

    private static List<ServerPlayer> eligiblePlayers(
        MinecraftServer server,
        WorldEventConfig config
    ) {
        return server.getPlayerList().getPlayers().stream()
            .filter(player -> isEligible(player, config))
            .toList();
    }

    private static boolean isEligible(
        ServerPlayer player,
        WorldEventConfig config
    ) {
        return WorldEventRules.eligiblePlayer(
            ProfileManager.get(player).adventurerLevel(),
            config.minimumAdventurerLevel,
            player.isAlive(),
            player.isSpectator(),
            player.isCreative(),
            config.allowCreativePlayers
        );
    }

    private static List<WorldEventType> availableEvents(
        List<ServerPlayer> players,
        WorldEventConfig config
    ) {
        boolean overworld = players.stream().anyMatch(
            player -> Level.OVERWORLD.equals(player.level().dimension())
        );
        boolean nether = players.stream().anyMatch(
            player -> Level.NETHER.equals(player.level().dimension())
        );
        boolean underground = players.stream().anyMatch(player ->
            player.level() instanceof ServerLevel level
                && Level.OVERWORLD.equals(level.dimension())
                && isUnderground(player, level, config)
        );

        List<WorldEventType> candidates = new ArrayList<>();
        if (overworld) {
            candidates.add(WorldEventType.BLOOD_MOON);
            candidates.add(WorldEventType.SEVERE_THUNDERSTORM);
            candidates.add(WorldEventType.PREDATOR_MIGRATION);
            candidates.add(WorldEventType.LONG_NIGHT);
        }
        if (underground) {
            candidates.add(WorldEventType.CAVE_TREMOR);
        }
        if (nether) {
            candidates.add(WorldEventType.NETHER_SURGE);
        }
        candidates.add(WorldEventType.RESTLESS_DEAD);
        candidates.removeIf(type -> config.weight(type.id()) <= 0);
        return candidates;
    }

    private static boolean isUnderground(
        ServerPlayer player,
        ServerLevel level,
        WorldEventConfig config
    ) {
        return player.getBlockY() <= config.caveMaximumY
            && !level.canSeeSky(player.blockPosition());
    }

    private static BlockPos findSpawnPosition(
        ServerLevel level,
        ServerPlayer player,
        WorldEventConfig config
    ) {
        for (int attempt = 0; attempt < config.eventSpawnAttempts; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            int distance = config.eventSpawnMinimumDistance + level.getRandom().nextInt(
                config.eventSpawnMaximumDistance - config.eventSpawnMinimumDistance + 1
            );
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);

            for (int y = player.getBlockY() + 10; y >= player.getBlockY() - 14; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.hasChunkAt(pos) && isSafeSpawn(level, pos)) {
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

    private static boolean isRestlessCandidate(LivingEntity entity) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return switch (id) {
            case "minecraft:zombie",
                 "minecraft:zombie_villager",
                 "minecraft:husk",
                 "minecraft:drowned",
                 "minecraft:skeleton",
                 "minecraft:stray",
                 "minecraft:wither_skeleton",
                 "minecraft:zombified_piglin" -> true;
            default -> false;
        };
    }

    private static RandomSource randomSource(MinecraftServer server) {
        return server.overworld().getRandom();
    }

    private static void announce(MinecraftServer server, String message) {
        Component component = Component.literal(message);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(component);
        }
    }

    private static void discardMembers(
        MinecraftServer server,
        Set<UUID> members
    ) {
        for (ServerLevel level : knownLevels(server)) {
            for (UUID memberId : members) {
                Entity entity = level.getEntity(memberId);
                if (entity != null) {
                    entity.discard();
                }
            }
        }
    }

    private static List<ServerLevel> knownLevels(MinecraftServer server) {
        List<ServerLevel> levels = new ArrayList<>();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        ServerLevel nether = server.getLevel(Level.NETHER);
        ServerLevel end = server.getLevel(Level.END);
        if (overworld != null) {
            levels.add(overworld);
        }
        if (nether != null) {
            levels.add(nether);
        }
        if (end != null) {
            levels.add(end);
        }
        return levels;
    }

    private static void clearAll(MinecraftServer server) {
        if (active != null) {
            discardMembers(server, active.members());
        }
        pending = null;
        active = null;
        REVIVALS.clear();
        SPAWNING_EVENT_MOBS.clear();
        nextEventAllowedTick = 0L;
        schedulerInitialized = false;
    }

    private static boolean hasTag(Entity entity, String tag) {
        if (!entity.addTag(tag)) {
            return true;
        }
        entity.removeTag(tag);
        return false;
    }

    private record PendingEvent(
        WorldEventType type,
        long startsAtTick
    ) {}

    private record ActiveEvent(
        WorldEventType type,
        long startedAtTick,
        long endsAtTick,
        Set<UUID> members
    ) {}

    private record PendingRevival(
        EntityType<?> type,
        ResourceKey<Level> dimension,
        BlockPos position,
        long reviveAtTick
    ) {}
}
