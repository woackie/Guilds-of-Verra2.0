package com.guildsofverra.elite;

public record EliteVariantDefinition(
    String id,
    String baseEntity,
    String displayName,
    double scale,
    double healthMultiplier,
    double speedMultiplier,
    double armorBonus,
    double knockbackResistance,
    double damageMultiplier,
    double xpMultiplier
) {
    public EliteVariantDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Elite id must not be blank");
        }
        if (baseEntity == null || baseEntity.isBlank()) {
            throw new IllegalArgumentException("Elite base entity must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Elite display name must not be blank");
        }
        scale = Math.max(0.5, scale);
        healthMultiplier = Math.max(1.0, healthMultiplier);
        speedMultiplier = Math.max(0.1, speedMultiplier);
        armorBonus = Math.max(0.0, armorBonus);
        knockbackResistance = Math.max(0.0, Math.min(1.0, knockbackResistance));
        damageMultiplier = Math.max(0.1, damageMultiplier);
        xpMultiplier = Math.max(1.0, xpMultiplier);
    }

    public double rewardAdjustment() {
        return EliteSpawnRules.rewardAdjustment(healthMultiplier, xpMultiplier);
    }
}
