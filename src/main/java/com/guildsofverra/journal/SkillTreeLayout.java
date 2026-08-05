package com.guildsofverra.journal;

import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Produces a deterministic, scrollable two-dimensional layout from a data-driven skill tree.
 * Progression runs left-to-right by minimum level while node categories form vertical lanes.
 */
public final class SkillTreeLayout {
    public static final int NODE_WIDTH = 132;
    public static final int NODE_HEIGHT = 42;
    public static final int LEVEL_SPACING = 18;
    public static final int STACK_SPACING = 10;
    public static final int LANE_GAP = 36;
    public static final int PADDING = 40;

    private SkillTreeLayout() {}

    public static Layout build(SkillTreeDefinition tree) {
        LinkedHashMap<String, LaneMetrics> lanes = laneMetrics(tree.nodes());
        Map<String, Integer> laneBaseY = laneBasePositions(lanes);
        Map<String, Integer> stackIndexes = new HashMap<>();
        List<LayoutNode> nodes = new ArrayList<>();
        Map<String, LayoutNode> nodesById = new HashMap<>();

        int maximumX = PADDING;
        int maximumY = PADDING;
        for (SkillNodeDefinition node : tree.nodes()) {
            String category = normalizedCategory(node.category());
            LaneMetrics lane = lanes.get(category);
            String stackKey = category + "\u0000" + node.minLevel();
            int stackIndex = stackIndexes.merge(stackKey, 1, Integer::sum) - 1;
            int x = PADDING + node.minLevel() * LEVEL_SPACING;
            int y = laneBaseY.get(category) + stackIndex * (NODE_HEIGHT + STACK_SPACING);

            LayoutNode positioned = new LayoutNode(
                node.id(),
                category,
                lane.index(),
                node.minLevel(),
                x,
                y
            );
            nodes.add(positioned);
            nodesById.put(node.id(), positioned);
            maximumX = Math.max(maximumX, x + NODE_WIDTH);
            maximumY = Math.max(maximumY, y + NODE_HEIGHT);
        }

        List<LayoutEdge> edges = new ArrayList<>();
        for (SkillNodeDefinition node : tree.nodes()) {
            for (String prerequisite : node.prerequisites()) {
                if (nodesById.containsKey(prerequisite)) {
                    edges.add(new LayoutEdge(prerequisite, node.id()));
                }
            }
        }

        return new Layout(
            maximumX + PADDING,
            maximumY + PADDING,
            List.copyOf(nodes),
            List.copyOf(edges),
            List.copyOf(lanes.keySet())
        );
    }

    public static Set<String> unresolvedPrerequisites(SkillTreeDefinition tree) {
        Set<String> nodeIds = tree.nodes().stream()
            .map(SkillNodeDefinition::id)
            .collect(Collectors.toUnmodifiableSet());
        return tree.nodes().stream()
            .flatMap(node -> node.prerequisites().stream())
            .filter(prerequisite -> !nodeIds.contains(prerequisite))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static LinkedHashMap<String, LaneMetrics> laneMetrics(
        List<SkillNodeDefinition> nodes
    ) {
        LinkedHashMap<String, Map<Integer, Integer>> counts = new LinkedHashMap<>();
        for (SkillNodeDefinition node : nodes) {
            String category = normalizedCategory(node.category());
            counts.computeIfAbsent(category, ignored -> new HashMap<>())
                .merge(node.minLevel(), 1, Integer::sum);
        }

        LinkedHashMap<String, LaneMetrics> metrics = new LinkedHashMap<>();
        int index = 0;
        for (Map.Entry<String, Map<Integer, Integer>> entry : counts.entrySet()) {
            int maximumStack = entry.getValue().values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);
            metrics.put(entry.getKey(), new LaneMetrics(index++, maximumStack));
        }
        return metrics;
    }

    private static Map<String, Integer> laneBasePositions(
        LinkedHashMap<String, LaneMetrics> lanes
    ) {
        Map<String, Integer> positions = new LinkedHashMap<>();
        int y = PADDING;
        for (Map.Entry<String, LaneMetrics> entry : lanes.entrySet()) {
            positions.put(entry.getKey(), y);
            int laneHeight = entry.getValue().maximumStack()
                * (NODE_HEIGHT + STACK_SPACING)
                - STACK_SPACING;
            y += laneHeight + LANE_GAP;
        }
        return positions;
    }

    private static String normalizedCategory(String category) {
        if (category == null || category.isBlank()) {
            return "general";
        }
        return category.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public record Layout(
        int width,
        int height,
        List<LayoutNode> nodes,
        List<LayoutEdge> edges,
        List<String> lanes
    ) {
        public LayoutNode node(String id) {
            return nodes.stream()
                .filter(node -> node.id().equals(id))
                .findFirst()
                .orElse(null);
        }
    }

    public record LayoutNode(
        String id,
        String category,
        int lane,
        int minimumLevel,
        int x,
        int y
    ) {}

    public record LayoutEdge(String prerequisite, String dependent) {}

    private record LaneMetrics(int index, int maximumStack) {}
}
