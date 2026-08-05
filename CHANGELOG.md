# Changelog

## 0.1.0-dev.6 — Expanded Encounters

- Expanded the vanilla-themed elite roster from seven to exactly twenty-five variants.
- Added Plague Husk, Frostbound Stray, Hexbinder Witch, Raid Captain Pillager and Ironhide Ravager.
- Added Berserker Piglin, Ashen Wither Skeleton, Magma Colossus and End-only Voidstalker Enderman.
- Added Sporeguard Bogged, Cave Stalker, Tempest Breeze and Swarmheart Silverfish.
- Added Cinder Blaze, Soulreaver Ghast, End Sentinel Shulker, Abyssal Guardian and Dreadwing Phantom.
- Added a central deterministic elite registry with unique-ID and vanilla-entity coverage tests.
- Preserved Adventurer-level scaling, nearby/regional spawn budgets and reward calculations for every variant.
- Added configurable Poison, Slowness, Blindness, Levitation, Weakness, Wither, Darkness, Mining Fatigue, freezing and fire identities.
- Added role equipment for humanoid expansion variants.
- Extended automatic bestiary discoveries and first-discovery Exploration XP to all twenty-five variants.
- Restricted Voidstalker Endermen to the End while preserving ordinary Endermen elsewhere.
- Added configurable player-targeted Hunt Events with warnings, level-scaled packs and global concurrency caps.
- Changed the default Hunt Event per-player cooldown from 45 to 60 minutes.
- Added safe Hunt Event ring spawning, repeated target/path refresh and bounded pursuit distance.
- Added victory, timeout, death, disconnect, dimension-change, chunk-reload and server-shutdown Hunt cleanup.
- Kept Hunt Event mobs outside ordinary elite conversion and elite spawn budgets.
- Added generated Hunt Event configuration at `config/guildsofverra/hunt_events.json`.
- Added a mutually-exclusive server-wide survival event scheduler with a 30-minute startup grace period and 60-minute minimum separation.
- Added **Blood Moon**, forcing midnight while nearby monsters gain Speed, Strength and stronger player acquisition.
- Added **Severe Thunderstorm**, forcing violent weather and configurable lightning pressure around eligible Overworld players.
- Added **Cave Tremor**, applying underground Mining Fatigue and spawning bounded Silverfish/Cave Spider hazards.
- Added **Nether Surge**, applying Darkness, spawning dimension-specific pressure waves and empowering nearby Nether monsters.
- Added **Predator Migration**, sending bounded Spider, Cave Spider and Wolf groups through occupied Overworld regions.
- Added **Long Night**, holding the Overworld clock around midnight for an extended survival period.
- Added **Restless Dead**, allowing supported undead to revive once after a configurable delay.
- Added event-mob ownership, caps, unload/reload cleanup and elite-budget exclusion.
- Added generated world-event configuration at `config/guildsofverra/world_events.json`.
- Added eligibility, probability, cooldown, weighted-selection, spawn-cap and configuration-normalization tests.
- Added rapid runtime checklists for elites, Hunt Events and all seven world events.
- Added operator commands to start, stop and inspect world events and targeted Hunt Events.
- Added an autocompleted elite spawn command with an optional 1–25 count for deterministic testing.
- Migrated time control to Minecraft 26.2 world clocks and weather control to the server-owned weather system.
- Updated build and synchronized version identity to `0.1.0-dev.6`.

## 0.1.0-dev.5 — Interactive Journal & Collections

- Added clickable Overview, five skill, Collections, Gates and Unlocks journal tabs.
- Added a spatial skill-tree map with deterministic category lanes and prerequisite connection paths.
- Added bounded tree panning and zoom levels from 50% to 175%.
- Added real purchased, purchasable, level-locked, prerequisite-locked and point-locked node states.
- Added server-authoritative node purchasing directly from the journal with client/server rate limits.
- Added pending-request protection and immediate profile synchronization after successful purchases.
- Added prestige eligibility information, reset-consequence text and a two-step confirmation flow.
- Added server-authoritative prestige handling and synchronized confirmation state.
- Added separate Titles, Discoveries and Bestiary collection sections with independent paging.
- Added server-authoritative active-title selection and clear-title support.
- Added hidden-safe elite bestiary entries that reveal details only after discovery.
- Added active passive-bonus summaries derived from purchased skill-tree nodes.
- Added a synchronized catalogue for sixteen tool, weapon, shield, armour and Elytra unlock gates.
- Added live Nether and End requirement summaries and current eligibility.
- Upgraded the synchronized profile payload to schema version 3.
- Added a dedicated clientbound journal-action result payload for success, denial and rate-limit feedback.
- Expanded English and Belgian-Dutch journal vocabulary and added an automated translation-parity test.
- Added deterministic spatial-layout, prerequisite-edge, pan-bound, zoom-anchor, title-selection and passive-summary tests.
- Preserved compatibility with existing dev.2, dev.3 and dev.4 player profiles.
- Verified project validation, Java 25/Fabric compilation, automated tests and artifact packaging in CI.

## 0.1.0-dev.4 — Progression Gates & Elite Encounters

- Replaced raw restriction IDs with readable node, skill and level requirements.
- Added cooldowns so repeated blocked actions and portal attempts do not flood chat.
- Added recurring server-side armour and Elytra audits to close save, command and mod bypasses.
- Stopped unauthorized Elytra flight and safely returned removed equipment to inventory or the world.
- Added safe Nether and End returns using pre-portal position history and transfer-loop protection.
- Added complete dimension requirement summaries and exposed gate state to the synchronized profile payload.
- Added generated elite configuration at `config/guildsofverra/elites.json`.
- Added seven vanilla-themed elite roles with scaling, names, equipment and configurable spawn budgets.
- Added Venom Spider poison, Marksman Skeleton slowness, Bulwark Drowned weakness and Volatile Creeper fire effects.
- Added configurable elite Combat XP scaling and first-discovery Exploration XP.
- Added persistent bestiary discoveries for the first defeated elite of each variant.
- Expanded client synchronization with discovery/title IDs, selected title, dimension gates and hidden-safe bestiary data.
- Added unit tests for requirement text, safe returns, elite spawn rules, scaling, caps and reward calculations.
- Verified project validation, Java 25/Fabric compilation, tests and artifact packaging in CI.

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
