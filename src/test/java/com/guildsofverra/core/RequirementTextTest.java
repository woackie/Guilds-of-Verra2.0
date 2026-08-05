package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class RequirementTextTest {
    @Test
    void formatsNodeIdentifiersForPlayers() {
        assertEquals(
            "Combat — Iron Blade Training",
            RequirementText.node("combat:iron_blade_training")
        );
    }

    @Test
    void formatsSkillNamesForPlayers() {
        assertEquals("Exploration", RequirementText.skill(SkillId.EXPLORATION));
    }

    @Test
    void handlesIdentifiersWithoutNamespaces() {
        assertEquals("Elytra Certification", RequirementText.node("elytra_certification"));
    }
}
