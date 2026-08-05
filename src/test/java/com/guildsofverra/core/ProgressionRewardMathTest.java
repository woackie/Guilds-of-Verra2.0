package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProgressionRewardMathTest {
    @Test
    void fishingCategoriesUseDifferentXpValues() {
        assertEquals(259, ProgressionRewardMath.fishingXp(2, 1, 2, 1, 35, 100, 12, 65));
    }

    @Test
    void bonusRollsClampChance() {
        assertEquals(3, ProgressionRewardMath.bonusRolls(3, 2.0, () -> 0.25));
        assertEquals(0, ProgressionRewardMath.bonusRolls(3, -1.0, () -> 0.25));
    }
}
