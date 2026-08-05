package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TitleSelectionServiceTest {
    @Test
    void selectsOnlyUnlockedTitles() {
        PlayerProfile profile = new PlayerProfile(
            PlayerProfile.SCHEMA_VERSION,
            Map.of(),
            Set.of(),
            Set.of(),
            Set.of("guildsofverra:elite_hunter"),
            ""
        );

        PurchaseResult selected = TitleSelectionService.select(
            profile,
            "guildsofverra:elite_hunter"
        );
        assertTrue(selected.success());
        assertEquals("guildsofverra:elite_hunter", selected.profile().selectedTitle());

        PurchaseResult locked = TitleSelectionService.select(
            profile,
            "guildsofverra:unknown"
        );
        assertFalse(locked.success());
        assertEquals("", locked.profile().selectedTitle());
    }

    @Test
    void rejectsAlreadyActiveTitleAndAllowsClearing() {
        PlayerProfile profile = new PlayerProfile(
            PlayerProfile.SCHEMA_VERSION,
            Map.of(),
            Set.of(),
            Set.of(),
            Set.of("guildsofverra:elite_hunter"),
            "guildsofverra:elite_hunter"
        );

        assertFalse(
            TitleSelectionService.select(profile, "guildsofverra:elite_hunter").success()
        );

        PurchaseResult cleared = TitleSelectionService.select(profile, "");
        assertTrue(cleared.success());
        assertEquals("", cleared.profile().selectedTitle());
    }
}
