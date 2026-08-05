package com.guildsofverra.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import com.guildsofverra.core.XpCurve;
import com.guildsofverra.journal.SkillTreeLayout;
import com.guildsofverra.journal.TreeViewport;
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
    private static final int ROW_BUTTON_COUNT = 8;
    private static final int NODE_ROWS_PER_PAGE = 8;
    private static final int COLLECTION_ROWS_PER_PAGE = 8;
    private static final int BESTIARY_ROWS_PER_PAGE = 5;
    private static final int MAX_PRESTIGE = 5;
    private static final long ACTION_PENDING_MILLIS = 3_000L;
    private static final long PRESTIGE_CONFIRM_MILLIS = 5_000L;
    private static final long STATUS_MESSAGE_MILLIS = 2_500L;

    private final List<Button> rowButtons = new ArrayList<>();

    private View view = View.OVERVIEW;
    private CollectionSection collectionSection = CollectionSection.TITLES;
    private String selectedSkill = "exploration";
    private int page;

    private Button previousPage;
    private Button centerAction;
    private Button nextPage;
    private Button treeLeft;
    private Button treeRight;
    private Button treeUp;
    private Button treeDown;
    private Button treeZoomOut;
    private Button treeZoomIn;

    private SkillTreeLayout.Layout spatialLayout;
    private TreeViewport.State treeViewport = new TreeViewport.State(1.0, 0.0, 0.0);

    private String pendingNodeId = "";
    private long pendingNodeUntil;

    private String pendingPrestigeSkill = "";
    private int pendingPrestigeFromRank;
    private long pendingPrestigeUntil;
    private String confirmPrestigeSkill = "";
    private long confirmPrestigeUntil;

    private boolean pendingTitleAction;
    private String requestedTitleId = "";
    private long pendingTitleUntil;

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
        rowButtons.clear();

        tabX = addTab("Overview", tabX, tabY, 72, () -> selectView(View.OVERVIEW));
        for (String skill : SKILLS) {
            String label = Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
            tabX = addTab(label, tabX, tabY, 70, () -> selectSkill(skill));
        }
        tabX = addTab("Collections", tabX, tabY, 82, () -> selectView(View.COLLECTIONS));
        addTab("Gates", tabX, tabY, 58, () -> selectView(View.GATES));

        previousPage = addRenderableWidget(
            Button.builder(Component.literal("Previous"), button -> changePage(-1))
                .bounds(x + 20, y + panelHeight - 46, 82, 20)
                .build()
        );
        centerAction = addRenderableWidget(
            Button.builder(Component.literal("Prestige"), button -> handleCenterAction())
                .bounds(x + panelWidth / 2 - 64, y + panelHeight - 46, 128, 20)
                .build()
        );
        nextPage = addRenderableWidget(
            Button.builder(Component.literal("Next"), button -> changePage(1))
                .bounds(x + panelWidth - 102, y + panelHeight - 46, 82, 20)
                .build()
        );

        int treeControlsY = y + panelHeight - 72;
        treeLeft = addRenderableWidget(
            Button.builder(Component.literal("←"), button -> panTree(80, 0))
                .bounds(x + 24, treeControlsY, 30, 20)
                .build()
        );
        treeRight = addRenderableWidget(
            Button.builder(Component.literal("→"), button -> panTree(-80, 0))
                .bounds(x + 58, treeControlsY, 30, 20)
                .build()
        );
        treeUp = addRenderableWidget(
            Button.builder(Component.literal("↑"), button -> panTree(0, 60))
                .bounds(x + 92, treeControlsY, 30, 20)
                .build()
        );
        treeDown = addRenderableWidget(
            Button.builder(Component.literal("↓"), button -> panTree(0, -60))
                .bounds(x + 126, treeControlsY, 30, 20)
                .build()
        );
        treeZoomOut = addRenderableWidget(
            Button.builder(Component.literal("−"), button -> zoomTree(-0.15))
                .bounds(x + 166, treeControlsY, 30, 20)
                .build()
        );
        treeZoomIn = addRenderableWidget(
            Button.builder(Component.literal("+"), button -> zoomTree(0.15))
                .bounds(x + 200, treeControlsY, 30, 20)
                .build()
        );

        int rowButtonX = x + panelWidth - 126;
        int firstRowButtonY = y + 107;
        for (int row = 0; row < ROW_BUTTON_COUNT; row++) {
            final int visibleRow = row;
            Button rowButton = addRenderableWidget(
                Button.builder(
                    Component.literal("Locked"),
                    button -> handleRowAction(visibleRow)
                )
                    .bounds(rowButtonX, firstRowButtonY + row * 34, 92, 20)
                    .build()
            );
            rowButtons.add(rowButton);
        }

        resetTreeViewport();
        updateControls();
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
        page = 0;
        clearPrestigeConfirmation();
        updateControls();
    }

    private void selectSkill(String skill) {
        selectedSkill = skill;
        resetTreeViewport();
        selectView(View.SKILL);
    }

    private void changePage(int delta) {
        page = Math.max(0, Math.min(maxPage(), page + delta));
        clearPrestigeConfirmation();
        updateControls();
    }

    private void handleCenterAction() {
        if (view == View.SKILL) {
            handlePrestigeClick();
            return;
        }
        if (view == View.COLLECTIONS) {
            collectionSection = collectionSection.next();
            page = 0;
            updateControls();
        }
    }

    private void handleRowAction(int visibleRow) {
        if (view == View.SKILL) {
            purchaseNodeAt(visibleRow);
        } else if (view == View.COLLECTIONS && collectionSection == CollectionSection.TITLES) {
            selectTitleAt(visibleRow);
        }
    }

    private void updateControls() {
        updatePagingButtons();
        updateRowButtons();
        updateCenterButton();
        updateTreeControlButtons();
    }

    private void updatePagingButtons() {
        if (previousPage == null || nextPage == null) {
            return;
        }
        boolean pagedView = view == View.SKILL || view == View.COLLECTIONS;
        previousPage.visible = pagedView;
        nextPage.visible = pagedView;
        previousPage.active = pagedView && page > 0;
        nextPage.active = pagedView && page < maxPage();
    }

    private void updateTreeControlButtons() {
        boolean visible = view == View.SKILL;
        for (Button button : List.of(
            treeLeft,
            treeRight,
            treeUp,
            treeDown,
            treeZoomOut,
            treeZoomIn
        )) {
            if (button != null) {
                button.visible = visible;
                button.active = visible;
            }
        }
    }

    private void updateRowButtons() {
        long now = System.currentTimeMillis();

        for (int row = 0; row < rowButtons.size(); row++) {
            Button button = rowButtons.get(row);
            button.visible = false;
            button.active = false;

            if (view == View.SKILL) {
                SkillTreeDefinition tree = selectedTree();
                int nodeIndex = page * NODE_ROWS_PER_PAGE + row;
                if (tree == null || nodeIndex >= tree.nodes().size()) {
                    continue;
                }

                SkillNodeDefinition node = tree.nodes().get(nodeIndex);
                String fullNodeId = selectedSkill + ":" + node.id();
                NodeState state = nodeState(node);
                boolean pending = fullNodeId.equals(pendingNodeId) && now < pendingNodeUntil;

                button.visible = true;
                button.setMessage(Component.literal(pending ? "Pending…" : buttonLabel(state, node)));
                button.active = state == NodeState.PURCHASABLE
                    && !pending
                    && !anyActionPending();
                continue;
            }

            if (view == View.COLLECTIONS && collectionSection == CollectionSection.TITLES) {
                int titleIndex = page * COLLECTION_ROWS_PER_PAGE + row;
                String titleId = titleAt(titleIndex);
                if (titleId == null) {
                    continue;
                }

                boolean selected = titleId.equals(ClientProfileCache.selectedTitle());
                boolean pending = pendingTitleAction
                    && titleId.equals(requestedTitleId)
                    && now < pendingTitleUntil;

                button.visible = true;
                button.setMessage(Component.literal(
                    pending ? "Pending…" : selected ? "Active" : titleId.isBlank() ? "Clear" : "Select"
                ));
                button.active = !selected && !pending && !anyActionPending();
            }
        }
    }

    private void updateCenterButton() {
        if (centerAction == null) {
            return;
        }

        if (view == View.COLLECTIONS) {
            centerAction.visible = true;
            centerAction.active = true;
            centerAction.setMessage(Component.literal(collectionSection.displayName() + " →"));
            return;
        }

        boolean skillView = view == View.SKILL;
        centerAction.visible = skillView;
        if (!skillView) {
            centerAction.active = false;
            return;
        }

        long now = System.currentTimeMillis();
        int level = ClientProfileCache.level(selectedSkill);
        int prestige = ClientProfileCache.prestige(selectedSkill);
        boolean pending = selectedSkill.equals(pendingPrestigeSkill) && now < pendingPrestigeUntil;
        boolean confirming = selectedSkill.equals(confirmPrestigeSkill) && now < confirmPrestigeUntil;

        if (pending) {
            centerAction.setMessage(Component.literal("Prestiging…"));
            centerAction.active = false;
        } else if (prestige >= MAX_PRESTIGE) {
            centerAction.setMessage(Component.literal("Prestige Max"));
            centerAction.active = false;
        } else if (level < XpCurve.MAX_LEVEL) {
            centerAction.setMessage(Component.literal("Prestige at Lv 100"));
            centerAction.active = false;
        } else if (confirming) {
            centerAction.setMessage(Component.literal("Confirm Prestige"));
            centerAction.active = !anyActionPending();
        } else {
            centerAction.setMessage(Component.literal("Prestige " + (prestige + 1)));
            centerAction.active = !anyActionPending();
        }
    }

    private void purchaseNodeAt(int visibleRow) {
        SkillTreeDefinition tree = selectedTree();
        int nodeIndex = page * NODE_ROWS_PER_PAGE + visibleRow;
        if (view != View.SKILL
            || tree == null
            || nodeIndex >= tree.nodes().size()
            || anyActionPending()) {
            return;
        }

        SkillNodeDefinition node = tree.nodes().get(nodeIndex);
        if (nodeState(node) != NodeState.PURCHASABLE) {
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
        updateControls();
    }

    private void handlePrestigeClick() {
        if (view != View.SKILL
            || ClientProfileCache.level(selectedSkill) < XpCurve.MAX_LEVEL
            || ClientProfileCache.prestige(selectedSkill) >= MAX_PRESTIGE
            || anyActionPending()) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean confirmed = selectedSkill.equals(confirmPrestigeSkill) && now < confirmPrestigeUntil;
        if (!confirmed) {
            confirmPrestigeSkill = selectedSkill;
            confirmPrestigeUntil = now + PRESTIGE_CONFIRM_MILLIS;
            actionStatus = "Prestige resets level and XP; nodes, points and highest level are kept.";
            actionStatusUntil = confirmPrestigeUntil;
            updateCenterButton();
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
        updateControls();
    }

    private void selectTitleAt(int visibleRow) {
        if (view != View.COLLECTIONS
            || collectionSection != CollectionSection.TITLES
            || anyActionPending()) {
            return;
        }

        int titleIndex = page * COLLECTION_ROWS_PER_PAGE + visibleRow;
        String titleId = titleAt(titleIndex);
        if (titleId == null || titleId.equals(ClientProfileCache.selectedTitle())) {
            return;
        }

        if (JournalClientActions.selectTitle(titleId)) {
            long now = System.currentTimeMillis();
            pendingTitleAction = true;
            requestedTitleId = titleId;
            pendingTitleUntil = now + ACTION_PENDING_MILLIS;
            actionStatus = titleId.isBlank()
                ? "Clear-title request sent."
                : "Title request sent — " + readable(titleId);
            actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
        } else {
            actionStatus = "Title request unavailable.";
            actionStatusUntil = System.currentTimeMillis() + STATUS_MESSAGE_MILLIS;
        }
        updateControls();
    }

    private boolean anyActionPending() {
        long now = System.currentTimeMillis();
        return (!pendingNodeId.isBlank() && now < pendingNodeUntil)
            || (!pendingPrestigeSkill.isBlank() && now < pendingPrestigeUntil)
            || (pendingTitleAction && now < pendingTitleUntil);
    }

    private void clearPrestigeConfirmation() {
        confirmPrestigeSkill = "";
        confirmPrestigeUntil = 0L;
    }

    private void resetTreeViewport() {
        SkillTreeDefinition tree = selectedTree();
        if (tree == null) {
            spatialLayout = null;
            treeViewport = new TreeViewport.State(1.0, 0.0, 0.0);
            return;
        }
        spatialLayout = SkillTreeLayout.build(tree);
        treeViewport = TreeViewport.initial(
            spatialLayout.width(),
            spatialLayout.height(),
            treeViewportWidth(),
            treeViewportHeight()
        );
    }

    private void panTree(double deltaX, double deltaY) {
        if (view != View.SKILL || spatialLayout == null) {
            return;
        }
        treeViewport = TreeViewport.pan(
            treeViewport,
            deltaX,
            deltaY,
            spatialLayout.width(),
            spatialLayout.height(),
            treeViewportWidth(),
            treeViewportHeight()
        );
    }

    private void zoomTree(double delta) {
        if (view != View.SKILL || spatialLayout == null) {
            return;
        }
        treeViewport = TreeViewport.zoomAround(
            treeViewport,
            delta,
            treeViewportWidth() / 2.0,
            treeViewportHeight() / 2.0,
            spatialLayout.width(),
            spatialLayout.height(),
            treeViewportWidth(),
            treeViewportHeight()
        );
    }

    private int treeViewportWidth() {
        return Math.max(220, panelWidth() - 334);
    }

    private int treeViewportHeight() {
        return Math.max(180, panelHeight() - 176);
    }

    private int maxPage() {
        int itemCount;
        int rowsPerPage;

        if (view == View.SKILL) {
            SkillTreeDefinition tree = selectedTree();
            itemCount = tree == null ? 0 : tree.nodes().size();
            rowsPerPage = NODE_ROWS_PER_PAGE;
        } else if (view == View.COLLECTIONS) {
            itemCount = switch (collectionSection) {
                case TITLES -> ClientProfileCache.titles().size() + 1;
                case DISCOVERIES -> ClientProfileCache.discoveries().size();
                case BESTIARY -> ClientProfileCache.discoveredElites().size();
            };
            rowsPerPage = collectionSection == CollectionSection.BESTIARY
                ? BESTIARY_ROWS_PER_PAGE
                : COLLECTION_ROWS_PER_PAGE;
        } else {
            return 0;
        }

        return itemCount <= 0 ? 0 : Math.max(0, (itemCount - 1) / rowsPerPage);
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
            boolean titleChanged = pendingTitleAction
                && ClientProfileCache.selectedTitle().equals(requestedTitleId);

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
            } else if (pendingTitleAction) {
                actionStatus = titleChanged
                    ? requestedTitleId.isBlank()
                        ? "Active title cleared."
                        : "Title selected — " + readable(requestedTitleId)
                    : "Profile synchronized.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
            }

            clearPendingActions();
            seenProfileRevision = revision;
            updateControls();
        } else {
            if (!pendingNodeId.isBlank() && now >= pendingNodeUntil) {
                pendingNodeId = "";
                pendingNodeUntil = 0L;
                actionStatus = "No purchase confirmation received — check chat feedback.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
                updateControls();
            }
            if (!pendingPrestigeSkill.isBlank() && now >= pendingPrestigeUntil) {
                pendingPrestigeSkill = "";
                pendingPrestigeUntil = 0L;
                actionStatus = "No prestige confirmation received — check chat feedback.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
                updateControls();
            }
            if (pendingTitleAction && now >= pendingTitleUntil) {
                pendingTitleAction = false;
                requestedTitleId = "";
                pendingTitleUntil = 0L;
                actionStatus = "No title confirmation received — check chat feedback.";
                actionStatusUntil = now + STATUS_MESSAGE_MILLIS;
                updateControls();
            }
        }

        if (!confirmPrestigeSkill.isBlank() && now >= confirmPrestigeUntil) {
            clearPrestigeConfirmation();
            updateCenterButton();
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
        pendingTitleAction = false;
        requestedTitleId = "";
        pendingTitleUntil = 0L;
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
        if (spatialLayout == null) {
            resetTreeViewport();
        }

        int mapWidth = treeViewportWidth();
        int mapHeight = treeViewportHeight();
        drawSpatialTree(graphics, x, y + 24, mapWidth, mapHeight, tree);

        int listX = x + mapWidth + 12;
        int listWidth = width - mapWidth - 12;
        int start = page * NODE_ROWS_PER_PAGE;
        int end = Math.min(tree.nodes().size(), start + NODE_ROWS_PER_PAGE);
        int rowY = y + 24;
        for (int index = start; index < end; index++) {
            drawCompactNodeRow(graphics, tree.nodes().get(index), listX, rowY, listWidth);
            rowY += 34;
        }

        drawPageStatus(
            graphics,
            listX,
            y + 302,
            listWidth,
            "Nodes " + (start + 1) + "–" + end + " of " + tree.nodes().size()
        );
    }

    private void drawSpatialTree(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int width,
        int height,
        SkillTreeDefinition tree
    ) {
        graphics.fill(x, y, x + width, y + height, 0xD0141916);
        graphics.fill(x, y, x + width, y + 1, 0xFF54645B);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF54645B);
        graphics.fill(x, y, x + 1, y + height, 0xFF54645B);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF54645B);

        if (spatialLayout == null) {
            return;
        }

        for (SkillTreeLayout.LayoutEdge edge : spatialLayout.edges()) {
            SkillTreeLayout.LayoutNode from = spatialLayout.node(edge.prerequisite());
            SkillTreeLayout.LayoutNode to = spatialLayout.node(edge.dependent());
            if (from == null || to == null) {
                continue;
            }
            int fromX = x + screenTreeX(from.x() + SkillTreeLayout.NODE_WIDTH);
            int fromY = y + screenTreeY(from.y() + SkillTreeLayout.NODE_HEIGHT / 2);
            int toX = x + screenTreeX(to.x());
            int toY = y + screenTreeY(to.y() + SkillTreeLayout.NODE_HEIGHT / 2);
            drawTreeEdge(graphics, x, y, width, height, fromX, fromY, toX, toY);
        }

        for (SkillTreeLayout.LayoutNode positioned : spatialLayout.nodes()) {
            SkillNodeDefinition node = tree.nodesById().get(positioned.id());
            if (node == null) {
                continue;
            }

            int nodeX = x + screenTreeX(positioned.x());
            int nodeY = y + screenTreeY(positioned.y());
            int nodeWidth = Math.max(42, (int) Math.round(SkillTreeLayout.NODE_WIDTH * treeViewport.zoom()));
            int nodeHeight = Math.max(18, (int) Math.round(SkillTreeLayout.NODE_HEIGHT * treeViewport.zoom()));
            if (nodeX < x
                || nodeY < y
                || nodeX + nodeWidth > x + width
                || nodeY + nodeHeight > y + height) {
                continue;
            }

            NodeState state = nodeState(node);
            int background = switch (state) {
                case PURCHASED -> 0xE02E4935;
                case PURCHASABLE -> 0xE05A4D27;
                case LEVEL_LOCKED -> 0xE02A302D;
                case PREREQUISITE_LOCKED, POINTS_LOCKED -> 0xE03B3025;
            };
            graphics.fill(nodeX, nodeY, nodeX + nodeWidth, nodeY + nodeHeight, background);
            graphics.fill(nodeX, nodeY, nodeX + nodeWidth, nodeY + 1, stateColor(state));

            if (treeViewport.zoom() >= 0.70) {
                graphics.text(
                    font,
                    ellipsize(readable(node.id()), treeViewport.zoom() >= 1.0 ? 18 : 11),
                    nodeX + 5,
                    nodeY + 5,
                    0xFFF0F0E8,
                    false
                );
                graphics.text(
                    font,
                    "Lv " + node.minLevel(),
                    nodeX + 5,
                    nodeY + nodeHeight - 11,
                    stateColor(state),
                    false
                );
            }
        }

        graphics.text(
            font,
            "Tree map • " + Math.round(treeViewport.zoom() * 100) + "% • arrows pan • −/+ zoom",
            x + 6,
            y + height - 13,
            0xFF9DA8A2,
            false
        );
    }

    private void drawTreeEdge(
        GuiGraphicsExtractor graphics,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        int fromX,
        int fromY,
        int toX,
        int toY
    ) {
        int minimumX = viewportX;
        int maximumX = viewportX + viewportWidth;
        int minimumY = viewportY;
        int maximumY = viewportY + viewportHeight;
        if ((fromX < minimumX && toX < minimumX)
            || (fromX > maximumX && toX > maximumX)
            || (fromY < minimumY && toY < minimumY)
            || (fromY > maximumY && toY > maximumY)) {
            return;
        }

        int middleX = (fromX + toX) / 2;
        fillClipped(graphics, minimumX, minimumY, maximumX, maximumY, fromX, fromY, middleX, fromY + 1);
        fillClipped(graphics, minimumX, minimumY, maximumX, maximumY, middleX, Math.min(fromY, toY), middleX + 1, Math.max(fromY, toY) + 1);
        fillClipped(graphics, minimumX, minimumY, maximumX, maximumY, middleX, toY, toX, toY + 1);
    }

    private static void fillClipped(
        GuiGraphicsExtractor graphics,
        int minimumX,
        int minimumY,
        int maximumX,
        int maximumY,
        int x1,
        int y1,
        int x2,
        int y2
    ) {
        int left = Math.max(minimumX, Math.min(x1, x2));
        int right = Math.min(maximumX, Math.max(x1, x2));
        int top = Math.max(minimumY, Math.min(y1, y2));
        int bottom = Math.min(maximumY, Math.max(y1, y2));
        if (left < right && top < bottom) {
            graphics.fill(left, top, right, bottom, 0xFF63746A);
        }
    }

    private int screenTreeX(double worldX) {
        return (int) Math.round(TreeViewport.screenX(treeViewport, worldX));
    }

    private int screenTreeY(double worldY) {
        return (int) Math.round(TreeViewport.screenY(treeViewport, worldY));
    }

    private void drawCompactNodeRow(
        GuiGraphicsExtractor graphics,
        SkillNodeDefinition node,
        int x,
        int y,
        int width
    ) {
        NodeState state = nodeState(node);
        boolean purchased = state == NodeState.PURCHASED;

        graphics.fill(x, y, x + width, y + 30, purchased ? 0xA02E4935 : 0x9A29312D);
        graphics.text(font, ellipsize(readable(node.id()), 20), x + 8, y + 5, 0xFFF0F0E8, false);
        graphics.text(font, stateLabel(state, node), x + 8, y + 17, stateColor(state), false);
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
        graphics.text(font, "Collections — " + collectionSection.displayName(), x, y, 0xFFE4C775, false);
        graphics.text(
            font,
            "Discoveries " + ClientProfileCache.discoveryCount()
                + " • Titles " + ClientProfileCache.titleCount()
                + " • Active: " + (selectedTitle.isBlank() ? "None" : readable(selectedTitle))
                + " • Bestiary " + ClientProfileCache.discoveredEliteCount()
                + "/" + ClientProfileCache.eliteTotal(),
            x + 150,
            y,
            0xFFC9D2CC,
            false
        );

        switch (collectionSection) {
            case TITLES -> drawTitles(graphics, x, y + 24, width);
            case DISCOVERIES -> drawDiscoveries(graphics, x, y + 24, width);
            case BESTIARY -> drawBestiary(graphics, x, y + 24, width);
        }

        drawPageStatus(graphics, x, y + 302, width, collectionSection.pageLabel());
    }

    private void drawTitles(GuiGraphicsExtractor graphics, int x, int y, int width) {
        int start = page * COLLECTION_ROWS_PER_PAGE;
        int total = ClientProfileCache.titles().size() + 1;
        int end = Math.min(total, start + COLLECTION_ROWS_PER_PAGE);

        for (int index = start; index < end; index++) {
            String titleId = titleAt(index);
            boolean selected = titleId != null && titleId.equals(ClientProfileCache.selectedTitle());
            int rowY = y + (index - start) * 34;

            graphics.fill(x, rowY, x + width, rowY + 30, selected ? 0xA02E4935 : 0x9A29312D);
            graphics.text(
                font,
                titleId == null || titleId.isBlank() ? "No active title" : readable(titleId),
                x + 8,
                rowY + 6,
                0xFFF0F0E8,
                false
            );
            graphics.text(
                font,
                selected ? "ACTIVE" : titleId != null && titleId.isBlank() ? "Clear the displayed title" : "Unlocked title",
                x + 205,
                rowY + 6,
                selected ? 0xFF82C98B : 0xFFB8C5BE,
                false
            );
        }

        if (total == 1) {
            graphics.text(
                font,
                "No titles are unlocked yet. The clear-title option remains available.",
                x,
                y + 42,
                0xFF9DA8A2,
                false
            );
        }
    }

    private void drawDiscoveries(GuiGraphicsExtractor graphics, int x, int y, int width) {
        int start = page * COLLECTION_ROWS_PER_PAGE;
        int total = ClientProfileCache.discoveries().size();
        int end = Math.min(total, start + COLLECTION_ROWS_PER_PAGE);

        if (total == 0) {
            graphics.text(
                font,
                "No discoveries unlocked yet. Hidden entries remain secret until discovered.",
                x,
                y + 8,
                0xFF9DA8A2,
                false
            );
            return;
        }

        for (int index = start; index < end; index++) {
            JsonElement element = ClientProfileCache.discoveries().get(index);
            String discoveryId = element.isJsonPrimitive() ? element.getAsString() : "unknown";
            int rowY = y + (index - start) * 34;

            graphics.fill(x, rowY, x + width, rowY + 30, 0x9A29312D);
            graphics.text(font, readable(discoveryId), x + 8, rowY + 6, 0xFFF0F0E8, false);
            graphics.text(font, ellipsize(discoveryId, 54), x + 205, rowY + 6, 0xFF9DA8A2, false);
            graphics.text(font, "DISCOVERED", x + width - 98, rowY + 6, 0xFF82C98B, false);
        }
    }

    private void drawBestiary(GuiGraphicsExtractor graphics, int x, int y, int width) {
        int start = page * BESTIARY_ROWS_PER_PAGE;
        int total = ClientProfileCache.discoveredElites().size();
        int end = Math.min(total, start + BESTIARY_ROWS_PER_PAGE);

        if (total == 0) {
            graphics.text(
                font,
                "Defeat an elite variant to reveal its hidden bestiary entry.",
                x,
                y + 8,
                0xFF9DA8A2,
                false
            );
            return;
        }

        for (int index = start; index < end; index++) {
            JsonElement element = ClientProfileCache.discoveredElites().get(index);
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject elite = element.getAsJsonObject();
            String name = elite.has("displayName")
                ? elite.get("displayName").getAsString()
                : "Unknown elite";
            String base = elite.has("baseEntity") ? elite.get("baseEntity").getAsString() : "";
            int rowY = y + (index - start) * 52;

            graphics.fill(x, rowY, x + width, rowY + 46, 0x9A29312D);
            graphics.text(font, name, x + 8, rowY + 6, 0xFFF0F0E8, false);
            graphics.text(font, base, x + 190, rowY + 6, 0xFF9DA8A2, false);
            graphics.text(
                font,
                "Health ×" + number(elite, "healthMultiplier")
                    + " • Damage ×" + number(elite, "damageMultiplier")
                    + " • XP ×" + number(elite, "xpMultiplier"),
                x + 8,
                rowY + 23,
                0xFFE4C775,
                false
            );
            graphics.text(
                font,
                "Scale ×" + number(elite, "scale")
                    + " • Armor +" + number(elite, "armorBonus"),
                x + width - 210,
                rowY + 23,
                0xFFB8C5BE,
                false
            );
        }
    }

    private void drawPageStatus(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int width,
        String label
    ) {
        if (!actionStatus.isBlank()) {
            graphics.text(font, actionStatus, x, y, 0xFFE4C775, false);
        }
        graphics.text(
            font,
            label + " • page " + (page + 1) + "/" + (maxPage() + 1),
            x + width - 210,
            y,
            0xFF9DA8A2,
            false
        );
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

    private String titleAt(int index) {
        if (index < 0) {
            return null;
        }
        if (index == 0) {
            return "";
        }
        int titleIndex = index - 1;
        if (titleIndex >= ClientProfileCache.titles().size()) {
            return null;
        }
        JsonElement element = ClientProfileCache.titles().get(titleIndex);
        return element.isJsonPrimitive() ? element.getAsString() : null;
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
            if (word.isBlank()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
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

    private static String number(JsonObject object, String field) {
        if (!object.has(field)) {
            return "1";
        }
        double value = object.get(field).getAsDouble();
        if (Math.rint(value) == value) {
            return Long.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value)
            .replaceAll("0+$", "")
            .replaceAll("\\.$", "");
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

    private enum CollectionSection {
        TITLES("Titles", "Unlocked titles"),
        DISCOVERIES("Discoveries", "Unlocked discoveries"),
        BESTIARY("Bestiary", "Discovered elites");

        private final String displayName;
        private final String pageLabel;

        CollectionSection(String displayName, String pageLabel) {
            this.displayName = displayName;
            this.pageLabel = pageLabel;
        }

        String displayName() {
            return displayName;
        }

        String pageLabel() {
            return pageLabel;
        }

        CollectionSection next() {
            return switch (this) {
                case TITLES -> DISCOVERIES;
                case DISCOVERIES -> BESTIARY;
                case BESTIARY -> TITLES;
            };
        }
    }

    private enum NodeState {
        PURCHASED,
        LEVEL_LOCKED,
        PREREQUISITE_LOCKED,
        POINTS_LOCKED,
        PURCHASABLE
    }
}
