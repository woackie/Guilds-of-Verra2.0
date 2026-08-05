package com.guildsofverra.world;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps a short per-player position history so a blocked dimension transfer can
 * return the player to a position from before they entered a portal.
 */
public final class SafeReturnHistory<D> {
    private final int maximumSnapshots;
    private final int lookbackSnapshots;
    private final Map<UUID, Deque<Snapshot<D>>> snapshots = new HashMap<>();

    public SafeReturnHistory(int maximumSnapshots, int lookbackSnapshots) {
        if (maximumSnapshots < 1) {
            throw new IllegalArgumentException("maximumSnapshots must be positive");
        }
        if (lookbackSnapshots < 0 || lookbackSnapshots >= maximumSnapshots) {
            throw new IllegalArgumentException("lookbackSnapshots must be within the history window");
        }
        this.maximumSnapshots = maximumSnapshots;
        this.lookbackSnapshots = lookbackSnapshots;
    }

    public void record(
        UUID playerId,
        D dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
    ) {
        Deque<Snapshot<D>> history = snapshots.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        history.addLast(new Snapshot<>(dimension, x, y, z, yaw, pitch));
        while (history.size() > maximumSnapshots) {
            history.removeFirst();
        }
    }

    public Snapshot<D> select(UUID playerId, D originDimension) {
        Deque<Snapshot<D>> history = snapshots.get(playerId);
        if (history == null || history.isEmpty()) {
            return null;
        }

        Snapshot<D> oldestMatch = null;
        int matchingSnapshots = 0;
        Iterator<Snapshot<D>> iterator = history.descendingIterator();
        while (iterator.hasNext()) {
            Snapshot<D> snapshot = iterator.next();
            if (!snapshot.dimension().equals(originDimension)) {
                continue;
            }
            oldestMatch = snapshot;
            if (matchingSnapshots++ >= lookbackSnapshots) {
                return snapshot;
            }
        }
        return oldestMatch;
    }

    public void retainPlayers(Set<UUID> onlinePlayers) {
        snapshots.keySet().retainAll(onlinePlayers);
    }

    public record Snapshot<D>(
        D dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
    ) {}
}
