package com.guildsofverra.event;

import com.guildsofverra.GuildsOfVerra;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;

/** Restores safe world state when an orderly shutdown happens during a clock/weather event. */
public final class WorldEventShutdownRecovery {
    private WorldEventShutdownRecovery() {}

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WorldEventType type = WorldEventService.activeType();
            if (type == null) {
                return;
            }

            if (type == WorldEventType.SEVERE_THUNDERSTORM) {
                server.setWeatherParameters(6000, 0, false, false);
            }

            if (type == WorldEventType.BLOOD_MOON
                || type == WorldEventType.LONG_NIGHT) {
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    long current = overworld.getOverworldClockTime();
                    long nearSunrise = current
                        - Math.floorMod(current, 24_000L)
                        + 23_000L;
                    var clock = overworld.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
                    overworld.clockManager().setTotalTicks(clock, nearSunrise);
                }
            }

            GuildsOfVerra.LOGGER.info(
                "Restored safe world state before shutdown during {}.",
                type.displayName()
            );
        });
    }
}
