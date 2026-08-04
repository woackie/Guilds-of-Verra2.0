# Build verification — 0.1.0-dev.1

Completed for this source snapshot:

- All packaged JSON parsed successfully.
- Five skill trees found.
- 124 skill nodes validated.
- Every skill tree costs exactly 100 points.
- All node prerequisites resolve.
- XP requirements increase monotonically through level 100.
- Total XP to level 100 equals 561,950.
- Dependency-free progression core compiled and passed its executable self-test.
- Current Minecraft 26.2 Fabric API rename audit applied to networking, key mappings,
  permissions, entity events and teleportation.

Not completed in the generation environment:

- Full Loom/Fabric remap build, because the available local runtime was JDK 21 and external
  Gradle/Minecraft dependency resolution was unavailable. Minecraft 26.2 targets JDK 25.
- Client launch, dedicated-server launch and in-game GameTests.

The included GitHub Actions workflow and Gradle wrapper are configured for JDK 25 so a
networked development machine or GitHub repository can perform the full build.
