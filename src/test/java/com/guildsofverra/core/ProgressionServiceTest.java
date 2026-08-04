package com.guildsofverra.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class ProgressionServiceTest {
    @Test void oneMaxedSkillEqualsTwentyAdventurerLevels() {
        PlayerProfile profile = PlayerProfile.empty().withSkill(SkillId.MINING, new SkillProgress(100,0,100,0,100));
        assertEquals(20, profile.adventurerLevel());
    }
    @Test void allSkillsAtFiftyEqualsFifty() {
        PlayerProfile profile = PlayerProfile.empty();
        for (SkillId skill: SkillId.values()) profile = profile.withSkill(skill, new SkillProgress(50,0,50,0,50));
        assertEquals(50, profile.adventurerLevel());
    }
    @Test void prestigeKeepsHighestAndEarnedPoints() {
        SkillProgress maxed = new SkillProgress(100,0,100,0,100);
        SkillProgress reset = maxed.withPrestigeReset();
        assertEquals(0, reset.level()); assertEquals(100, reset.highestLevel()); assertEquals(100, reset.earnedPoints()); assertEquals(1, reset.prestige());
    }
}
