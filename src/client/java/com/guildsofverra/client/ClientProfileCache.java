package com.guildsofverra.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class ClientProfileCache {
    private static final Gson GSON = new Gson();
    private static JsonObject profile = new JsonObject();

    private ClientProfileCache() {}

    public static void update(String json) {
        JsonObject decoded = GSON.fromJson(json, JsonObject.class);
        profile = decoded == null ? new JsonObject() : decoded;
    }

    public static JsonObject profile() {
        return profile;
    }

    public static String version() {
        return profile.has("version") ? profile.get("version").getAsString() : "development";
    }

    public static int adventurerLevel() {
        return profile.has("adventurerLevel") ? profile.get("adventurerLevel").getAsInt() : 0;
    }

    public static int level(String skill) {
        return integer(skill, "level");
    }

    public static long xp(String skill) {
        JsonObject data = skillData(skill);
        return data != null && data.has("xp") ? data.get("xp").getAsLong() : 0L;
    }

    public static int prestige(String skill) {
        return integer(skill, "prestige");
    }

    public static int availablePoints(String skill) {
        return integer(skill, "availablePoints");
    }

    public static int spentPoints(String skill) {
        return integer(skill, "spentPoints");
    }

    public static int purchasedNodeCount() {
        return profile.has("purchasedNodeCount") ? profile.get("purchasedNodeCount").getAsInt() : 0;
    }

    public static int discoveryCount() {
        return profile.has("discoveryCount") ? profile.get("discoveryCount").getAsInt() : 0;
    }

    private static int integer(String skill, String field) {
        JsonObject data = skillData(skill);
        return data != null && data.has(field) ? data.get(field).getAsInt() : 0;
    }

    private static JsonObject skillData(String skill) {
        if (!profile.has("skills")) return null;
        JsonObject skills = profile.getAsJsonObject("skills");
        return skills.has(skill) ? skills.getAsJsonObject(skill) : null;
    }
}
