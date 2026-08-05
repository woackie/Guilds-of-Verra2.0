# Guilds of Verra 0.1.0-dev.6 — Expanded Encounters

This stacked milestone expands elite encounters without changing the frozen dev.5 test candidate.

## Planned second-wave variants

### Overworld
- **Plague Husk** — desert disease elite that inflicts Hunger and brief Poison.
- **Frostbound Stray** — snow-biome control elite that applies stronger Slowness and freezing pressure.
- **Hexbinder Witch** — tougher support caster with resistance to ordinary burst damage and improved reward value.
- **Raid Captain Pillager** — armoured crossbow commander with a short Weakness effect on hit.
- **Ironhide Ravager** — rare raid tank with heavy knockback resistance and high reward value.

### Nether
- **Berserker Piglin** — fast melee glass cannon with high damage and limited armour.
- **Ashen Wither Skeleton** — durable fire-themed elite with a longer Wither effect.
- **Magma Colossus** — oversized magma cube with strong knockback resistance and fire pressure.

### End
- **Voidstalker Enderman** — fast, durable End elite that briefly blinds struck targets.

## Shared systems
- All variants remain configurable through `config/guildsofverra/elites.json`.
- Existing nearby and regional elite budgets apply to every new variant.
- Every new variant has a persistent bestiary discovery and first-discovery reward.
- Variant-specific equipment and effects remain server authoritative.
- New variants must not modify the frozen dev.5 artifact or `main`.

## Validation requirements
- Deterministic variant registry tests.
- Every registered variant resolves to a supported vanilla entity type.
- Ability configuration can disable each new effect.
- Existing seven elite variants remain compatible.
- Java 25/Fabric compilation, automated tests and artifact packaging pass.

## Runtime verification
- Natural spawning and rarity in the correct dimensions/biomes.
- Visual scale, names, equipment and role readability.
- Ability duration and balance.
- Bestiary discovery persistence and multiplayer isolation.
- Spawn-budget performance with the larger registry.
