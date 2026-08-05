# Development status — 0.1.0-dev.4

## Implemented and compiling in this snapshot

- Complete Minecraft 26.2 Fabric/Gradle project structure.
- Five skills: Exploration, Fishing, Cooking, Mining and Combat.
- Level 0–100 progression, prestige and approved XP formula.
- Permanent skill points and Adventurer Level from highest-ever levels.
- Persistent, synchronized player profiles through Fabric Data Attachments.
- Data-driven loading of all five 100-point skill trees.
- Server-side node purchase, prestige and administrative `/gv` commands.
- Mining, Combat, Exploration, Fishing and Cooking XP event coverage.
- Generated progression configuration for XP values and feedback settings.
- First functional purchased-node passives: ore XP, extra fish and extra cooked output.
- Readable hard-lock feedback with required nodes, levels and current progress.
- Recurring armour and Elytra enforcement against save, command and mod bypasses.
- Unauthorized Elytra-flight cancellation and safe item return.
- Nether and End progression gates with full requirement summaries.
- Safe pre-portal return history, transfer guards and denial-message cooldowns.
- Generated elite configuration for rarity, caps, scaling, rewards and abilities.
- Seven vanilla-themed elite variants with names, attributes and role equipment.
- Nearby and regional elite budgets to prevent clustering and excessive server load.
- Elite on-hit identities: poison, slowness, weakness and fire where appropriate.
- Configurable elite Combat XP and one-time Exploration XP for first discoveries.
- Persistent elite bestiary discoveries synchronized immediately to the client.
- Expanded profile payload with discoveries, titles, selected title, gate state and hidden-safe bestiary entries.
- Default English and Dutch language files.
- Unit tests, standalone data validation and Java 25 GitHub Actions builds.

## Requires in-game verification for dev.4

- Existing dev.3 profile compatibility and persistence.
- Every tool, weapon, shield, armour and Elytra restriction path.
- Equipment inserted through commands, dispensers or other mods.
- Elytra equip and take-off behavior at locked and unlocked progression states.
- Nether and End denial from normal portals, commands and unusual transfer paths.
- Safe return positions near walls, drops, lava, overlapping portals and multiplayer portals.
- Elite rarity, nearby caps and regional caps during ordinary survival play.
- Elite attribute scaling at low, medium and high Adventurer Levels.
- Elite equipment, names, abilities and Combat XP rewards.
- One-time bestiary discovery and Exploration XP per variant per player.
- Elite configuration generation and edited-value loading after restart.
- Multiplayer isolation for gates, discoveries, cooldowns and elite rewards.
- Tick-time behavior with many hostile mobs and several players.

## Deliberately incomplete before the first beta

- Persistent placed-ore anti-exploit tracking across chunk unloads.
- Passive-bonus application for every remaining tree-node bonus type.
- Interactive journal tree rendering and direct node purchasing.
- Full discoveries, bestiary, titles and settings pages in the journal.
- Final title presentation integrations.
- Config hot reload and detailed validation diagnostics.
- Dedicated GameTests for every hard-lock and dimension bypass path.
- Longer survival-world progression and balance testing.

Dev.4 remains a release candidate on its development branch until the runtime checklist passes and dev.3 playtest feedback is carried forward.
