package com.guildsofverra.core;

public record PurchaseResult(boolean success, PlayerProfile profile, String message) {
    public static PurchaseResult failure(PlayerProfile profile, String message) { return new PurchaseResult(false, profile, message); }
    public static PurchaseResult success(PlayerProfile profile, String message) { return new PurchaseResult(true, profile, message); }
}
