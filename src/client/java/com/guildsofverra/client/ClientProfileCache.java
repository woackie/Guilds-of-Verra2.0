package com.guildsofverra.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class ClientProfileCache {
    private static final Gson GSON = new Gson();
    private static JsonObject profile = new JsonObject();
    private ClientProfileCache() {}
    public static void update(String json) { profile = GSON.fromJson(json, JsonObject.class); }
    public static JsonObject profile() { return profile; }
    public static int adventurerLevel() { return profile.has("adventurerLevel") ? profile.get("adventurerLevel").getAsInt() : 0; }
    public static int level(String skill) { return profile.has("skills") && profile.getAsJsonObject("skills").has(skill) ? profile.getAsJsonObject("skills").getAsJsonObject(skill).get("level").getAsInt() : 0; }
    public static int prestige(String skill) { return profile.has("skills") && profile.getAsJsonObject("skills").has(skill) ? profile.getAsJsonObject("skills").getAsJsonObject(skill).get("prestige").getAsInt() : 0; }
}
