# Guilds of Verra

A lightweight, standalone, configurable Fabric progression mod for Minecraft Java 26.2.

## Current snapshot

`0.1.0-dev.1` is the first full repository foundation. It includes the complete data model,
all five 100-point skill trees, XP curves, player persistence through Fabric Data Attachments,
server-authoritative networking, administrative commands, hard-lock rule definitions,
dimension requirements, prestige definitions, discovery/title data, elite-mob definitions,
a basic journal client, validation scripts and automated core tests.

See [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) for the exact implementation status.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer compatible release
- Fabric API 0.156.0+26.2
- Java Development Kit 25

## Build

Windows:

```bat
gradlew.bat build
```

Linux/macOS:

```sh
./gradlew build
```

The remapped mod JAR will be written to `build/libs/`.

## Development run

```sh
./gradlew runClient
./gradlew runServer
```

## Core validation without Minecraft dependencies

The repository contains a dependency-free progression core. Run:

```sh
python scripts/validate_project.py
```

This validates JSON, skill-point totals, node prerequisites, XP monotonicity and default progression rules.
