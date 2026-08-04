package com.guildsofverra.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlayerProfile {
    public static final int SCHEMA_VERSION = 1;
    private final int schemaVersion;
    private final Map<SkillId, SkillProgress> skills;
    private final Set<String> purchasedNodes;
    private final Set<String> discoveries;
    private final Set<String> titles;
    private final String selectedTitle;

    public PlayerProfile(int schemaVersion, Map<SkillId, SkillProgress> skills, Set<String> purchasedNodes,
                         Set<String> discoveries, Set<String> titles, String selectedTitle) {
        this.schemaVersion = schemaVersion;
        EnumMap<SkillId, SkillProgress> normalized = new EnumMap<>(SkillId.class);
        for (SkillId skill : SkillId.values()) normalized.put(skill, skills.getOrDefault(skill, SkillProgress.empty()));
        this.skills = Collections.unmodifiableMap(normalized);
        this.purchasedNodes = Collections.unmodifiableSet(new LinkedHashSet<>(purchasedNodes));
        this.discoveries = Collections.unmodifiableSet(new LinkedHashSet<>(discoveries));
        this.titles = Collections.unmodifiableSet(new LinkedHashSet<>(titles));
        this.selectedTitle = selectedTitle == null ? "" : selectedTitle;
    }

    public static PlayerProfile empty() { return new PlayerProfile(SCHEMA_VERSION, Map.of(), Set.of(), Set.of(), Set.of(), ""); }
    public int schemaVersion() { return schemaVersion; }
    public SkillProgress skill(SkillId id) { return skills.get(id); }
    public Map<SkillId, SkillProgress> skills() { return skills; }
    public Set<String> purchasedNodes() { return purchasedNodes; }
    public Set<String> discoveries() { return discoveries; }
    public Set<String> titles() { return titles; }
    public String selectedTitle() { return selectedTitle; }

    public int adventurerLevel() {
        int total = 0;
        for (SkillId skill : SkillId.values()) total += skill(skill).highestLevel();
        return total / SkillId.values().length;
    }

    public int spentPoints(SkillId skill, SkillTreeDefinition tree) {
        int spent = 0;
        String prefix = skill.serializedName() + ":";
        for (String fullId : purchasedNodes) {
            if (fullId.startsWith(prefix)) {
                String local = fullId.substring(prefix.length());
                SkillNodeDefinition node = tree.nodesById().get(local);
                if (node != null) spent += node.cost();
            }
        }
        return spent;
    }

    public PlayerProfile withSkill(SkillId id, SkillProgress progress) {
        EnumMap<SkillId, SkillProgress> copy = new EnumMap<>(skills);
        copy.put(id, progress);
        return new PlayerProfile(schemaVersion, copy, purchasedNodes, discoveries, titles, selectedTitle);
    }

    public PlayerProfile withPurchasedNode(String fullNodeId) {
        Set<String> copy = new LinkedHashSet<>(purchasedNodes); copy.add(fullNodeId);
        return new PlayerProfile(schemaVersion, skills, copy, discoveries, titles, selectedTitle);
    }

    public PlayerProfile withDiscovery(String id) {
        Set<String> copy = new LinkedHashSet<>(discoveries); copy.add(id);
        return new PlayerProfile(schemaVersion, skills, purchasedNodes, copy, titles, selectedTitle);
    }

    public PlayerProfile withTitle(String id) {
        Set<String> copy = new LinkedHashSet<>(titles); copy.add(id);
        return new PlayerProfile(schemaVersion, skills, purchasedNodes, discoveries, copy, selectedTitle);
    }

    public PlayerProfile withSelectedTitle(String id) {
        return new PlayerProfile(schemaVersion, skills, purchasedNodes, discoveries, titles, id);
    }

    public List<String> sortedNodeList() { List<String> out = new ArrayList<>(purchasedNodes); Collections.sort(out); return out; }
}
