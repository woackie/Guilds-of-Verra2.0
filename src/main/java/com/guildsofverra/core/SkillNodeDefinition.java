package com.guildsofverra.core;

import java.util.List;
import java.util.Map;

public record SkillNodeDefinition(String id, int minLevel, int cost, String category, String effect,
                                  List<String> prerequisites, Map<String, Object> bonus) {
    public SkillNodeDefinition {
        prerequisites = List.copyOf(prerequisites == null ? List.of() : prerequisites);
        bonus = Map.copyOf(bonus == null ? Map.of() : bonus);
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Node id cannot be blank");
        if (minLevel < 0 || minLevel > 100) throw new IllegalArgumentException("Invalid node level: " + minLevel);
        if (cost <= 0) throw new IllegalArgumentException("Node cost must be positive");
    }
}
