package com.guildsofverra.client;

import com.guildsofverra.network.PurchaseNodePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class JournalClientActions {
    private static final long PURCHASE_COOLDOWN_MILLIS = 300L;
    private static long nextPurchaseAt;

    private JournalClientActions() {}

    public static boolean purchaseNode(String skill, String node) {
        long now = System.currentTimeMillis();
        if (now < nextPurchaseAt
            || skill == null
            || skill.isBlank()
            || node == null
            || node.isBlank()
            || !ClientPlayNetworking.canSend(PurchaseNodePayload.TYPE)) {
            return false;
        }

        nextPurchaseAt = now + PURCHASE_COOLDOWN_MILLIS;
        ClientPlayNetworking.send(new PurchaseNodePayload(skill, node));
        return true;
    }
}
