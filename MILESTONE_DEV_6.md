# Guilds of Verra 0.1.0-dev.6 — Expanded Encounters

This stacked milestone expands survival encounters without changing the frozen dev.5 test candidate.

## Complete 25-variant elite roster

### Original seven

- Tank Zombie
- Bulwark Drowned
- Armoured Skeleton
- Marksman Skeleton
- Brute Spider
- Venom Spider
- Volatile Creeper

### Expansion wave one

- Plague Husk
- Frostbound Stray
- Hexbinder Witch
- Raid Captain Pillager
- Ironhide Ravager
- Berserker Piglin
- Ashen Wither Skeleton
- Magma Colossus
- Voidstalker Enderman

### Expansion wave two

- Sporeguard Bogged
- Cave Stalker
- Tempest Breeze
- Cinder Blaze
- Soulreaver Ghast
- End Sentinel Shulker
- Swarmheart Silverfish
- Abyssal Guardian
- Dreadwing Phantom

All variants use the shared Adventurer-level scaling, nearby/regional budgets, Combat XP, first-discovery Exploration XP and hidden-safe bestiary systems. Voidstalker Endermen are explicitly restricted to the End.

## Player Hunt Event

A separate personal event periodically selects an eligible player and sends a hostile pack to track them down.

- Begins at Adventurer Level 15 by default.
- Checks every 30 seconds with a 1.5% chance per eligible check.
- Uses a 60-minute default per-player cooldown.
- Warns the player five seconds before spawning.
- Spawns five mobs initially and scales to ten with Adventurer Level.
- Uses a 40–64 block safe spawn ring.
- Reacquires and navigates toward its selected player every second.
- Uses separate Overworld, Nether and End hostile pools.
- Ends on victory, timeout, death, disconnect, dimension change, ineligibility or pursuit-distance breach.
- Cleans up loaded and later-reloaded orphaned hunters.
- Hunt members cannot become elites or consume elite budgets.

Configuration: `config/guildsofverra/hunt_events.json`.

## Seven survival world events

World events use one server-wide scheduler. A pending or active world event blocks every other world event, preventing combinations such as Blood Moon plus Long Night. Hunt Events remain a separate personal system.

### Shared pacing

- Minimum Adventurer Level 10 by default.
- Thirty-minute startup grace period.
- One scheduling check per minute.
- Three-percent chance per eligible check.
- Fifteen-second warning.
- At least 60 minutes after an event ends before another world event can begin.
- Weighted selection allows individual events to be disabled or made rarer.
- Event mobs use strict per-player caps and remain outside elite conversion/budgets.
- Event mobs are cleaned up when an event ends and when orphaned mobs later reload.

### Blood Moon

- Available while eligible players occupy the Overworld.
- Holds the Overworld clock around midnight.
- Nearby hostile monsters receive short refreshed Speed and Strength effects.
- Idle monsters acquire nearby eligible players more aggressively.
- Default duration: ten minutes.

### Severe Thunderstorm

- Available while eligible players occupy the Overworld.
- Forces rain and thunder through the server-owned weather system.
- Periodically rolls configurable lightning strikes in a radius around eligible players.
- Clears the forced storm when the event ends.
- Default duration: eight minutes.

### Cave Tremor

- Available only when an eligible Overworld player is underground at Y 48 or lower without sky access.
- Applies short Mining Fatigue pulses.
- Spawns bounded Silverfish and Cave Spider hazards around underground players.
- Does not destroy terrain, ores, tunnels or player builds.
- Default duration: six minutes.

### Nether Surge

- Available while eligible players occupy the Nether.
- Applies short Darkness pressure.
- Spawns bounded Blaze, Magma Cube, Piglin Brute and Wither Skeleton waves.
- Refreshes Speed and Fire Resistance on nearby Nether monsters.
- Default duration: seven minutes.

### Predator Migration

- Available while eligible players occupy the Overworld.
- Spawns bounded Spider, Cave Spider and Wolf groups farther from players.
- Sends those groups through occupied regions rather than placing them directly beside players.
- Default duration: five minutes.

### Long Night

- Available while eligible players occupy the Overworld.
- Holds the Overworld clock around midnight using Minecraft 26.2 world clocks.
- Sleeping cannot permanently skip the event because the event restores midnight while active.
- Moves the clock toward dawn when the event ends.
- Default duration: twelve minutes.

### Restless Dead

- Available whenever at least one eligible player is online.
- Supported slain undead have a configurable 25% chance to return after five seconds.
- Zombies, Zombie Villagers, Husks, Drowned, Skeletons, Strays, Wither Skeletons and Zombified Piglins are supported.
- Revived undead are tagged and cannot revive a second time.
- Pending revivals are capped and cancelled at event end.
- Default duration: seven minutes.

Configuration: `config/guildsofverra/world_events.json`.

## Automated validation

- Exactly 25 unique elite IDs.
- Supported vanilla base-entity coverage and End-only filtering.
- Elite spawn chance, scaling, budgets and reward calculations.
- Hunt eligibility, probability, cooldown and pack scaling.
- World-event player eligibility, trigger probability, cooldowns, weighted selection and spawn caps.
- World-event configuration defaults and unsafe-value normalization.
- Java 25/Fabric client and server compilation.
- Automated tests and artifact packaging.

## Runtime verification still required

### Elite encounters

- Natural rarity and biome/dimension identity.
- Visual scale, names and equipment.
- Melee and projectile-triggered abilities.
- Bestiary total, discoveries, rewards and multiplayer isolation.
- Nearby/regional caps and performance.

### Hunt Event

- Warning and announced member count.
- Safe 40–64 block ring spawning.
- Pursuit pressure and path refresh.
- Victory, timeout, death, disconnect and dimension cleanup.
- Chunk-unload/reload cleanup.
- Multiplayer selection and global concurrency.

### World events

- Minecraft 26.2 clock behavior during Blood Moon and Long Night.
- Weather start/end and lightning safety during Severe Thunderstorm.
- Underground eligibility and bounded hazards during Cave Tremor.
- Nether-only waves and buffs during Nether Surge.
- Predator navigation and event-mob caps.
- One-time undead revival and pending-revival cleanup.
- One-world-event exclusivity, frequency and ordinary survival performance.

Use `WORLD_EVENT_TESTING.md` for the rapid test configuration and detailed checks.
