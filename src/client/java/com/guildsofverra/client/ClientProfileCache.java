package com.guildsofverra.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ClientProfileCache {
    private static final Gson GSON = new Gson();
    private static JsonObject profile = new JsonObject();
    private static long revision;

    private ClientProfileCache() {}

    public static void update(String json) {
        JsonObject decoded = GSON.fromJson(json, JsonObject.class);
        profile = decoded == null ? new JsonObject() : decoded;
        revision++;
    }

    public static JsonObject profile() {
        return profile;
    }

    public static long revision() {
        return revision;
    }

    public static String version() {
        return profile.has("version") ? profile.get("version").getAsString() : "development";
    }

    public static int payloadVersion() {
        return profile.has("payloadVersion") ? profile.get("payloadVersion").getAsInt() : 1;
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

    public static boolean hasNode(String fullNodeId) {
        for (JsonElement element : array("nodes")) {
            if (element.isJsonPrimitive() && fullNodeId.equals(element.getAsString())) {
                return true;
            }
        }
        return false;
    }

    public static int purchasedNodeCount(String skill) {
        String prefix = skill + ":";
        int count = 0;
        for (JsonElement element : array("nodes")) {
            if (element.isJsonPrimitive() && element.getAsString().startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    public static int discoveryCount() {
        return profile.has("discoveryCount") ? profile.get("discoveryCount").getAsInt() : 0;
    }

    public static JsonArray discoveries() {
        return array("discoveries");
    }

    public static int titleCount() {
        return profile.has("titleCount") ? profile.get("titleCount").getAsInt() : 0;
    }

    public static JsonArray titles() {
        return array("titles");
    }

    public static String selectedTitle() {
        return profile.has("selectedTitle") ? profile.get("selectedTitle").getAsString() : "";
    }

    public static JsonObject dimensionGate(String id) {
        if (!profile.has("dimensionGates")) {
            return new JsonObject();
        }
        JsonObject gates = profile.getAsJsonObject("dimensionGates");
        return gates.has(id) ? gates.getAsJsonObject(id) : new JsonObject();
    }

    public static int eliteTotal() {
        JsonObject bestiary = bestiary();
        return bestiary.has("total") ? bestiary.get("total").getAsInt() : 0;
    }

    public static int discoveredEliteCount() {
        JsonObject bestiary = bestiary();
        return bestiary.has("discoveredCount") ? bestiary.get("discoveredCount").getAsInt() : 0;
    }

    public static JsonArray discoveredElites() {
        JsonObject bestiary = bestiary();
        return bestiary.has("discovered")
            ? bestiary.getAsJsonArray("discovered")
            : new JsonArray();
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

    private static JsonArray array(String field) {
        return profile.has(field) ? profile.getAsJsonArray(field) : new JsonArray();
    }

    private static JsonObject bestiary() {
        return profile.has("eliteBestiary")
            ? profile.getAsJsonObject("eliteBestiary")
            : new JsonObject();
    }
}
