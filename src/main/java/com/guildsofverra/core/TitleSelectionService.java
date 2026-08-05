package com.guildsofverra.core;

public final class TitleSelectionService {
    private TitleSelectionService() {}

    public static PurchaseResult select(PlayerProfile profile, String titleId) {
        String normalized = titleId == null ? "" : titleId.trim();
        if (normalized.isEmpty()) {
            if (profile.selectedTitle().isEmpty()) {
                return PurchaseResult.failure(profile, "No active title to clear");
            }
            return PurchaseResult.success(profile.withSelectedTitle(""), "Active title cleared");
        }
        if (!profile.titles().contains(normalized)) {
            return PurchaseResult.failure(profile, "Title is not unlocked: " + normalized);
        }
        if (normalized.equals(profile.selectedTitle())) {
            return PurchaseResult.failure(profile, "Title is already active");
        }
        return PurchaseResult.success(
            profile.withSelectedTitle(normalized),
            "Selected title " + normalized
        );
    }
}
