package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class ProgressionRewardMathTest {
    @Test
    void fishingCategoriesUseDifferentXpValues() {
        assertEquals(259, ProgressionRewardMath.fishingXp(2, 1, 2, 1, 35, 100, 12, 65));
    }

    @Test
    void bonusRollsClampChance() {
        RandomGenerator random = new RandomGenerator() {
            @Override public long nextLong() { return 0L; }
            @Override public double nextDouble() { return 0.25; }
        };
        assertEquals(3, ProgressionRewardMath.bonusRolls(3, 2.0, random));
        assertEquals(0, ProgressionRewardMath.bonusRolls(3, -1.0, random));
    }
}
