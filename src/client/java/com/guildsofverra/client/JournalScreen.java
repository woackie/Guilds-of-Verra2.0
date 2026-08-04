package com.guildsofverra.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class JournalScreen extends Screen {
    private static final String[] SKILLS = {"exploration","fishing","cooking","mining","combat"};
    public JournalScreen() { super(Component.translatable("screen.guildsofverra.journal")); }

    @Override protected void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(520, width - 40); int x = (width - panelWidth) / 2; int y = 28;
        graphics.fill(x, y, x + panelWidth, height - 28, 0xDD151A1F);
        graphics.fill(x, y, x + panelWidth, y + 34, 0xFF334039);
        graphics.drawCenteredString(font, title, width / 2, y + 12, 0xFFE4C775);
        graphics.drawString(font, Component.translatable("screen.guildsofverra.adventurer_level", ClientProfileCache.adventurerLevel()), x + 24, y + 52, 0xFFFFFFFF, false);
        int rowY = y + 82;
        for (String skill : SKILLS) {
            graphics.fill(x + 20, rowY - 4, x + panelWidth - 20, rowY + 18, 0x88303934);
            graphics.drawString(font, Component.translatable("skill.guildsofverra." + skill), x + 30, rowY + 2, 0xFFF0F0E8, false);
            String value = "Lv. " + ClientProfileCache.level(skill) + "   P" + ClientProfileCache.prestige(skill);
            graphics.drawString(font, value, x + panelWidth - 115, rowY + 2, 0xFFE4C775, false);
            rowY += 32;
        }
        graphics.drawCenteredString(font, "Development journal — full tree browser follows in the next UI pass", width / 2, height - 48, 0xFFAAAAAA);
    }

    @Override public boolean isPauseScreen() { return false; }
}
