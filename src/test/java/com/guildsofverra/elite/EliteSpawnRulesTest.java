package com.guildsofverra.elite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class EliteSpawnRulesTest {
    @Test
    void spawnChanceRespectsMinimumAndMaximum() {
        assertEquals(0.0, EliteSpawnRules.spawnChance(9, 10, 0.005, 0.00045, 0.05));
        assertEquals(0.0095, EliteSpawnRules.spawnChance(10, 10, 0.005, 0.00045, 0.05), 0.000001);
        assertEquals(0.05, EliteSpawnRules.spawnChance(1000, 10, 0.005, 0.00045, 0.05));
    }

    @Test
    void budgetsRequireSpaceInBothWindows() {
        assertTrue(EliteSpawnRules.withinBudgets(2, 3, 7, 8));
        assertFalse(EliteSpawnRules.withinBudgets(3, 3, 7, 8));
        assertFalse(EliteSpawnRules.withinBudgets(2, 3, 8, 8));
        assertFalse(EliteSpawnRules.withinBudgets(0, 0, 0, 8));
    }

    @Test
    void scalingAndRewardAdjustmentStayBounded() {
        assertEquals(1.15, EliteSpawnRules.statScale(50, 0.003, 1.30), 0.000001);
        assertEquals(1.30, EliteSpawnRules.statScale(1000, 0.003, 1.30), 0.000001);
        assertEquals(2.0, EliteSpawnRules.rewardAdjustment(2.0, 4.0), 0.000001);
        assertEquals(1.0, EliteSpawnRules.rewardAdjustment(2.0, 1.0), 0.000001);
    }
}
