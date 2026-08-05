package com.guildsofverra.journal;

import com.guildsofverra.content.GvContent;
import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillNodeDefinition;
import com.guildsofverra.core.SkillTreeDefinition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-derived journal summaries that keep the client free of duplicated progression rules. */
public final class JournalSummaryService {
    private static final List<GateDefinition> EQUIPMENT_GATES = List.of(
        new GateDefinition("iron_tools", "mining:iron_tool_mastery"),
        new GateDefinition("diamond_tools", "mining:diamond_tool_mastery"),
        new GateDefinition("netherite_tools", "mining:netherite_tool_mastery"),
        new GateDefinition("iron_swords", "combat:iron_blade_training"),
        new GateDefinition("diamond_swords", "combat:diamond_blade_training"),
        new GateDefinition("netherite_swords", "combat:netherite_blade_training"),
        new GateDefinition("iron_axes", "combat:iron_axe_combat"),
        new GateDefinition("diamond_axes", "combat:diamond_axe_combat"),
        new GateDefinition("netherite_axes", "combat:netherite_axe_combat"),
        new GateDefinition("bows", "combat:bow_training"),
        new GateDefinition("crossbows", "combat:crossbow_training"),
        new GateDefinition("shields", "combat:shield_training"),
        new GateDefinition("iron_armour", "combat:iron_armour_training"),
        new GateDefinition("diamond_armour", "combat:diamond_armour_training"),
        new GateDefinition("netherite_armour", "combat:netherite_armour_training"),
        new GateDefinition("elytra", "exploration:elytra_certification")
    );

    private JournalSummaryService() {}

    public static Map<String, Double> passiveBonuses(
        PlayerProfile profile,
        SkillId skill,
        SkillTreeDefinition tree
    ) {
        LinkedHashMap<String, Double> totals = new LinkedHashMap<>();
        String prefix = skill.serializedName() + ":";
        for (String purchased : profile.purchasedNodes().stream().sorted().toList()) {
            if (!purchased.startsWith(prefix)) {
                continue;
            }
            SkillNodeDefinition node = tree.nodesById().get(purchased.substring(prefix.length()));
            if (node == null) {
                continue;
            }
            Object type = node.bonus().get("type");
            Object value = node.bonus().get("value");
            if (type == null || !(value instanceof Number number)) {
                continue;
            }
            String bonusType = String.valueOf(type);
            totals.merge(bonusType, number.doubleValue(), Double::sum);
        }
        return Map.copyOf(totals);
    }

    public static List<EquipmentGate> equipmentGates(PlayerProfile profile) {
        List<EquipmentGate> gates = new ArrayList<>();
        for (GateDefinition definition : EQUIPMENT_GATES) {
            int separator = definition.nodeId().indexOf(':');
            if (separator <= 0 || separator + 1 >= definition.nodeId().length()) {
                continue;
            }

            SkillId skill = SkillId.parse(definition.nodeId().substring(0, separator)).orElse(null);
            if (skill == null) {
                continue;
            }
            String localNodeId = definition.nodeId().substring(separator + 1);
            SkillNodeDefinition node = GvContent.tree(skill).nodesById().get(localNodeId);
            if (node == null) {
                continue;
            }

            gates.add(new EquipmentGate(
                definition.id(),
                definition.nodeId(),
                profile.purchasedNodes().contains(definition.nodeId()),
                skill.serializedName(),
                node.minLevel(),
                node.cost(),
                node.category(),
                node.effect(),
                node.prerequisites()
            ));
        }
        return List.copyOf(gates);
    }

    public record EquipmentGate(
        String id,
        String nodeId,
        boolean unlocked,
        String skill,
        int minimumLevel,
        int cost,
        String category,
        String effect,
        List<String> prerequisites
    ) {
        public EquipmentGate {
            prerequisites = List.copyOf(prerequisites);
        }
    }

    private record GateDefinition(String id, String nodeId) {}
}
