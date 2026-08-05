# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`0.1.0-dev.3` expands the working dev.2 foundation with complete Fishing and Cooking progression loops, configurable XP rewards, the first functional purchased-node passives, clearer progression feedback and an improved journal with XP bars and point totals.

The project also includes all five 100-point skill trees, persistent player profiles, server-authoritative networking, administrative commands, hard equipment locks, dimension requirements, prestige, discoveries, titles and vanilla-themed elite foundations.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for exact implementation and runtime-verification status. Use [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) for the dev.3 playtest.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer compatible release
- Fabric API 0.156.0+26.2
- Java Development Kit 25

## Runtime configuration

The mod generates:

```text
config/guildsofverra/progression.json
```

This controls Mining, Fishing, Cooking, Combat and Exploration rewards plus progression-message settings. Restart Minecraft after editing the file.

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
