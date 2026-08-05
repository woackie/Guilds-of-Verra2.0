package com.guildsofverra;

import com.guildsofverra.command.GvCommands;
import com.guildsofverra.config.EliteConfig;
import com.guildsofverra.config.ProgressionConfig;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.data.GvAttachments;
import com.guildsofverra.elite.EliteMobService;
import com.guildsofverra.event.ProgressionEvents;
import com.guildsofverra.network.GvNetworking;
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
        GvAttachments.initialize();
        GvNetworking.initialize();
        GvCommands.initialize();
        ProgressionEvents.initialize();
        RestrictionEvents.initialize();
        DimensionGateEvents.initialize();
        EliteMobService.initialize();
        LOGGER.info("Guilds of Verra 0.1.0-dev.3 initialized.");
    }

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}
