package com.guildsofverra.elite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EliteVariantRegistryTest {
    @Test
    void containsOriginalAndExpandedVariantsWithUniqueIds() {
        var variants = EliteVariantRegistry.all();
        Set<String> ids = variants.stream()
            .map(EliteVariantDefinition::id)
            .collect(Collectors.toSet());

        assertEquals(16, variants.size());
        assertEquals(variants.size(), ids.size());
        assertTrue(ids.containsAll(Set.of(
            "tank_zombie",
            "volatile_creeper",
            "plague_husk",
            "frostbound_stray",
            "hexbinder_witch",
            "raid_captain_pillager",
            "ironhide_ravager",
            "berserker_piglin",
            "ashen_wither_skeleton",
            "magma_colossus",
            "voidstalker_enderman"
        )));
        assertTrue(variants.stream().allMatch(
            variant -> variant.baseEntity().startsWith("minecraft:")
        ));
    }

    @Test
    void keepsVoidstalkerRestrictedToTheEnd() {
        assertTrue(EliteVariantRegistry.matching("minecraft:enderman", false).isEmpty());
        assertEquals(
            Set.of("voidstalker_enderman"),
            EliteVariantRegistry.matching("minecraft:enderman", true).stream()
                .map(EliteVariantDefinition::id)
                .collect(Collectors.toSet())
        );
    }

    @Test
    void preservesMultipleRolesForSharedBaseEntities() {
        assertEquals(
            Set.of("armoured_skeleton", "marksman_skeleton"),
            EliteVariantRegistry.matching("minecraft:skeleton", false).stream()
                .map(EliteVariantDefinition::id)
                .collect(Collectors.toSet())
        );
    }
}
