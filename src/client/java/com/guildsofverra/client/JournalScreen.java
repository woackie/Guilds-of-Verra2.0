package com.guildsofverra.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class JournalScreen extends Screen {
    private static final String[] SKILLS = {
        "exploration",
        "fishing",
        "cooking",
        "mining",
        "combat"
    };

    public JournalScreen() {
        super(Component.translatable("screen.guildsofverra.journal"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int panelWidth = Math.min(520, width - 40);
        int x = (width - panelWidth) / 2;
        int y = 28;

        graphics.fill(x, y, x + panelWidth, height - 28, 0xDD151A1F);
        graphics.fill(x, y, x + panelWidth, y + 34, 0xFF334039);
        centeredText(graphics, title, width / 2, y + 12, 0xFFE4C775);

        graphics.text(
            font,
            Component.translatable(
                "screen.guildsofverra.adventurer_level",
                ClientProfileCache.adventurerLevel()
            ),
            x + 24,
            y + 52,
            0xFFFFFFFF,
            false
        );

        int rowY = y + 82;
        for (String skill : SKILLS) {
            graphics.fill(x + 20, rowY - 4, x + panelWidth - 20, rowY + 18, 0x88303934);
            graphics.text(
                font,
                Component.translatable("skill.guildsofverra." + skill),
                x + 30,
                rowY + 2,
                0xFFF0F0E8,
                false
            );

            String value = "Lv. " + ClientProfileCache.level(skill)
                + "   P" + ClientProfileCache.prestige(skill);
            graphics.text(font, value, x + panelWidth - 115, rowY + 2, 0xFFE4C775, false);
            rowY += 32;
        }

        centeredText(
            graphics,
            Component.literal("Development journal — full tree browser follows in the next UI pass"),
            width / 2,
            height - 48,
            0xFFAAAAAA
        );
    }

    private void centeredText(
        GuiGraphicsExtractor graphics,
        Component text,
        int centerX,
        int y,
        int color
    ) {
        graphics.text(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
