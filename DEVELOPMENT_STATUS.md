# Development status — 0.1.0-dev.1

## Implemented in this snapshot

- Complete Minecraft 26.2 Fabric/Gradle project structure.
- Five skills: Exploration, Fishing, Cooking, Mining and Combat.
- Level 0–100 progression and approved XP formula.
- One permanent point per newly reached level.
- Adventurer Level calculated from highest-ever levels.
- Immutable, codec-backed player profile for Fabric Data Attachments.
- Profile persistence, copy-on-death and client synchronization.
- Data-driven loading of all five 100-point skill trees.
- Server-side node purchase and prestige request handling.
- Administrative `/gv` command foundation.
- Hard-lock requirement engine and item-tag mappings.
- Nether, End and Elytra requirement definitions.
- Basic mining, combat and exploration XP event hooks.
- Basic equipment-use/equip enforcement hooks.
- Vanilla-themed elite variant definitions and conversion service foundation.
- Discovery/title definitions and player-profile storage.
- Basic journal screen and `J` key binding.
- Default English and Dutch language files.
- Unit tests and standalone project validator.
- GitHub Actions build workflow using Java 25.

## Deliberately incomplete before the first playable release

These systems need a real Minecraft 26.2 compile/run pass and in-game testing before they can be called finished:

- Final event coverage for every Fishing and Cooking XP source.
- Persistent placed-ore anti-exploit tracking across chunk unloads.
- Full passive-bonus application for every tree node.
- Final journal tree rendering, discoveries browser and settings pages.
- Final elite equipment/visual variation and spawn-budget tuning.
- Full title presentation integrations.
- Config hot reload and detailed validation diagnostics.
- GameTests for hard-lock bypass attempts and dimension return safety.

The repository is intentionally versioned as a development snapshot rather than falsely labelled a complete v1 release.
