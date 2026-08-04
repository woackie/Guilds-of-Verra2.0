package com.guildsofverra.core;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SkillId {
    EXPLORATION, FISHING, COOKING, MINING, COMBAT;

    public String serializedName() { return name().toLowerCase(Locale.ROOT); }

    public static Optional<SkillId> parse(String value) {
        return Arrays.stream(values()).filter(skill -> skill.serializedName().equalsIgnoreCase(value)).findFirst();
    }
}
