# Development status — 0.1.0-dev.5

## Implemented and compiling in this snapshot

### Core progression

- Complete Minecraft 26.2 Fabric/Gradle project structure.
- Five skills: Exploration, Fishing, Cooking, Mining and Combat.
- Level 0–100 progression, prestige and the approved XP formula.
- Permanent skill points and Adventurer Level from highest-ever levels.
- Persistent, synchronized player profiles through Fabric Data Attachments.
- Data-driven loading of all five 100-point skill trees.
- Server-side node purchase, prestige and administrative `/gv` commands.
- Mining, Combat, Exploration, Fishing and Cooking XP event coverage.
- Generated progression configuration for XP values and feedback settings.
- Functional ore-XP, extra-fish and extra-cooked-output purchased-node passives.

### Progression gates and elites from dev.4

- Readable hard-lock feedback with required nodes, levels and current progress.
- Recurring armour and Elytra enforcement against save, command and mod bypasses.
- Unauthorized Elytra-flight cancellation and safe item return.
- Nether and End progression gates with complete requirement summaries.
- Safe pre-portal return history, transfer guards and denial-message cooldowns.
- Generated elite configuration for rarity, caps, scaling, rewards and abilities.
- Seven vanilla-themed elite variants with names, attributes and role equipment.
- Nearby and regional elite budgets to prevent clustering and excessive server load.
- Elite on-hit identities: poison, slowness, weakness and fire where appropriate.
- Configurable elite Combat XP and one-time Exploration XP for first discoveries.
- Persistent elite bestiary discoveries synchronized immediately to the client.

### Interactive dev.5 journal

- Overview, Exploration, Fishing, Cooking, Mining, Combat, Collections, Gates and Unlocks tabs.
- Spatial skill-tree maps derived from the real loaded tree definitions.
- Deterministic category lanes and prerequisite connection paths.
- Bounded arrow-button panning and 50–175% zoom.
- Purchased, purchasable, level-locked, prerequisite-locked and point-locked node states.
- Safe paged node controls beside the visual tree map.
- Direct server-authoritative node purchasing from the journal.
- Client and server journal-action rate limits and pending-request protection.
- Prestige eligibility preview, consequence explanation and two-step confirmation.
- Separate Titles, Discoveries and Bestiary collection sections with paging.
- Server-authoritative title selection and clear-active-title support.
- Hidden-safe bestiary entries that expose details only after discovery.
- Active passive-bonus summaries derived server-side from purchased nodes.
- Sixteen server-derived tool, weapon, shield, armour and Elytra unlock cards.
- Live Nether and End requirement state.
- Profile payload schema version 3 with passive, gate, unlock and bestiary data.
- Dedicated journal-action result packets for success, denial and rate-limit feedback.
- Expanded English and Belgian-Dutch translation catalogues with parity testing.

### Automated validation

- Standalone JSON, skill-tree, point-total, prerequisite and XP-curve validation.
- Unit tests for progression, titles, passives, elite rules and safe returns.
- Deterministic spatial-layout and prerequisite-edge tests.
- Same-level non-overlap tests.
- Tree pan-bound and cursor-anchored zoom tests.
- English/Belgian-Dutch translation-key parity test.
- Java 25 GitHub Actions compilation, tests and artifact packaging.

## Requires in-game verification for dev.5

### Startup and compatibility

- Existing dev.3/dev.4 profile compatibility and persistence.
- Dev.5 JAR metadata and startup log.
- Profile payload schema 3 synchronization on join and reconnect.
- Configuration generation and edited-value loading after restart.

### Journal layout and interaction

- All tabs at small, normal and ultrawide GUI scales.
- Spatial map visibility, panning, zoom and prerequisite paths for all five skills.
- Node state accuracy against live levels, points and prerequisites.
- Node purchasing success, denial, rapid clicks and reconnect behavior.
- Prestige preview, confirmation timeout, success and maximum-rank handling.
- Collection paging, title selection, clear-title behavior and bestiary secrecy.
- Passive summaries and all sixteen Unlocks entries.
- Nether/End gate state updating after progression changes.
- English and Belgian-Dutch presentation once journal strings are fully wired to translation keys.

### Multiplayer and stability

- Simultaneous journal actions from multiple players.
- No profile, title, pending-action or bestiary state leaking between players.
- Connection loss during a pending purchase, prestige or title request.
- Tick-time behavior with elites, equipment audits and several open journals.
- Longer ordinary survival sessions without repeated log errors.

## Remaining before dev.5 promotion

- Complete runtime testing of dev.3 and dev.4 and carry any fixes through the branch stack.
- Wire the prepared translation catalogue into every remaining hard-coded journal label.
- Add direct spatial-map node selection and a selected-node detail/purchase panel.
- Add hover help and optional richer tooltips.
- Perform keyboard-navigation and narrator/accessibility review in game.
- Complete small, normal and ultrawide GUI checks.
- Complete multiplayer journal-action testing.
- Update the candidate only for confirmed runtime bugs, layout issues and balance feedback.

## Deliberately incomplete before the first beta

- Persistent placed-ore anti-exploit tracking across chunk unloads.
- Gameplay application for every remaining passive-bonus type in all five trees.
- Final title presentation above players and in chat.
- Configuration hot reload and detailed validation diagnostics.
- Dedicated GameTests for every hard-lock and dimension bypass path.
- Longer survival-world progression and balance tuning.

Dev.5 remains a stacked test candidate on its development branch. Dev.4 must be runtime-verified and promoted first; `main` remains the playable dev.3 release until that happens.
