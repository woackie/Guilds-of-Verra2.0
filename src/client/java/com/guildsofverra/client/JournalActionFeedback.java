package com.guildsofverra.client;

public final class JournalActionFeedback {
    private static Snapshot snapshot = new Snapshot(0L, "", false, "");

    private JournalActionFeedback() {}

    public static void update(String action, boolean success, String message) {
        snapshot = new Snapshot(
            snapshot.revision() + 1L,
            action == null ? "" : action,
            success,
            message == null ? "" : message
        );
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public record Snapshot(long revision, String action, boolean success, String message) {}
}
