package com.guildsofverra.client;

import com.guildsofverra.network.PrestigePayload;
import com.guildsofverra.network.PurchaseNodePayload;
import com.guildsofverra.network.SelectTitlePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class JournalClientActions {
    private static final long ACTION_COOLDOWN_MILLIS = 300L;
    private static long nextActionAt;

    private JournalClientActions() {}

    public static boolean purchaseNode(String skill, String node) {
        long now = System.currentTimeMillis();
        if (!allowAction(now)
            || skill == null
            || skill.isBlank()
            || node == null
            || node.isBlank()
            || !ClientPlayNetworking.canSend(PurchaseNodePayload.TYPE)) {
            return false;
        }

        nextActionAt = now + ACTION_COOLDOWN_MILLIS;
        ClientPlayNetworking.send(new PurchaseNodePayload(skill, node));
        return true;
    }

    public static boolean prestigeSkill(String skill) {
        long now = System.currentTimeMillis();
        if (!allowAction(now)
            || skill == null
            || skill.isBlank()
            || !ClientPlayNetworking.canSend(PrestigePayload.TYPE)) {
            return false;
        }

        nextActionAt = now + ACTION_COOLDOWN_MILLIS;
        ClientPlayNetworking.send(new PrestigePayload(skill));
        return true;
    }

    public static boolean selectTitle(String titleId) {
        long now = System.currentTimeMillis();
        String normalized = titleId == null ? "" : titleId.trim();
        if (!allowAction(now) || !ClientPlayNetworking.canSend(SelectTitlePayload.TYPE)) {
            return false;
        }

        nextActionAt = now + ACTION_COOLDOWN_MILLIS;
        ClientPlayNetworking.send(new SelectTitlePayload(normalized));
        return true;
    }

    public static void reset() {
        nextActionAt = 0L;
    }

    private static boolean allowAction(long now) {
        return now >= nextActionAt;
    }
}
