# Development status — 0.1.0-dev.3

## Implemented and compiling in this snapshot

- Complete Minecraft 26.2 Fabric/Gradle project structure.
- Five skills: Exploration, Fishing, Cooking, Mining and Combat.
- Level 0–100 progression, prestige and approved XP formula.
- Permanent skill points and Adventurer Level from highest-ever levels.
- Persistent, synchronized player profiles through Fabric Data Attachments.
- Data-driven loading of all five 100-point skill trees.
- Server-side node purchase, prestige and administrative `/gv` commands.
- Hard-lock requirement engine, item mappings and dimension requirements.
- Mining, Combat and Exploration XP event coverage.
- Fishing XP classified as fish, treasure, junk or fallback catches.
- Cooking XP from collected cooked and prepared food statistics.
- Generated progression configuration for XP values and feedback settings.
- First functional purchased-node passives: ore XP, extra fish and extra cooked output.
- Improved journal with XP bars, point totals, prestige and collection summaries.
- Vanilla-themed elite variant definitions and conversion foundation.
- Default English and Dutch language files.
- Unit tests, standalone data validation and Java 25 GitHub Actions builds.

## Requires in-game verification for dev.3

- Fishing category detection across normal, enchanted and unusual catches.
- Cooking statistics across furnaces, smokers, campfires and crafting recipes.
- Extra-fish and extra-cooked-output passive probabilities.
- Generated configuration creation and edited-value loading.
- Journal layout at small and ultrawide resolutions.
- Multiplayer isolation for all new progression statistics.

## Deliberately incomplete before the first beta

- Persistent placed-ore anti-exploit tracking across chunk unloads.
- Passive-bonus application for every remaining tree-node bonus type.
- Interactive journal tree rendering, node purchasing, discoveries and settings pages.
- Final elite equipment, visual variation and spawn-budget tuning.
- Full title presentation integrations.
- Config hot reload and detailed validation diagnostics.
- GameTests for hard-lock bypass attempts and dimension return safety.

The project remains a development snapshot until the runtime checklist and longer survival-world balance tests pass.
