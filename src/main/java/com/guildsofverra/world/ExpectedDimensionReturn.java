package com.guildsofverra.world;

import java.util.Objects;

/**
 * Identifies the one dimension transfer caused by a queued gate return.
 * Unrelated transfers must still pass through the normal gate checks.
 */
record ExpectedDimensionReturn<D>(D destination, long expiresAt) {
    ExpectedDimensionReturn {
        Objects.requireNonNull(destination, "destination");
    }

    boolean matches(D actualDestination, long now) {
        return expiresAt > now && destination.equals(actualDestination);
    }

    boolean isExpired(long now) {
        return expiresAt <= now;
    }
}
