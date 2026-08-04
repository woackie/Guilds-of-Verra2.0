# Guilds of Verra architecture

## Design goals

Guilds of Verra is organized around a small dependency-free progression core and a thin
Minecraft/Fabric integration layer. The server owns all authoritative state and validates
XP, node purchases, prestige requests, equipment use and dimension access.

## Modules

### `core`

Pure Java records and services for:

- five skills and their progression state;
- the level 0–100 XP curve;
- immutable player profiles;
- Adventurer Level calculation;
- node prerequisites and point spending;
- prestige transitions;
- requirement results.

The core can be compiled and tested without launching Minecraft.

### `data`

Fabric Data Attachment registration and Mojang codecs. Player profiles are persistent,
initialized on first access and retained across death. The profile schema is explicitly
versioned to support future migrations.

### `content`

Loads the default skill-tree definitions and verifies that each tree costs exactly 100
points. This snapshot packages defaults as resources. A later release will move loading to
a reload listener so server datapacks can override content safely at runtime.

### `event`

Awards XP from server-side gameplay events. The first snapshot covers mining, combat and
coarse exploration-region movement. Fishing, Cooking and richer discovery events are marked
for the first playable build.

### `restriction`

Context-aware hard locks. Mining governs tool use on blocks; Combat governs sword and axe
attacks; ranged weapons and shields are checked when activated; armour and Elytra are checked
when equipped. Item tags are the compatibility boundary for modded equipment.

### `world`

Checks dimension requirements after a level transition and returns an under-levelled player
to the origin dimension. The return path needs dedicated GameTests before release.

### `elite`

Converts a tightly limited percentage of newly loaded vanilla hostile mobs into lightweight
elite variants by adjusting standard attributes. It does not add custom AI or continuous
world scans.

### `network`

Uses object-based Fabric payloads. Client requests are treated as untrusted: the server
re-checks skill levels, prerequisites, costs and prestige limits before mutating the profile.

### `client`

Contains the journal key mapping, profile cache and first journal screen. No client class is
referenced by common/server code.

## Data flow

1. A server gameplay event calls `GuildsOfVerraApi.awardXp`.
2. `ProgressionService` returns a new immutable profile and a change summary.
3. The server stores the profile in the player attachment.
4. A compact profile payload synchronizes the client cache.
5. The journal renders only cached data; it never grants progression locally.

## Performance rules

- Event-driven XP instead of per-block or per-entity scanning.
- Exploration checks once per second and only on region changes.
- Immutable profile replacement only when data changes.
- Network sync only after a mutation.
- Elite conversion only when an entity is initially loaded.
- Vanilla entities, attributes, sounds and equipment wherever possible.

## Versioning

`PlayerProfile.SCHEMA_VERSION` controls saved-data migrations. Content identifiers are stable,
namespaced strings. Removing or renaming a node must include an explicit migration rather than
silently deleting player progression.
