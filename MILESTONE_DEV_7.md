# Guilds of Verra 0.1.0-dev.7 — Functional Skill Passives

Dev.7 turns purchased skill-tree promises into server-authoritative gameplay mechanics. It is based directly on the runtime-approved dev.6 merge.

## Runtime coverage

### Exploration

- `movement_speed`: transient on-foot movement modifier.
- `travel_exhaustion`: reduces server-side exhaustion requests.
- `fall_damage`: reduces fall damage at the damage entrypoint.
- `swim_speed`: uses vanilla water-movement efficiency.
- `air_supply`: increases maximum server-side air supply.
- `discovery_xp`: multiplies first elite-discovery Exploration XP.
- `map_radius`: deliberately deferred until Guilds of Verra has an actual exploration/discovery map.

### Fishing

- `fishing_wait`: probabilistically advances the live fishing-hook countdown to match the intended average reduction.
- `fishing_durability`: preserves rod durability per requested damage point.
- `extra_fish`: retains the existing bonus-common-fish implementation.
- `treasure_weight`: grants the declared relative increase as bonus treasure output.
- `junk_weight`: suppresses the declared fraction of junk output.
- `aquatic_drops`: grants expected-value bonus drops from Drowned and Guardian-family kills.

### Cooking

- `meal_saturation`: increases saturation actually gained from tagged prepared food.
- `ingredient_preservation`: returns one representative primary ingredient after a successful prepared-food output.
- `extra_cooked_output`: retains the existing personal bonus-output implementation.
- `positive_meal_duration`: extends beneficial effects applied synchronously by tagged prepared food.
- `negative_food_duration`: shortens harmful effects applied synchronously by tagged prepared food.
- `eating_duration`: shortens the server-authoritative use duration of tagged prepared food.

### Mining

- `mining_speed`: transient block-break-speed modifier while holding a vanilla mining tool.
- `tool_durability`: preserves mining-tool durability per requested damage point.
- `ore_xp`: retains the existing ore-XP multiplier.
- `common_resource_bonus`: grants bonus tagged common resources through block-loot post-processing.
- `underground_damage`: reduces fall and explosion damage below Y 60 without sky access.

### Combat

- `max_health`: transient additive maximum-health modifier.
- `incoming_damage`: multiplies incoming server damage without changing armour values.
- `sword_damage`: increases sword damage and adds the declared sweeping ratio.
- `axe_damage`: increases axe damage. Exact armour-penetration modelling remains balance work.
- `projectile_damage`: increases player-fired arrow damage and accelerates bow/crossbow use progress.
- `shield_durability`: preserves shield durability and adds the declared blocking knockback resistance.

## Safety and compatibility

- Purchased bonuses are always resolved from the server-owned persistent profile.
- Attribute bonuses use namespaced transient modifiers and never rewrite vanilla base values.
- Durability preservation modifies only the requested damage amount and leaves vanilla Unbreaking/break callbacks intact.
- Food-effect changes are limited to the synchronous prepared-food consumption window.
- Loot changes operate only when a responsible player can be resolved from the loot context.
- All probabilities and multipliers are clamped.

## Known boundaries

- `map_radius` is the only declared passive type without a runtime handler.
- Travel exhaustion currently applies to all vanilla exhaustion calls because vanilla does not label their source at that hook.
- Treasure/junk passives post-process fishing output rather than rewriting nested vanilla loot-table weights.
- Ingredient preservation returns a representative primary ingredient; full recipe-input provenance is a later crafting-system refinement.
- Axe armour penetration still needs an exact post-armour implementation.
- Placed-block Mining provenance and persistent exploration-cell history remain separate anti-exploit milestones.

## Test helper

Operators can prepare a full tree instantly:

```text
/gv test master-skill <player> <exploration|fishing|cooking|mining|combat>
```

Use `/gv reset <player>` between baseline and mastered comparisons.
