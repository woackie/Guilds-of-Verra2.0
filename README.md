# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`0.1.0-dev.5` remains the frozen interactive-journal test candidate stacked on the verified dev.4 gate and elite foundation.

The stacked `0.1.0-dev.6` development branch expands encounters to twenty-five vanilla-themed elite variants, adds configurable player-targeted Hunt Events, and adds seven mutually-exclusive survival world events: Blood Moon, Severe Thunderstorm, Cave Tremor, Nether Surge, Predator Migration, Long Night and Restless Dead.

Hunt Events warn an eligible player, spawn a scaled hostile pack farther away, repeatedly retarget the selected player, and clean up safely after victory, timeout, death, disconnect, dimension changes or server shutdown. World events use a separate server-wide scheduler, never overlap one another, and create combat, weather, underground, dimension and time-of-day pressure without modifying the frozen dev.5 artifact.

The underlying mod includes all five 100-point skill trees, persistent player profiles, Mining/Combat/Exploration/Fishing/Cooking progression, purchased-node passives, hard equipment locks, safe dimension gates, configurable elite encounters, discoveries, titles, an interactive journal and administrative commands.

`main` remains the latest promoted playable build. Dev.5 remains the prepared runtime-test candidate; dev.6 remains stacked on dev.5 and must not replace the frozen dev.5 artifact during its testing session.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for exact implementation status. Use [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) for the prioritized dev.5 playtest, [RUNTIME_TEST_REPORT.md](RUNTIME_TEST_REPORT.md) to record problems consistently, [MILESTONE_DEV_6.md](MILESTONE_DEV_6.md) for the dev.6 scope, and [WORLD_EVENT_TESTING.md](WORLD_EVENT_TESTING.md) for the rapid seven-event test setup.

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
