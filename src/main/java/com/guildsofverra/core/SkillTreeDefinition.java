package com.guildsofverra.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreeDefinition {
    private final SkillId skill;
    private final int maxLevel;
    private final int pointsPerLevel;
    private final List<SkillNodeDefinition> nodes;
    private final Map<String, SkillNodeDefinition> nodesById;

    public SkillTreeDefinition(SkillId skill, int maxLevel, int pointsPerLevel, List<SkillNodeDefinition> nodes) {
        this.skill = skill; this.maxLevel = maxLevel; this.pointsPerLevel = pointsPerLevel; this.nodes = List.copyOf(nodes);
        LinkedHashMap<String, SkillNodeDefinition> map = new LinkedHashMap<>();
        for (SkillNodeDefinition node : nodes) {
            if (map.put(node.id(), node) != null) throw new IllegalArgumentException("Duplicate node: " + node.id());
        }
        this.nodesById = Map.copyOf(map);
    }

    public SkillId skill() { return skill; }
    public int maxLevel() { return maxLevel; }
    public int pointsPerLevel() { return pointsPerLevel; }
    public List<SkillNodeDefinition> nodes() { return nodes; }
    public Map<String, SkillNodeDefinition> nodesById() { return nodesById; }
    public int totalCost() { return nodes.stream().mapToInt(SkillNodeDefinition::cost).sum(); }
}
