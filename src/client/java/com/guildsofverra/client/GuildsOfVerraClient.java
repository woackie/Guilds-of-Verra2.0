package com.guildsofverra.client;

import com.guildsofverra.network.JournalActionResultPayload;
import com.guildsofverra.network.ProfileSyncPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class GuildsOfVerraClient implements ClientModInitializer {
    private static KeyMapping openJournal;

    @Override
    public void onInitializeClient() {
        openJournal = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.guildsofverra.open_journal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KeyMapping.Category.MISC
        ));

        ClientPlayNetworking.registerGlobalReceiver(
            ProfileSyncPayload.TYPE,
            (payload, context) -> context.client().execute(
                () -> ClientProfileCache.update(payload.json())
            )
        );
        ClientPlayNetworking.registerGlobalReceiver(
            JournalActionResultPayload.TYPE,
            (payload, context) -> context.client().execute(
                () -> JournalActionFeedback.update(
                    payload.action(),
                    payload.success(),
                    payload.message()
                )
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openJournal.consumeClick()) {
                Minecraft.getInstance().setScreenAndShow(new JournalScreen());
            }
        });
    }
}
