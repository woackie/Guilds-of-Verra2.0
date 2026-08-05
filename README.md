# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`0.1.0-dev.4` expands the working core progression systems with hardened equipment and Elytra locks, safe Nether/End progression gates, and configurable vanilla-themed elite encounters.

The update includes readable restriction feedback, portal-loop protection, safe pre-portal returns, seven scaled elite variants, spawn budgets, special abilities, first-encounter bestiary discoveries and expanded synchronized data for the interactive dev.5 journal.

The project also includes all five 100-point skill trees, persistent player profiles, Fishing and Cooking progression, purchased-node passives, administrative commands, prestige, discoveries and titles.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for exact implementation and runtime-verification status. Use [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) for the dev.4 playtest.

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

This validates JSON, skill-point totals, node prerequisites, XP monotonicity and default progression rules.
