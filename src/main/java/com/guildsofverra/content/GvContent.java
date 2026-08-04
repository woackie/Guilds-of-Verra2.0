package com.guildsofverra.content;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.guildsofverra.GuildsOfVerra;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GvContent {
    private static final Gson GSON = new Gson();
    private static final EnumMap<SkillId, SkillTreeDefinition> TREES = new EnumMap<>(SkillId.class);
    private GvContent() {}

    public static void initialize() {
        for (SkillId skill : SkillId.values()) TREES.put(skill, loadTree(skill));
        GuildsOfVerra.LOGGER.info("Loaded {} skill trees with {} total nodes.", TREES.size(), TREES.values().stream().mapToInt(t -> t.nodes().size()).sum());
    }

    public static SkillTreeDefinition tree(SkillId skill) {
        SkillTreeDefinition tree = TREES.get(skill);
        if (tree == null) throw new IllegalStateException("Skill trees not initialized");
        return tree;
    }

    private static SkillTreeDefinition loadTree(SkillId skill) {
        String path = "/data/guildsofverra/guilds_of_verra/skill_trees/" + skill.serializedName() + ".json";
        try (var stream = GvContent.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing resource " + path);
            JsonObject root = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            List<SkillNodeDefinition> nodes = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("nodes")) {
                JsonObject obj = element.getAsJsonObject();
                List<String> prerequisites = new ArrayList<>();
                JsonArray prereqArray = obj.getAsJsonArray("prerequisites");
                if (prereqArray != null) prereqArray.forEach(p -> prerequisites.add(p.getAsString()));
                Map<String,Object> bonus = obj.has("bonus") ? GSON.fromJson(obj.get("bonus"), Map.class) : Map.of();
                nodes.add(new SkillNodeDefinition(obj.get("id").getAsString(), obj.get("min_level").getAsInt(),
                    obj.get("cost").getAsInt(), obj.get("category").getAsString(), obj.get("effect").getAsString(), prerequisites, bonus));
            }
            SkillTreeDefinition tree = new SkillTreeDefinition(skill, root.get("max_level").getAsInt(), root.get("points_per_level").getAsInt(), nodes);
            if (tree.totalCost() != 100) throw new IllegalStateException(skill + " tree must cost exactly 100, got " + tree.totalCost());
            return tree;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load " + skill + " tree", exception);
        }
    }
}
