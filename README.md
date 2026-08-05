# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`0.1.0-dev.5` is the interactive-journal test candidate stacked on the verified dev.4 gate and elite foundation.

It adds spatial five-skill tree maps, prerequisite paths, pan and zoom controls, server-authoritative node purchasing and prestige, paged titles/discoveries/bestiary collections, active passive summaries, live dimension requirements, and a sixteen-entry equipment/Elytra unlock catalogue.

The underlying mod includes all five 100-point skill trees, persistent player profiles, Mining/Combat/Exploration/Fishing/Cooking progression, purchased-node passives, hard equipment locks, safe dimension gates, configurable elite encounters, discoveries, titles and administrative commands.

`main` remains the latest promoted playable build. Dev.5 remains a stacked development candidate until dev.3/dev.4 runtime feedback is carried forward and the complete checklist passes.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for exact implementation status. Use [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) for the prioritized dev.5 playtest and [RUNTIME_TEST_REPORT.md](RUNTIME_TEST_REPORT.md) to record problems consistently.

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
```

`progression.json` controls skill XP rewards and progression feedback. `elites.json` controls elite rarity, nearby and regional caps, stat scaling, reward scaling, first-discovery XP, visible names and special-ability durations. Restart Minecraft after editing either file.

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

This validates JSON, skill-point totals, node prerequisites, XP monotonicity and default progression rules. The Gradle test suite additionally checks progression services, elite rules, safe returns, spatial tree layout, viewport behavior, summaries and language-key parity.
