# Guilds of Verra 0.1.0-dev.5 — Interactive Journal & Collections

This is a stacked milestone built on `dev-0.1.0-dev.4`. It must not be merged before dev.4 is complete and promoted.

## Interactive skill-tree browser

- Add separate tabs for Exploration, Fishing, Cooking, Mining and Combat.
- Render the complete 100-point tree for each skill with pan, zoom and responsive layout behavior.
- Clearly distinguish locked, available, purchasable and purchased nodes.
- Show node name, description, level requirement, cost, prerequisites and passive effect details.
- Add prerequisite connection lines and readable branch grouping for Combat specializations.
- Preserve usability at small, standard and ultrawide GUI scales.

## Node purchasing

- Allow players to select and purchase nodes directly from the journal.
- Keep all purchases server-authoritative and reuse the existing validation rules.
- Add clear success and denial feedback for missing levels, points or prerequisites.
- Prevent duplicate requests, rapid-click abuse and stale client-state purchases.
- Refresh points, node state and derived bonuses immediately after server confirmation.

## Prestige interface

- Add a prestige overview for every skill.
- Explain the required level, current rank, retained progress and reset consequences.
- Require an explicit confirmation before sending a prestige request.
- Refresh the journal immediately after successful prestige.

## Discoveries, titles and collections

- Add a discoveries browser with discovered and undiscovered entries.
- Add title collection and active-title selection foundations.
- Surface elite encounters and bestiary-style information supplied by dev.4.
- Add collection totals, filters and category navigation.
- Avoid exposing hidden discovery details before they are earned.

## Player-facing progression information

- Add detailed XP values and progress-to-next-level information.
- Show Adventurer Level composition across all five skills.
- Surface equipment, dimension and Elytra requirements from dev.4.
- Show active purchased passives and their resolved bonus values.
- Add concise tooltips and icon-supported states so information is not communicated by color alone.

## Networking and safety

- Add only the client-to-server payloads required for journal actions.
- Validate every request against the current server profile and loaded tree data.
- Rate-limit journal actions and reject malformed node, skill, title and prestige identifiers.
- Preserve compatibility with existing dev.2, dev.3 and dev.4 profiles.

## Tests and release requirements

- Add unit coverage for journal state derivation and action validation.
- Add tests for invalid purchases, stale state, insufficient points and missing prerequisites.
- Validate all five trees can be rendered without disconnected or missing nodes.
- Pass Java 25/Fabric compilation, project validation and automated tests.
- Complete multiplayer and multiple-resolution in-game checks before promotion.

## Branch policy

- `main` remains the latest playable release.
- `dev-0.1.0-dev.4` owns gate and elite implementation.
- `dev-0.1.0-dev.5` owns the interactive journal and collection experience.
- Fixes discovered in earlier playable builds must be applied to the earliest affected branch and then carried forward through the stack.
