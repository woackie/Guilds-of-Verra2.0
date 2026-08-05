package com.guildsofverra.journal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SkillTreeLayoutTest {
    @Test
    void createsDeterministicLanesAndPrerequisiteEdges() {
        SkillTreeDefinition tree = tree();

        SkillTreeLayout.Layout first = SkillTreeLayout.build(tree);
        SkillTreeLayout.Layout second = SkillTreeLayout.build(tree);

        assertEquals(first, second);
        assertEquals(List.of("core", "swords"), first.lanes());
        assertEquals(
            List.of(
                new SkillTreeLayout.LayoutEdge("root", "follow_up"),
                new SkillTreeLayout.LayoutEdge("root", "blade")
            ),
            first.edges()
        );
        assertTrue(SkillTreeLayout.unresolvedPrerequisites(tree).isEmpty());
    }

    @Test
    void stacksNodesThatShareALaneAndLevelWithoutOverlap() {
        SkillTreeLayout.Layout layout = SkillTreeLayout.build(tree());
        SkillTreeLayout.LayoutNode root = layout.node("root");
        SkillTreeLayout.LayoutNode sibling = layout.node("sibling");

        assertNotNull(root);
        assertNotNull(sibling);
        assertEquals(root.x(), sibling.x());
        assertNotEquals(root.y(), sibling.y());
        assertTrue(Math.abs(root.y() - sibling.y()) >= SkillTreeLayout.NODE_HEIGHT);
        assertTrue(layout.width() > root.x() + SkillTreeLayout.NODE_WIDTH);
        assertTrue(layout.height() > sibling.y() + SkillTreeLayout.NODE_HEIGHT);
    }

    @Test
    void reportsMissingPrerequisitesWithoutInventingEdges() {
        SkillNodeDefinition broken = new SkillNodeDefinition(
            "broken",
            1,
            1,
            "core",
            "Broken",
            List.of("missing"),
            Map.of()
        );
        SkillTreeDefinition tree = new SkillTreeDefinition(
            SkillId.EXPLORATION,
            100,
            1,
            List.of(broken)
        );

        SkillTreeLayout.Layout layout = SkillTreeLayout.build(tree);
        assertTrue(layout.edges().isEmpty());
        assertEquals(java.util.Set.of("missing"), SkillTreeLayout.unresolvedPrerequisites(tree));
    }

    private static SkillTreeDefinition tree() {
        return new SkillTreeDefinition(
            SkillId.COMBAT,
            100,
            1,
            List.of(
                node("root", 1, "core", List.of()),
                node("sibling", 1, "core", List.of()),
                node("follow_up", 10, "core", List.of("root")),
                node("blade", 10, "swords", List.of("root"))
            )
        );
    }

    private static SkillNodeDefinition node(
        String id,
        int level,
        String category,
        List<String> prerequisites
    ) {
        return new SkillNodeDefinition(
            id,
            level,
            1,
            category,
            id,
            prerequisites,
            Map.of()
        );
    }
}
