# Guilds of Verra 0.1.0-dev.6 — Expanded Encounters

This stacked milestone expands elite encounters without changing the frozen dev.5 test candidate.

## Complete 25-variant roster

### Original seven
- Tank Zombie
- Bulwark Drowned
- Armoured Skeleton
- Marksman Skeleton
- Brute Spider
- Venom Spider
- Volatile Creeper

### Expansion wave one

#### Overworld and raids
- **Plague Husk** — desert disease elite that inflicts Hunger and Poison.
- **Frostbound Stray** — snow-biome control elite that applies Slowness and freezing pressure.
- **Hexbinder Witch** — tougher support caster that inflicts Weakness and Darkness.
- **Raid Captain Pillager** — armoured crossbow commander with Weakness on hit.
- **Ironhide Ravager** — rare raid tank with heavy knockback resistance and impact Slowness.

#### Nether
- **Berserker Piglin** — fast melee glass cannon with high damage and limited armour.
- **Ashen Wither Skeleton** — durable fortress elite with a longer Wither effect.
- **Magma Colossus** — oversized magma cube with strong knockback resistance and fire pressure.

#### End
- **Voidstalker Enderman** — fast, durable End-only elite that blinds struck targets.

### Expansion wave two

#### Swamps, caves and structures
- **Sporeguard Bogged** — swamp archer that combines Poison and Slowness.
- **Cave Stalker** — mineshaft ambusher that combines Poison and Blindness.
- **Tempest Breeze** — trial-chamber controller that briefly levitates struck targets.
- **Swarmheart Silverfish** — stronghold disruption elite that applies Weakness and Mining Fatigue.

#### Ocean and nighttime encounters
- **Abyssal Guardian** — monument elite that applies Darkness and Slowness.
- **Dreadwing Phantom** — nighttime aerial elite that applies Blindness and Weakness.

#### Nether and End structures
- **Cinder Blaze** — fortress ranged elite that ignites and weakens targets.
- **Soulreaver Ghast** — Nether-sky artillery elite that applies Wither.
- **End Sentinel Shulker** — End-city defender that combines ordinary levitation pressure with Weakness.

## Player Hunt Event

A separate configurable encounter periodically selects an eligible player and sends a hostile pack to track them down.

### Runtime flow
- A warning announces that a hostile pack has found the player's trail.
- After a configurable delay, the pack spawns in a 40–64 block ring around the player.
- Pack size begins at five and scales with Adventurer Level up to ten by default.
- Every second, surviving members reacquire the selected player and refresh their path toward them.
- The event ends when the entire pack is defeated, the timer expires, the target dies, disconnects, changes dimension, becomes ineligible, or the hunters exceed the pursuit distance.
- Remaining event mobs are discarded during cancellation, timeout and server shutdown.

### Safety and multiplayer limits
- One active or pending hunt per player.
- Configurable global concurrent-hunt cap.
- Per-player cooldown prevents repeated attacks.
- Creative and spectator players are excluded by default.
- Spawn searches require loaded chunks, solid ground and two blocks of free space.
- Hunt members are excluded from normal elite conversion and elite spawn budgets.
- Overworld, Nether and End use separate mobile hostile pools.

### Configuration
The event generates `config/guildsofverra/hunt_events.json`, including:
- trigger chance and check interval;
- minimum Adventurer Level;
- warning delay and player cooldown;
- minimum/maximum pack size and level scaling;
- spawn distance and attempts;
- duration, pursuit distance, retarget interval and pathing speed;
- global active-event cap;
- dimension toggles and creative-player eligibility.

## Shared systems
- All 25 variants remain configurable through `config/guildsofverra/elites.json`.
- Existing nearby and regional elite budgets apply to every variant.
- Adventurer-level scaling and reward caps remain authoritative.
- Every variant has a persistent bestiary discovery and first-discovery reward.
- Variant-specific equipment and effects remain server authoritative.
- Voidstalker Endermen are explicitly restricted to the End.
- New variants and Hunt Events must not modify the frozen dev.5 artifact or `main`.

## Automated validation requirements
- Registry contains exactly 25 unique elite IDs.
- Every registered variant resolves to a supported vanilla entity identifier.
- Shared-base roles remain deterministic.
- Voidstalker End filtering remains enforced.
- All ability configuration values normalize to non-negative values.
- Existing seven elite variants remain compatible.
- Hunt eligibility, cooldown, trigger chance and pack scaling tests pass.
- Java 25/Fabric compilation, automated tests and artifact packaging pass.

## Runtime verification

### Ten-minute roster smoke test
1. Temporarily increase `baseSpawnChance` and lower `minimumAdventurerLevel` in `elites.json`.
2. Confirm existing original elites still spawn and retain their previous behaviour.
3. Spawn or locate each supported base entity and confirm the expected elite name can appear.
4. Verify the bestiary total reports 25 entries.
5. Defeat one new variant and confirm discovery, Exploration XP and persistence.

### Hunt Event checks
1. Temporarily set `triggerChancePerCheck` to `1.0`, `checkIntervalSeconds` to `5`, `warningSeconds` to `3` and `playerCooldownMinutes` to `1`.
2. Confirm the warning occurs before any pack member appears.
3. Confirm mobs spawn 40–64 blocks away on safe loaded terrain.
4. Confirm the announced member count matches the successfully spawned pack.
5. Run away and verify the pack repeatedly reacquires and pursues the selected player.
6. Kill the whole pack and verify the victory message fires once.
7. Test timeout, death, disconnect and dimension-change cleanup.
8. Test two eligible players with the global cap set to one and then two.
9. Confirm Hunt Event mobs do not become elites or consume elite caps.
10. Confirm disabling `hunt_events.json` removes active event mobs and stops scheduling.

### Expansion wave two checks
- Bogged: Poison and Slowness.
- Cave Spider: Poison and Blindness.
- Breeze: short Levitation without excessive fall-lock chains.
- Blaze: fire duration and Weakness.
- Ghast: Wither applies when its projectile damages a target.
- Shulker: Weakness applies from projectile damage.
- Silverfish: Weakness and Mining Fatigue.
- Guardian: Darkness and Slowness from beam damage.
- Phantom: Blindness and Weakness during dive attacks.

### Balance and stability
- Natural spawning and rarity in the correct dimensions, structures and conditions.
- Visual scale, names, equipment and role readability.
- Ability duration and counterplay.
- Bestiary discovery persistence and multiplayer isolation.
- Nearby/regional spawn-budget behaviour with the 25-entry registry.
- Hunt frequency, pack size, pursuit pressure and cleanup behaviour.
- Tick-time performance around active hunts, raids, monuments, strongholds and mob-heavy Nether areas.
