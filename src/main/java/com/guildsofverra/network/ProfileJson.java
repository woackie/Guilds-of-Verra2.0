package com.guildsofverra.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillProgress;

public final class ProfileJson {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private ProfileJson() {}

    public static String toJson(PlayerProfile profile) {
        JsonObject root = new JsonObject();
        root.addProperty("adventurerLevel", profile.adventurerLevel());
        JsonObject skills = new JsonObject();
        for (SkillId id : SkillId.values()) {
            SkillProgress p = profile.skill(id);
            JsonObject skill = new JsonObject();
            skill.addProperty("level", p.level()); skill.addProperty("xp", p.xp()); skill.addProperty("highest", p.highestLevel()); skill.addProperty("prestige", p.prestige()); skill.addProperty("earnedPoints", p.earnedPoints());
            skills.add(id.serializedName(), skill);
        }
        root.add("skills", skills);
        JsonArray nodes = new JsonArray(); profile.purchasedNodes().forEach(nodes::add); root.add("nodes", nodes);
        return GSON.toJson(root);
    }
}
