package com.guildsofverra.client;

import com.guildsofverra.core.XpCurve;
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

        int panelWidth = Math.min(600, width - 32);
        int panelHeight = Math.min(364, height - 32);
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xEE151A1F);
        graphics.fill(x, y, x + panelWidth, y + 38, 0xFF334039);
        centeredText(graphics, title, width / 2, y + 14, 0xFFE4C775);

        graphics.text(
            font,
            Component.translatable(
                "screen.guildsofverra.adventurer_level",
                ClientProfileCache.adventurerLevel()
            ),
            x + 22,
            y + 50,
            0xFFFFFFFF,
            false
        );
        graphics.text(
            font,
            Component.literal(
                "Purchased nodes: " + ClientProfileCache.purchasedNodeCount()
                    + "   Discoveries: " + ClientProfileCache.discoveryCount()
            ),
            x + panelWidth - 250,
            y + 50,
            0xFFB8C5BE,
            false
        );

        int rowY = y + 78;
        for (String skill : SKILLS) {
            drawSkillRow(graphics, skill, x + 18, rowY, panelWidth - 36);
            rowY += 50;
        }

        centeredText(
            graphics,
            Component.literal("J opens this journal • " + ClientProfileCache.version()),
            width / 2,
            y + panelHeight - 20,
            0xFF9DA8A2
        );
    }

    private void drawSkillRow(
        GuiGraphicsExtractor graphics,
        String skill,
        int x,
        int y,
        int width
    ) {
        int level = ClientProfileCache.level(skill);
        long xp = ClientProfileCache.xp(skill);
        long required = level >= XpCurve.MAX_LEVEL ? 1L : XpCurve.xpToNextLevel(level);
        int available = ClientProfileCache.availablePoints(skill);
        int spent = ClientProfileCache.spentPoints(skill);

        graphics.fill(x, y, x + width, y + 42, 0xA02A332E);
        graphics.text(
            font,
            Component.translatable("skill.guildsofverra." + skill),
            x + 10,
            y + 7,
            0xFFF0F0E8,
            false
        );
        String levelText = "Lv. " + level + "   Prestige " + ClientProfileCache.prestige(skill);
        graphics.text(font, levelText, x + width - 142, y + 7, 0xFFE4C775, false);

        int barX = x + 10;
        int barY = y + 24;
        int barWidth = width - 180;
        int filled = level >= XpCurve.MAX_LEVEL
            ? barWidth
            : (int) Math.min(barWidth, Math.round((double) xp / Math.max(1L, required) * barWidth));
        graphics.fill(barX, barY, barX + barWidth, barY + 8, 0xFF101411);
        graphics.fill(barX, barY, barX + filled, barY + 8, 0xFF6C9A70);

        String xpText = level >= XpCurve.MAX_LEVEL ? "MAX" : xp + " / " + required + " XP";
        graphics.text(font, xpText, barX + barWidth + 8, barY, 0xFFC9D2CC, false);
        graphics.text(
            font,
            "Points " + available + " available / " + spent + " spent",
            x + width - 180,
            y + 31,
            0xFFB8C5BE,
            false
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
