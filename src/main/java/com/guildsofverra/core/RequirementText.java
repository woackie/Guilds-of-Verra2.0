package com.guildsofverra.core;

import java.util.Locale;

/** Converts internal requirement identifiers into player-facing labels. */
public final class RequirementText {
    private RequirementText() {}

    public static String node(String fullNodeId) {
        int separator = fullNodeId.indexOf(':');
        if (separator < 0) {
            return titleCase(fullNodeId);
        }

        String skill = fullNodeId.substring(0, separator);
        String node = fullNodeId.substring(separator + 1);
        return titleCase(skill) + " — " + titleCase(node);
    }

    public static String skill(SkillId skill) {
        return titleCase(skill.serializedName());
    }

    static String titleCase(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("[_\\-\\s]+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
        return result.toString();
    }
}
