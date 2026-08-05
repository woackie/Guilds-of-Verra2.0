package com.guildsofverra.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.RequirementResult;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillProgress;
import com.guildsofverra.elite.EliteMobService;
import com.guildsofverra.elite.EliteVariantDefinition;
import com.guildsofverra.world.DimensionGateService;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ProfileJson {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private ProfileJson() {}

    public static String toJson(PlayerProfile profile) {
        JsonObject root = new JsonObject();
        root.addProperty("version", "0.1.0-dev.4");
        root.addProperty("payloadVersion", 2);
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
        profile.purchasedNodes().stream().sorted().forEach(nodes::add);
        root.add("nodes", nodes);
        root.addProperty("purchasedNodeCount", profile.purchasedNodes().size());

        JsonArray discoveries = new JsonArray();
        profile.discoveries().stream().sorted().forEach(discoveries::add);
        root.add("discoveries", discoveries);
        root.addProperty("discoveryCount", profile.discoveries().size());

        JsonArray titles = new JsonArray();
        profile.titles().stream().sorted().forEach(titles::add);
        root.add("titles", titles);
        root.addProperty("titleCount", profile.titles().size());
        root.addProperty("selectedTitle", profile.selectedTitle());

        JsonObject gates = new JsonObject();
        addGate(gates, "nether", profile, Level.NETHER);
        addGate(gates, "end", profile, Level.END);
        root.add("dimensionGates", gates);

        JsonObject bestiary = new JsonObject();
        bestiary.addProperty("total", EliteMobService.variants().size());
        JsonArray discoveredElites = new JsonArray();
        for (EliteVariantDefinition variant : EliteMobService.variants()) {
            String discoveryId = EliteMobService.discoveryId(variant.id());
            if (!profile.discoveries().contains(discoveryId)) {
                continue;
            }
            JsonObject elite = new JsonObject();
            elite.addProperty("id", variant.id());
            elite.addProperty("discoveryId", discoveryId);
            elite.addProperty("baseEntity", variant.baseEntity());
            elite.addProperty("displayName", variant.displayName());
            elite.addProperty("scale", variant.scale());
            elite.addProperty("healthMultiplier", variant.healthMultiplier());
            elite.addProperty("speedMultiplier", variant.speedMultiplier());
            elite.addProperty("armorBonus", variant.armorBonus());
            elite.addProperty("knockbackResistance", variant.knockbackResistance());
            elite.addProperty("damageMultiplier", variant.damageMultiplier());
            elite.addProperty("xpMultiplier", variant.xpMultiplier());
            discoveredElites.add(elite);
        }
        bestiary.add("discovered", discoveredElites);
        bestiary.addProperty("discoveredCount", discoveredElites.size());
        root.add("eliteBestiary", bestiary);

        return GSON.toJson(root);
    }

    private static void addGate(
        JsonObject gates,
        String id,
        PlayerProfile profile,
        ResourceKey<Level> dimension
    ) {
        DimensionGateService.GateRequirement requirement =
            DimensionGateService.requirement(dimension);
        if (requirement == null) {
            return;
        }

        RequirementResult result = DimensionGateService.canEnter(profile, dimension);
        JsonObject gate = new JsonObject();
        gate.addProperty("adventurerLevel", requirement.adventurerLevel());
        gate.addProperty("allowed", result.allowed());
        gate.addProperty("reason", result.reason());

        JsonObject skillLevels = new JsonObject();
        requirement.skillLevels().entrySet().stream()
            .sorted((left, right) -> left.getKey().serializedName()
                .compareTo(right.getKey().serializedName()))
            .forEach(entry -> skillLevels.addProperty(
                entry.getKey().serializedName(),
                entry.getValue()
            ));
        gate.add("skills", skillLevels);
        gates.add(id, gate);
    }
}
