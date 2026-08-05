package com.guildsofverra.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import com.guildsofverra.core.XpCurve;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
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
    private static final int NODE_ROWS_PER_PAGE = 8;
    private static final int MAX_PRESTIGE = 5;
    private static final long ACTION_PENDING_MILLIS = 3_000L;
    private static final long PRESTIGE_CONFIRM_MILLIS = 5_000L;
    private static final long STATUS_MESSAGE_MILLIS = 2_500L;

    private final List<Button> nodeButtons = new ArrayList<>();
    private View view = View.OVERVIEW;
    private String selectedSkill = "exploration";
    private int nodePage;
    private Button previousNodes;
    private Button nextNodes;
    private Button prestigeButton;

    private String pendingNodeId = "";
    private long pendingNodeUntil;
    private String pendingPrestigeSkill = "";
    private int pendingPrestigeFromRank;
    private long pendingPrestigeUntil;
    private String confirmPrestigeSkill = "";
    private long confirmPrestigeUntil;

    private long seenProfileRevision;
    private String actionStatus = "";
    private long actionStatusUntil;

    public JournalScreen() {
        super(Component.translatable("screen.guildsofverra.journal"));
    }

    @Override
    protected void init() {
        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;
        int tabY = y + 42;
        int tabX = x + 16;

        seenProfileRevision = ClientProfileCache.revision();
        nodeButtons.clear();

        tabX = addTab("Overview", tabX, tabY, 72, () -> selectView(View.OVERVIEW));
        for (String skill : SKILLS) {
            String label = Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
            tabX = addTab(label, tabX, tabY, 70, () -> selectSkill(skill));
        }
        tabX = addTab("Collections", tabX, tabY, 82, () -> selectView(View.COLLECTIONS));
        addTab("Gates", tabX, tabY, 58, () -> selectView(View.GATES));

        previousNodes = addRenderableWidget(
            Button.builder(Component.literal("Previous"), button -> changeNodePage(-1))
                .bounds(x + 20, y + panelHeight - 46, 82, 20)
                .build()
        );
        prestigeButton = addRenderableWidget(
            Button.builder(Component.literal("Prestige"), button -> handlePrestigeClick())
                .bounds(x + panelWidth / 2 - 58, y + panelHeight - 46, 116, 20)
                .build()
        );
        nextNodes = addRenderableWidget(
            Button.builder(Component.literal("Next"), button -> changeNodePage(1))
                .bounds(x + panelWidth - 102, y + panelHeight - 46, 82, 20)
                .build()
        );

        int nodeButtonX = x + panelWidth - 126;
        int firstNodeButtonY = y + 107;
        for (int row = 0; row < NODE_ROWS_PER_PAGE; row++) {
            final int visibleRow = row;
            Button nodeButton = addRenderableWidget(
                Button.builder(
                    Component.literal("Locked"),
                    button -> purchaseNodeAt(visibleRow)
                )
                    .bounds(nodeButtonX, firstNodeButtonY + row * 34, 92, 20)
                    .build()
            );
            nodeButtons.add(nodeButton);
        }

        updatePagingButtons();
        updateNodeButtons();
        updatePrestigeButton();
    }

    private int addTab(String label, int x, int y, int width, Runnable action) {
        addRenderableWidget(
            Button.builder(Component.literal(label), button -> action.run())
                .bounds(x, y, width, 20)
                .build()
        );
        return x + width + 4;
    }

    private void selectView(View next) {
        view = next;
        nodePage = 0;
        pendingNodeId = "";
        clearPrestigeConfirmation();
        updatePagingButtons();
        updateNodeButtons();
        updatePrestigeButton();
    }

    private void selectSkill(String skill) {
        selectedSkill = skill;
        selectView(View.SKILL);
    }

    private void changeNodePage(int delta) {
        nodePage = Math.max(0, Math.min(maxNodePage(), nodePage + delta));
        pendingNodeId = "";
        updatePagingButtons();
        updateNodeButtons();
    }

    private void updatePagingButtons() {
        if (previousNodes == null || nextNodes == null) {
            return;
        }
        boolean skillView = view == View.SKILL;
        previousNodes.visible = skillView;
        nextNodes.visible = skillView;
        previousNodes.active = skillView && nodePage > 0;
        nextNodes.active = skillView && nodePage < maxNodePage();
    }

    private void updateNodeButtons() {
        SkillTreeDefinition tree = selectedTree();
        int firstNode = nodePage * NODE_ROWS_PER_PAGE;
        long now = System.currentTimeMillis();

        for (int row = 0; row < nodeButtons.size(); row++) {
            Button button = nodeButtons.get(row);
            int nodeIndex = firstNode + row;
            boolean visible = view == View.SKILL
                && tree != null
                && nodeIndex < tree.nodes().size();
            button.visible = visible;

            if (!visible) {
                button.active = false;
                continue;
            }

            SkillNodeDefinition node = tree.nodes().get(nodeIndex);
            String fullNodeId = selectedSkill + ":" + node.id();
            NodeState state = nodeState(node);
            boolean pending = fullNodeId.equals(pendingNodeId) && now < pendingNodeUntil;

            button.setMessage(Component.literal(pending ? "Pending…" : buttonLabel(state, node)));
            button.active = state == NodeState.PURCHASABLE
                && !pending
                && pendingPrestigeSkill.isBlank();
        }
    }

    private void updatePrestigeButton() {
        if (prestigeButton == null) {
            return;
        }

        boolean skillView = view == View.SKILL;
        prestigeButton.visible = skillView;
        if (!skillView) {
            prestigeButton.active = false;
            return;
        }

        long now = System.currentTimeMillis();
        int level = ClientProfileCache.level(selectedSkill);
        int prestige = ClientProfileCache.prestige(selectedSkill);
        boolean pending = selectedSkill.equals(pendingPrestigeSkill) && now < pendingPrestigeUntil;
        boolean confirming = selectedSkill.equals(confirmPrestigeSkill) && now < confirmPrestigeUntil;

        if (pending) {
            prestigeButton.setMessage(Component.literal("Prestiging…"));
            prestigeButton.active = false;
        } else if (prestige >= MAX_PRESTIGE) {
            prestigeButton.setMessage(Component.literal("Prestige Max"));
            prestigeButton.active = false;
        } else if (level < XpCurve.MAX_LEVEL) {
            prestigeButton.setMessage(Component.literal("Prestige at Lv 100"));
            prestigeButton.active = false;
        } else if (confirming) {
            prestigeButton.setMessage(Component.literal("Confirm Prestige"));
            prestigeButton.active = pendingNodeId.isBlank();
        } else {
            prestigeButton.setMessage(Component.literal("Prestige " + (prestige + 1)));
            prestigeButton.active = pendingNodeId.isBlank();
        }
    }

    private void purchaseNodeAt(int visibleRow) {
        SkillTreeDefinition tree = selectedTree();
        int nodeIndex = nodePage * NODE_ROWS_PER_PAGE + visibleRow;
        if (view != View.SKILL || tree == null || nodeIndex >= tree.nodes().size()) {
            return;
        }

        SkillNodeDefinition node = tree.nodes().get(nodeIndex);
        if (nodeState(node) != NodeState.PURCHASABLE
            || !pendingNodeId.isBlank()
            || !pendingPrestigeSkill.isBlank()) {
            return;
        }

        if (JournalClientActions.purchaseNode(selectedSkill, node.id())) {
            long now = System.currentTimeMillis();
            pendingNodeId = selectedSkill + ":" + node.id();
            pendingNodeUntil = now + ACTION_PENDING_MILLIS;
            actionStatus = "Purchase request sent — " + readable(node.id());
            actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
            clearPrestigeConfirmation();
        } else {
            actionStatus = "Purchase request unavailable.";
            actionStatusUntil = System.currentTimeMillis() + STATUS_MESSAGE_MILLIS;
        }
        updateNodeButtons();
        updatePrestigeButton();
    }

    private void handlePrestigeClick() {
        if (view != View.SKILL
            || ClientProfileCache.level(selectedSkill) < XpCurve.MAX_LEVEL
            || ClientProfileCache.prestige(selectedSkill) >= MAX_PRESTIGE
            || !pendingNodeId.isBlank()
            || !pendingPrestigeSkill.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean confirmed = selectedSkill.equals(confirmPrestigeSkill) && now < confirmPrestigeUntil;
        if (!confirmed) {
            confirmPrestigeSkill = selectedSkill;
            confirmPrestigeUntil = now + PRESTIGE_CONFIRM_MILLIS;
            actionStatus = "Prestige resets level and XP; nodes, points and highest level are kept.";
            actionStatusUntil = confirmPrestigeUntil;
            updatePrestigeButton();
            return;
        }

        if (JournalClientActions.prestigeSkill(selectedSkill)) {
            pendingPrestigeSkill = selectedSkill;
            pendingPrestigeFromRank = ClientProfileCache.prestige(selectedSkill);
            pendingPrestigeUntil = now + ACTION_PENDING_MILLIS;
            actionStatus = "Prestige request sent — " + readable(selectedSkill);
            actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
            clearPrestigeConfirmation();
        } else {
            actionStatus = "Prestige request unavailable.";
            actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
        }
        updateNodeButtons();
        updatePrestigeButton();
    }

    private void clearPrestigeConfirmation() {
        confirmPrestigeSkill = "";
        confirmPrestigeUntil = 0L;
    }

    private int maxNodePage() {
        if (view != View.SKILL) {
            return 0;
        }
        SkillTreeDefinition tree = selectedTree();
        return tree == null ? 0 : Math.max(0, (tree.nodes().size() - 1) / NODE_ROWS_PER_PAGE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        refreshActionState();

        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xF0151A1F);
        graphics.fill(x, y, x + panelWidth, y + 36, 0xFF334039);
        graphics.fill(x + 12, y + 68, x + panelWidth - 12, y + panelHeight - 58, 0x8A202722);

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        centeredText(graphics, title, width / 2, y + 13, 0xFFE4C775);
        graphics.text(
            font,
            "Adventurer Level " + ClientProfileCache.adventurerLevel(),
            x + 18,
            y + panelHeight - 22,
            0xFFC9D2CC,
            false
        );
        graphics.text(
            font,
            ClientProfileCache.version() + " • payload " + ClientProfileCache.payloadVersion(),
            x + panelWidth - 160,
            y + panelHeight - 22,
            0xFF9DA8A2,
            false
        );

        int contentX = x + 24;
        int contentY = y + 78;
        int contentWidth = panelWidth - 48;
        switch (view) {
            case OVERVIEW -> drawOverview(graphics, contentX, contentY, contentWidth);
            case SKILL -> drawSkillView(graphics, contentX, contentY, contentWidth);
            case COLLECTIONS -> drawCollections(graphics, contentX, contentY, contentWidth);
            case GATES -> drawGates(graphics, contentX, contentY, contentWidth);
        }
    }

    private void refreshActionState() {
        long now = System.currentTimeMillis();
        long revision = ClientProfileCache.revision();
        if (revision != seenProfileRevision) {
            boolean nodePurchased = !pendingNodeId.isBlank()
                && ClientProfileCache.hasNode(pendingNodeId);
            boolean prestiged = !pendingPrestigeSkill.isBlank()
                && ClientProfileCache.prestige(pendingPrestigeSkill) > pendingPrestigeFromRank;

            if (!pendingNodeId.isBlank()) {
                actionStatus = nodePurchased
                    ? "Purchase confirmed — " + readable(pendingNodeId)
                    : "Profile synchronized.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
            } else if (!pendingPrestigeSkill.isBlank()) {
                actionStatus = prestiged
                    ? "Prestige confirmed — " + readable(pendingPrestigeSkill)
                    : "Profile synchronized.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
            }

            clearPendingActions();
            seenProfileRevision = revision;
            updateNodeButtons();
            updatePrestigeButton();
        } else {
            if (!pendingNodeId.isBlank() && now >= pendingNodeUntil) {
                pendingNodeId = "";
                pendingNodeUntil = 0L;
                actionStatus = "No purchase confirmation received — check chat feedback.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
                updateNodeButtons();
                updatePrestigeButton();
            }
            if (!pendingPrestigeSkill.isBlank() && now >= pendingPrestigeUntil) {
                pendingPrestigeSkill = "";
                pendingPrestigeUntil = 0L;
                actionStatus = "No prestige confirmation received — check chat feedback.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
                updateNodeButtons();
                updatePrestigeButton();
            }
        }

        if (!confirmPrestigeSkill.isBlank() && now >= confirmPrestigeUntil) {
            clearPrestigeConfirmation();
            updatePrestigeButton();
        }
        if (!actionStatus.isBlank() && now >= actionStatusUntil) {
            actionStatus = "";
        }
    }

    private void clearPendingActions() {
        pendingNodeId = "";
        pendingNodeUntil = 0L;
        pendingPrestigeSkill = "";
        pendingPrestigeFromRank = 0;
        pendingPrestigeUntil = 0L;
    }

    private void drawOverview(GuiGraphicsExtractor graphics, int x, int y, int width) {
        graphics.text(font, "Progression overview", x, y, 0xFFE4C775, false);
        graphics.text(
            font,
            "Purchased nodes: " + ClientProfileCache.purchasedNodeCount()
                + "   Discoveries: " + ClientProfileCache.discoveryCount()
                + "   Titles: " + ClientProfileCache.titleCount(),
            x + width - 330,
            y,
            0xFFB8C5BE,
            false
        );

        int rowY = y + 22;
        for (String skill : SKILLS) {
            drawSkillRow(graphics, skill, x, rowY, width);
            rowY += 48;
        }
    }

    private void drawSkillView(GuiGraphicsExtractor graphics, int x, int y, int width) {
        int level = ClientProfileCache.level(selectedSkill);
        int available = ClientProfileCache.availablePoints(selectedSkill);
        SkillTreeDefinition tree = selectedTree();

        graphics.text(
            font,
            Component.translatable("skill.guildsofverra." + selectedSkill),
            x,
            y,
            0xFFE4C775,
            false
        );
        graphics.text(
            font,
            "Level " + level
                + " • Prestige " + ClientProfileCache.prestige(selectedSkill) + "/" + MAX_PRESTIGE
                + " • " + available + " points available"
                + " • " + ClientProfileCache.purchasedNodeCount(selectedSkill) + " nodes purchased",
            x + 130,
            y,
            0xFFC9D2CC,
            false
        );

        if (tree == null) {
            graphics.text(font, "Skill tree data is unavailable.", x, y + 28, 0xFFFF7777, false);
            return;
        }

        int start = nodePage * NODE_ROWS_PER_PAGE;
        int end = Math.min(tree.nodes().size(), start + NODE_ROWS_PER_PAGE);
        int rowY = y + 24;
        for (int index = start; index < end; index++) {
            drawNodeRow(graphics, tree.nodes().get(index), x, rowY, width);
            rowY += 34;
        }

        graphics.text(
            font,
            "Nodes " + (start + 1) + "–" + end + " of " + tree.nodes().size()
                + " • page " + (nodePage + 1) + "/" + (maxNodePage() + 1),
            x + width / 2 - 75,
            y + 302,
            0xFF9DA8A2,
            false
        );
        if (!actionStatus.isBlank()) {
            graphics.text(font, actionStatus, x, y + 302, 0xFFE4C775, false);
        }
    }

    private void drawNodeRow(
        GuiGraphicsExtractor graphics,
        SkillNodeDefinition node,
        int x,
        int y,
        int width
    ) {
        NodeState state = nodeState(node);
        boolean purchased = state == NodeState.PURCHASED;

        graphics.fill(x, y, x + width, y + 30, purchased ? 0xA02E4935 : 0x9A29312D);
        graphics.text(font, readable(node.id()), x + 8, y + 5, 0xFFF0F0E8, false);
        graphics.text(
            font,
            ellipsize(node.effect(), 48),
            x + 180,
            y + 5,
            0xFFB8C5BE,
            false
        );
        graphics.text(
            font,
            stateLabel(state, node),
            x + width - 265,
            y + 17,
            stateColor(state),
            false
        );
        graphics.text(
            font,
            "Cost " + node.cost() + " • " + node.category(),
            x + 8,
            y + 17,
            0xFF9DA8A2,
            false
        );
    }

    private NodeState nodeState(SkillNodeDefinition node) {
        String fullId = selectedSkill + ":" + node.id();
        if (ClientProfileCache.hasNode(fullId)) {
            return NodeState.PURCHASED;
        }
        if (ClientProfileCache.level(selectedSkill) < node.minLevel()) {
            return NodeState.LEVEL_LOCKED;
        }
        boolean prerequisitesMet = node.prerequisites().stream()
            .allMatch(required -> ClientProfileCache.hasNode(selectedSkill + ":" + required));
        if (!prerequisitesMet) {
            return NodeState.PREREQUISITE_LOCKED;
        }
        if (ClientProfileCache.availablePoints(selectedSkill) < node.cost()) {
            return NodeState.POINTS_LOCKED;
        }
        return NodeState.PURCHASABLE;
    }

    private static String stateLabel(NodeState state, SkillNodeDefinition node) {
        return switch (state) {
            case PURCHASED -> "Purchased";
            case LEVEL_LOCKED -> "Requires level " + node.minLevel();
            case PREREQUISITE_LOCKED -> "Missing prerequisite";
            case POINTS_LOCKED -> "Requires " + node.cost() + " points";
            case PURCHASABLE -> "Ready to purchase";
        };
    }

    private static String buttonLabel(NodeState state, SkillNodeDefinition node) {
        return switch (state) {
            case PURCHASED -> "Owned";
            case LEVEL_LOCKED -> "Lv " + node.minLevel();
            case PREREQUISITE_LOCKED -> "Prerequisite";
            case POINTS_LOCKED -> "Need " + node.cost();
            case PURCHASABLE -> "Purchase";
        };
    }

    private static int stateColor(NodeState state) {
        return switch (state) {
            case PURCHASED -> 0xFF82C98B;
            case LEVEL_LOCKED -> 0xFF8D9691;
            case PREREQUISITE_LOCKED, POINTS_LOCKED -> 0xFFD19A62;
            case PURCHASABLE -> 0xFFE4C775;
        };
    }

    private void drawCollections(GuiGraphicsExtractor graphics, int x, int y, int width) {
        String selectedTitle = ClientProfileCache.selectedTitle();
        graphics.text(font, "Collections", x, y, 0xFFE4C775, false);
        graphics.text(
            font,
            "Discoveries " + ClientProfileCache.discoveryCount()
                + " • Titles " + ClientProfileCache.titleCount()
                + " • Active title: " + (selectedTitle.isBlank() ? "None" : readable(selectedTitle)),
            x + 100,
            y,
            0xFFC9D2CC,
            false
        );

        graphics.text(
            font,
            "Elite bestiary " + ClientProfileCache.discoveredEliteCount()
                + "/" + ClientProfileCache.eliteTotal(),
            x,
            y + 28,
            0xFFF0F0E8,
            false
        );

        int rowY = y + 50;
        int shown = 0;
        for (JsonElement element : ClientProfileCache.discoveredElites()) {
            if (!element.isJsonObject() || shown++ >= 7) {
                continue;
            }
            JsonObject elite = element.getAsJsonObject();
            String name = elite.has("displayName") ? elite.get("displayName").getAsString() : "Unknown elite";
            String base = elite.has("baseEntity") ? elite.get("baseEntity").getAsString() : "";
            double health = elite.has("healthMultiplier") ? elite.get("healthMultiplier").getAsDouble() : 1.0;
            double xp = elite.has("xpMultiplier") ? elite.get("xpMultiplier").getAsDouble() : 1.0;

            graphics.fill(x, rowY, x + width, rowY + 30, 0x9A29312D);
            graphics.text(font, name, x + 8, rowY + 5, 0xFFF0F0E8, false);
            graphics.text(font, base, x + 180, rowY + 5, 0xFF9DA8A2, false);
            graphics.text(
                font,
                "Health ×" + health + " • Combat XP ×" + xp,
                x + width - 220,
                rowY + 5,
                0xFFE4C775,
                false
            );
            rowY += 34;
        }

        if (ClientProfileCache.discoveredEliteCount() == 0) {
            graphics.text(
                font,
                "Defeat an elite variant to reveal its bestiary entry.",
                x,
                y + 58,
                0xFF9DA8A2,
                false
            );
        }
    }

    private void drawGates(GuiGraphicsExtractor graphics, int x, int y, int width) {
        graphics.text(font, "Travel progression gates", x, y, 0xFFE4C775, false);
        drawGate(graphics, "Nether", "nether", x, y + 28, width);
        drawGate(graphics, "The End", "end", x, y + 126, width);

        graphics.text(
            font,
            "Locked transfers return you to a recorded pre-portal position.",
            x,
            y + 230,
            0xFF9DA8A2,
            false
        );
    }

    private void drawGate(
        GuiGraphicsExtractor graphics,
        String name,
        String id,
        int x,
        int y,
        int width
    ) {
        JsonObject gate = ClientProfileCache.dimensionGate(id);
        boolean allowed = gate.has("allowed") && gate.get("allowed").getAsBoolean();
        int requiredAdventurer = gate.has("adventurerLevel")
            ? gate.get("adventurerLevel").getAsInt()
            : 0;

        graphics.fill(x, y, x + width, y + 82, allowed ? 0xA02E4935 : 0x9A3D2929);
        graphics.text(font, name, x + 10, y + 8, 0xFFF0F0E8, false);
        graphics.text(
            font,
            allowed ? "UNLOCKED" : "LOCKED",
            x + width - 80,
            y + 8,
            allowed ? 0xFF82C98B : 0xFFE08383,
            false
        );
        graphics.text(
            font,
            "Adventurer Level " + requiredAdventurer
                + " (current " + ClientProfileCache.adventurerLevel() + ")",
            x + 10,
            y + 26,
            0xFFC9D2CC,
            false
        );

        if (gate.has("skills")) {
            JsonObject skills = gate.getAsJsonObject("skills");
            int skillX = x + 10;
            for (String skill : SKILLS) {
                if (!skills.has(skill)) {
                    continue;
                }
                int required = skills.get(skill).getAsInt();
                graphics.text(
                    font,
                    readable(skill) + " " + required + " (" + ClientProfileCache.level(skill) + ")",
                    skillX,
                    y + 44,
                    ClientProfileCache.level(skill) >= required ? 0xFF82C98B : 0xFFE08383,
                    false
                );
                skillX += 155;
            }
        }

        if (!allowed && gate.has("reason")) {
            graphics.text(
                font,
                ellipsize(gate.get("reason").getAsString(), 110),
                x + 10,
                y + 62,
                0xFFD8AAA5,
                false
            );
        }
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

        graphics.fill(x, y, x + width, y + 40, 0xA02A332E);
        graphics.text(
            font,
            Component.translatable("skill.guildsofverra." + skill),
            x + 10,
            y + 6,
            0xFFF0F0E8,
            false
        );
        String levelText = "Lv. " + level + " • P" + ClientProfileCache.prestige(skill);
        graphics.text(font, levelText, x + 120, y + 6, 0xFFE4C775, false);

        int barX = x + 10;
        int barY = y + 23;
        int barWidth = Math.max(80, width - 310);
        int filled = level >= XpCurve.MAX_LEVEL
            ? barWidth
            : (int) Math.min(barWidth, Math.round((double) xp / Math.max(1L, required) * barWidth));
        graphics.fill(barX, barY, barX + barWidth, barY + 8, 0xFF101411);
        graphics.fill(barX, barY, barX + filled, barY + 8, 0xFF6C9A70);

        String xpText = level >= XpCurve.MAX_LEVEL ? "MAX" : xp + " / " + required + " XP";
        graphics.text(font, xpText, barX + barWidth + 8, barY, 0xFFC9D2CC, false);
        graphics.text(
            font,
            available + " available • " + spent + " spent • "
                + ClientProfileCache.purchasedNodeCount(skill) + " nodes",
            x + width - 205,
            y + 6,
            0xFFB8C5BE,
            false
        );
    }

    private SkillTreeDefinition selectedTree() {
        return SkillId.parse(selectedSkill).map(GvContent::tree).orElse(null);
    }

    private int panelWidth() {
        return Math.min(820, width - 24);
    }

    private int panelHeight() {
        return Math.min(460, height - 24);
    }

    private static String readable(String value) {
        String normalized = value == null ? "" : value;
        int separator = normalized.indexOf(':');
        if (separator >= 0 && separator + 1 < normalized.length()) {
            normalized = normalized.substring(separator + 1);
        }
        String[] words = normalized.replace('-', '_').split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static String ellipsize(String value, int maximumCharacters) {
        if (value == null || value.length() <= maximumCharacters) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, maximumCharacters - 1)) + "…";
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

    private enum View {
        OVERVIEW,
        SKILL,
        COLLECTIONS,
        GATES
    }

    private enum NodeState {
        PURCHASED,
        LEVEL_LOCKED,
        PREREQUISITE_LOCKED,
        POINTS_LOCKED,
        PURCHASABLE
    }
}
