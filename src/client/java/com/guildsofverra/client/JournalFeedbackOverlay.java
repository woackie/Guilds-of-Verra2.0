package com.guildsofverra.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class JournalFeedbackOverlay {
    private static final long DISPLAY_MILLIS = 4_000L;
    private static long displayedRevision;
    private static long displayUntil;

    private JournalFeedbackOverlay() {}

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof JournalScreen)) {
                return;
            }

            ScreenEvents.afterExtract(screen).register(
                (currentScreen, graphics, mouseX, mouseY, tickProgress) ->
                    draw(client, graphics, scaledWidth, scaledHeight)
            );
        });
    }

    private static void draw(
        Minecraft client,
        GuiGraphicsExtractor graphics,
        int scaledWidth,
        int scaledHeight
    ) {
        JournalActionFeedback.Snapshot feedback = JournalActionFeedback.snapshot();
        if (feedback.revision() <= 0L || feedback.message().isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (feedback.revision() != displayedRevision) {
            displayedRevision = feedback.revision();
            displayUntil = now + DISPLAY_MILLIS;
        }
        if (now >= displayUntil) {
            return;
        }

        String rawMessage = (feedback.success() ? "✓ " : "! ") + feedback.message();
        Component message = Component.literal(
            fitToWidth(client, rawMessage, Math.max(40, scaledWidth - 24))
        );
        int textWidth = client.font.width(message);
        int x = Math.max(8, (scaledWidth - textWidth) / 2);
        int y = Math.max(8, scaledHeight - 64);
        int left = Math.max(4, x - 6);
        int right = Math.min(scaledWidth - 4, x + textWidth + 6);

        graphics.fill(left, y - 5, right, y + 14, 0xE01A1F1C);
        graphics.text(
            client.font,
            message,
            x,
            y,
            feedback.success() ? 0xFF82C98B : 0xFFE08383,
            false
        );
    }

    private static String fitToWidth(Minecraft client, String text, int maximumWidth) {
        if (client.font.width(text) <= maximumWidth) {
            return text;
        }

        String shortened = text;
        while (shortened.length() > 1
            && client.font.width(shortened + "…") > maximumWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + "…";
    }
}
