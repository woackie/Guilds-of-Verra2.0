package com.guildsofverra;

import com.guildsofverra.command.GvCommands;
import com.guildsofverra.config.EliteConfig;
import com.guildsofverra.config.HuntEventConfig;
import com.guildsofverra.config.ProgressionConfig;
import com.guildsofverra.config.WorldEventConfig;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.data.GvAttachments;
import com.guildsofverra.elite.EliteAbilityEvents;
import com.guildsofverra.elite.EliteMobService;
import com.guildsofverra.event.HuntEventService;
import com.guildsofverra.event.ProgressionEvents;
import com.guildsofverra.event.WorldEventService;
import com.guildsofverra.event.WorldEventShutdownRecovery;
import com.guildsofverra.network.GvNetworking;
import com.guildsofverra.passive.PassiveAttributeEvents;
import com.guildsofverra.passive.PassiveFoodContext;
import com.guildsofverra.passive.PassiveLootEvents;
import com.guildsofverra.restriction.RestrictionEvents;
import com.guildsofverra.world.DimensionGateEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GuildsOfVerra implements ModInitializer {
    public static final String MOD_ID = "guildsofverra";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override public void onInitialize() {
        GvContent.initialize();
        ProgressionConfig.initialize();
        EliteConfig.initialize();
        HuntEventConfig.initialize();
        WorldEventConfig.initialize();
        GvAttachments.initialize();
        GvNetworking.initialize();
        GvCommands.initialize();
        ProgressionEvents.initialize();
        PassiveAttributeEvents.initialize();
        PassiveFoodContext.initialize();
        PassiveLootEvents.initialize();
        RestrictionEvents.initialize();
        DimensionGateEvents.initialize();
        EliteMobService.initialize();
        EliteAbilityEvents.initialize();
        HuntEventService.initialize();
        WorldEventService.initialize();
        WorldEventShutdownRecovery.initialize();
        LOGGER.info("Guilds of Verra {} initialized.", GvVersion.CURRENT);
    }

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}
