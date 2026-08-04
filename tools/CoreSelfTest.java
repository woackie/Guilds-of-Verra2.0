import com.guildsofverra.core.*;
public final class CoreSelfTest {
    public static void main(String[] args) {
        if (XpCurve.totalXpToMax() != 561950L) throw new AssertionError("XP total");
        PlayerProfile p = PlayerProfile.empty().withSkill(SkillId.MINING, new SkillProgress(100,0,100,0,100));
        if (p.adventurerLevel() != 20) throw new AssertionError("Adventurer level");
        for (SkillId skill : SkillId.values()) p = p.withSkill(skill, new SkillProgress(100,0,100,0,100));
        if (p.adventurerLevel() != 100) throw new AssertionError("Max adventurer level");
        System.out.println("Guilds of Verra core self-test passed; XP total=" + XpCurve.totalXpToMax());
    }
}
