package com.guildsofverra.data;

import com.guildsofverra.core.PlayerProfile;
import com.guildsofverra.core.SkillId;
import com.guildsofverra.core.SkillProgress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class ProfileCodecs {
    private ProfileCodecs() {}

    public static final Codec<SkillProgress> SKILL_PROGRESS = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("level", 0).forGetter(SkillProgress::level),
        Codec.LONG.optionalFieldOf("xp", 0L).forGetter(SkillProgress::xp),
        Codec.INT.optionalFieldOf("highest_level", 0).forGetter(SkillProgress::highestLevel),
        Codec.INT.optionalFieldOf("prestige", 0).forGetter(SkillProgress::prestige),
        Codec.INT.optionalFieldOf("earned_points", 0).forGetter(SkillProgress::earnedPoints)
    ).apply(instance, SkillProgress::new));

    private static final Codec<Map<String, SkillProgress>> SKILL_MAP = Codec.unboundedMap(Codec.STRING, SKILL_PROGRESS);

    public static final Codec<PlayerProfile> PLAYER_PROFILE = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("schema_version", PlayerProfile.SCHEMA_VERSION).forGetter(PlayerProfile::schemaVersion),
        SKILL_MAP.optionalFieldOf("skills", Map.of()).forGetter(profile -> profile.skills().entrySet().stream().collect(java.util.stream.Collectors.toMap(e -> e.getKey().serializedName(), Map.Entry::getValue))),
        Codec.STRING.listOf().optionalFieldOf("purchased_nodes", List.of()).forGetter(PlayerProfile::sortedNodeList),
        Codec.STRING.listOf().optionalFieldOf("discoveries", List.of()).forGetter(profile -> List.copyOf(profile.discoveries())),
        Codec.STRING.listOf().optionalFieldOf("titles", List.of()).forGetter(profile -> List.copyOf(profile.titles())),
        Codec.STRING.optionalFieldOf("selected_title", "").forGetter(PlayerProfile::selectedTitle)
    ).apply(instance, ProfileCodecs::createProfile));

    private static PlayerProfile createProfile(int schema, Map<String, SkillProgress> rawSkills, List<String> nodes,
                                               List<String> discoveries, List<String> titles, String selectedTitle) {
        EnumMap<SkillId, SkillProgress> skills = new EnumMap<>(SkillId.class);
        for (Map.Entry<String, SkillProgress> entry : rawSkills.entrySet()) SkillId.parse(entry.getKey()).ifPresent(id -> skills.put(id, entry.getValue()));
        return new PlayerProfile(schema, skills, new LinkedHashSet<>(nodes), new LinkedHashSet<>(discoveries), new LinkedHashSet<>(titles), selectedTitle);
    }
}
