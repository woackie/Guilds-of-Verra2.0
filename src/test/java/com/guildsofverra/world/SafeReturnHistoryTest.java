package com.guildsofverra.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class SafeReturnHistoryTest {
    @Test
    void selectsPositionBeforeTheMostRecentPortalWindow() {
        UUID player = UUID.randomUUID();
        SafeReturnHistory<String> history = new SafeReturnHistory<>(6, 2);

        history.record(player, "overworld", 1, 64, 1, 0, 0);
        history.record(player, "overworld", 2, 64, 2, 0, 0);
        history.record(player, "overworld", 3, 64, 3, 0, 0);
        history.record(player, "overworld", 4, 64, 4, 0, 0);

        SafeReturnHistory.Snapshot<String> selected = history.select(player, "overworld");
        assertEquals(2.0, selected.x());
        assertEquals(2.0, selected.z());
    }

    @Test
    void ignoresSnapshotsFromAnotherDimension() {
        UUID player = UUID.randomUUID();
        SafeReturnHistory<String> history = new SafeReturnHistory<>(6, 1);

        history.record(player, "overworld", 10, 70, 10, 0, 0);
        history.record(player, "nether", 20, 70, 20, 0, 0);
        history.record(player, "overworld", 11, 70, 11, 0, 0);

        SafeReturnHistory.Snapshot<String> selected = history.select(player, "overworld");
        assertEquals("overworld", selected.dimension());
        assertEquals(10.0, selected.x());
    }

    @Test
    void removesHistoryForOfflinePlayers() {
        UUID online = UUID.randomUUID();
        UUID offline = UUID.randomUUID();
        SafeReturnHistory<String> history = new SafeReturnHistory<>(4, 1);

        history.record(online, "overworld", 1, 64, 1, 0, 0);
        history.record(offline, "overworld", 2, 64, 2, 0, 0);
        history.retainPlayers(Set.of(online));

        assertNull(history.select(offline, "overworld"));
        assertEquals(1.0, history.select(online, "overworld").x());
    }
}
