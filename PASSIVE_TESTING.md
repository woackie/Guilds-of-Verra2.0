# Dev.7 focused passive playtest

Use a copy of a test world. Enable cheats or operator permission level 2.

## Fast setup

For each skill, compare a blank profile with a mastered tree:

```text
/gv reset @s
/gv test master-skill @s exploration
/gv test master-skill @s fishing
/gv test master-skill @s cooking
/gv test master-skill @s mining
/gv test master-skill @s combat
```

The journal should immediately list the active passive totals after each command.

## Exploration

- Compare walking speed before/after mastery; vehicles and Elytra should not inherit it.
- Sprint, swim and jump while watching hunger drain over time.
- Take controlled fall damage at the same height.
- Swim and remain underwater; movement and air time should increase.
- Defeat a previously undiscovered elite and compare the Exploration XP reward.

## Fishing

- Time several casts; the mastered average wait should be shorter.
- Watch rod durability across many catches.
- Confirm common fish bonuses, occasional duplicate treasure and reduced junk.
- Kill Drowned/Guardians and check occasional additional ordinary drops.

## Cooking

- Collect prepared outputs and confirm occasional extra output and returned ingredients.
- Eat tagged prepared food and compare use time and saturation.
- Use a tagged modded prepared food with positive or negative effects if available; durations should scale in the correct direction.

## Mining

- Time the same block/tool combination before and after mastery.
- Compare tool durability across a large controlled block sample.
- Mine ores and verify the ore-XP bonus.
- Mine tagged common resources and watch for occasional extra drops.
- Compare fall/explosion damage underground and at the surface.

## Combat

- Confirm maximum health rises by four points.
- Compare incoming damage from an identical attack.
- Compare sword, sweeping, axe and bow/crossbow damage.
- Compare bow draw/crossbow reload time.
- Block repeated hits and compare shield durability and knockback.

## Regression

- Reconnect and confirm passives remain active without stacking twice.
- Die/respawn and confirm health/attribute passives reapply once.
- Reset the profile and confirm transient modifiers disappear.
- Recheck the journal, End gate, one elite, one Hunt Event and one world event.
- Report any crash with `latest.log` and the crash report.
