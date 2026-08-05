package com.guildsofverra.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillProgress;

public final class ProfileJson {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private ProfileJson() {}

    public static String toJson(PlayerProfile profile) {
        JsonObject root = new JsonObject();
        root.addProperty("version", "0.1.0-dev.3");
        root.addProperty("adventurerLevel", profile.adventurerLevel());
        JsonObject skills = new JsonObject();
        for (SkillId id : SkillId.values()) {
            SkillProgress progress = profile.skill(id);
            int spent = profile.spentPoints(id, GvContent.tree(id));
            JsonObject skill = new JsonObject();
            skill.addProperty("level", progress.level());
            skill.addProperty("xp", progress.xp());
            skill.addProperty("highest", progress.highestLevel());
            skill.addProperty("prestige", progress.prestige());
            skill.addProperty("earnedPoints", progress.earnedPoints());
            skill.addProperty("spentPoints", spent);
            skill.addProperty("availablePoints", Math.max(0, progress.earnedPoints() - spent));
            skills.add(id.serializedName(), skill);
        }
        root.add("skills", skills);
        JsonArray nodes = new JsonArray();
        profile.purchasedNodes().forEach(nodes::add);
        root.add("nodes", nodes);
        root.addProperty("purchasedNodeCount", profile.purchasedNodes().size());
        root.addProperty("discoveryCount", profile.discoveries().size());
        root.addProperty("titleCount", profile.titles().size());
        return GSON.toJson(root);
    }
}
