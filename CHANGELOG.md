# Changelog

## 0.1.0-dev.3 — Core Progression Expansion

- Added server-side Fishing XP for fish, treasure, junk and unclassified catches.
- Added Cooking XP for collected cooked and prepared foods.
- Added generated runtime progression configuration at `config/guildsofverra/progression.json`.
- Moved Mining, Combat and Exploration reward values into configuration.
- Applied the first purchased-node passives: ore XP, extra fish and extra cooked output.
- Added immediate Fishing/Cooking XP feedback and clearer level-up messages.
- Expanded synchronized profile data with XP and available/spent skill points.
- Upgraded the journal with XP bars, point totals, prestige and collection summaries.
- Added tests for reward classification and passive-bonus resolution.
- Preserved existing profiles and avoided retroactive XP from old vanilla statistics.
- Verified Java 25/Fabric compilation, project validation and automated unit tests.

## 0.1.0-dev.2 — First playable build

- Ported the project to the final Minecraft 26.2 APIs used by the first compiled build.
- Fixed client networking registration so the mod can initialize before joining a world.
- Verified profile persistence, XP, journal opening and core commands in game.

## 0.1.0-dev.1 — Initial repository foundation

- Created the standalone Fabric project from an empty directory.
- Added the complete approved progression data and architecture.
- Added persistent profiles, networking, commands, restrictions, prestige, elites and journal foundations.
- Added automated validation, tests and continuous-integration build configuration.
