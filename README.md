# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`main` is promoted and runtime-approved through `0.1.0-dev.6`: core progression, hard gates, the interactive journal, twenty-five elites, Hunt Events and seven world events.

The `0.1.0-dev.7` candidate makes purchased skill nodes affect real gameplay. Twenty-nine of the thirty declared passive types now have server-authoritative handlers for attributes, damage, durability, food, effects, fishing, loot and discovery rewards. `map_radius` remains deliberately inactive until the exploration/discovery map system exists, rather than pretending to modify vanilla maps incorrectly.

The underlying mod includes all five 100-point skill trees, persistent player profiles, Mining/Combat/Exploration/Fishing/Cooking progression, hard equipment and dimension locks, configurable elite encounters, discoveries, titles, an interactive journal and administrative testing commands.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for exact implementation status, [MILESTONE_DEV_7.md](MILESTONE_DEV_7.md) for the passive-runtime scope, and [PASSIVE_TESTING.md](PASSIVE_TESTING.md) for the focused dev.7 playtest. Earlier encounter checks remain in [MILESTONE_DEV_6.md](MILESTONE_DEV_6.md) and [WORLD_EVENT_TESTING.md](WORLD_EVENT_TESTING.md).

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer compatible release
- Fabric API 0.156.0+26.2
- Java Development Kit 25

## Runtime configuration

The mod generates:

```text
config/guildsofverra/progression.json
config/guildsofverra/elites.json
config/guildsofverra/hunt_events.json
config/guildsofverra/world_events.json
```

- `progression.json` controls skill XP rewards and progression feedback.
- `elites.json` controls elite rarity, nearby/regional caps, stat scaling, rewards, discoveries, visible names and ability durations.
- `hunt_events.json` controls personal hunt chance, eligibility, warning delay, the 60-minute default cooldown, pack scaling, spawn distance, pursuit behaviour, duration, dimensions and concurrency.
- `world_events.json` controls server-wide event frequency, the 60-minute minimum separation, event weights, durations, mob caps, weather pressure, revival chance and dimension-specific behavior.

Restart Minecraft after editing a configuration file.

## Operator testing commands

The dev.6 encounter systems can be exercised without changing spawn chances or waiting for
the natural schedulers. These commands require operator permission level 2:

```text
/gv event start <event>
/gv event stop
/gv event status
/gv hunt start <player>
/gv hunt stop <player>
/gv hunt status <player>
/gv elite spawn <variant> [count]
/gv test master-skill <player> <skill>
```

Event and elite IDs autocomplete in chat. Manual world events start immediately and still
use the normal effects, duration and cleanup. A manual Hunt Event keeps its configured warning
delay. Manually spawned elites use the executing player's Adventurer Level scaling while
bypassing natural rarity and elite-population caps.

`/gv test master-skill` raises one skill to level 100 and purchases its complete tree. It is intended for deterministic development-build testing; `/gv reset <player>` restores a blank profile.

## Journal controls

- Press `J` to open the journal.
- Use the top tabs to switch between progression, collections, gates and unlock information.
- Use Previous/Next for paged lists.
- Use the arrow buttons to pan a skill tree.
- Use `−` and `+` to zoom from 50% to 175%.
- Journal purchases, prestige and title changes are always validated by the server.

## Build

Windows:

```bat
gradlew.bat build
```

Linux/macOS:

```sh
./gradlew build
```

The remapped mod JAR is written to `build/libs/`.

## Development run

```sh
./gradlew runClient
./gradlew runServer
```

## Core validation without Minecraft dependencies

```sh
python scripts/validate_project.py
```

This validates JSON, skill-point totals, node prerequisites, XP monotonicity and default progression rules. The Gradle test suite additionally checks progression services, elite rules, Hunt Event eligibility/scaling, world-event eligibility/weights/cooldowns/configuration, safe returns, spatial tree layout, viewport behavior, summaries and language-key parity.
