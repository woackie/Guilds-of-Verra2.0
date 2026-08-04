# Guilds of Verra

Guilds of Verra is a lightweight, standalone and configurable Fabric mod that adds paced survival-RPG progression to Minecraft without replacing vanilla gameplay.

## Development snapshot

This repository currently contains the `0.1.0-dev.1` source snapshot targeting Minecraft Java 26.2, Fabric Loader and Fabric API.

Implemented foundations include:

- five skills from level 0 to 100;
- the approved XP curve and Adventurer Level calculation;
- permanent skill points and data-driven skill trees;
- player profile persistence and networking scaffolding;
- equipment and dimension restriction foundations;
- elite mob, discovery, title and journal foundations;
- administrator commands, translations, validation scripts and CI.

This is an early development build. It is not yet a verified playable release.

## Requirements

- JDK 25
- Minecraft Java 26.2
- Fabric Loader 0.19.3 or newer compatible version
- Fabric API 0.156.0+26.2

## Build

On Windows:

```powershell
.\gradlew.bat clean build
```

On Linux or macOS:

```bash
./gradlew clean build
```

Build output is written to `build/libs`.

## License

The source code is available under the MIT License. Original branding and artwork may be governed separately in future releases.

Guilds of Verra is not an official Minecraft product. It is not approved by or associated with Mojang or Microsoft.
