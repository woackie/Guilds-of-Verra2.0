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

## Shared systems
- All 25 variants remain configurable through `config/guildsofverra/elites.json`.
- Existing nearby and regional elite budgets apply to every variant.
- Adventurer-level scaling and reward caps remain authoritative.
- Every variant has a persistent bestiary discovery and first-discovery reward.
- Variant-specific equipment and effects remain server authoritative.
- Voidstalker Endermen are explicitly restricted to the End.
- New variants must not modify the frozen dev.5 artifact or `main`.

## Automated validation requirements
- Registry contains exactly 25 unique elite IDs.
- Every registered variant resolves to a supported vanilla entity identifier.
- Shared-base roles remain deterministic.
- Voidstalker End filtering remains enforced.
- All ability configuration values normalize to non-negative values.
- Existing seven elite variants remain compatible.
- Java 25/Fabric compilation, automated tests and artifact packaging pass.

## Runtime verification

### Ten-minute roster smoke test
1. Temporarily increase `baseSpawnChance` and lower `minimumAdventurerLevel` in `elites.json`.
2. Confirm existing original elites still spawn and retain their previous behaviour.
3. Spawn or locate each supported base entity and confirm the expected elite name can appear.
4. Verify the bestiary total reports 25 entries.
5. Defeat one new variant and confirm discovery, Exploration XP and persistence.

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
- Tick-time performance around raids, monuments, strongholds and mob-heavy Nether areas.
