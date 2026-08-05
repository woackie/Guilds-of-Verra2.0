# Dev.6 World Event Testing

The seven world events are mutually exclusive. Hunt Events remain a separate personal system.

## Default pacing

- The first world event cannot begin during the first 30 minutes after server startup.
- The scheduler checks once per minute.
- Each eligible check has a 3% trigger chance.
- After an event ends, another world event cannot begin for at least 60 minutes.
- Only one world event can be warning or active at a time.
- Hunt Events have their own 60-minute per-player cooldown.

This produces roughly one world event every 90–105 minutes during long eligible sessions, depending on event duration and random rolls.

## Rapid test configuration

Back up the world, start Minecraft once to generate the config, then close the game and edit:

`config/guildsofverra/world_events.json`

Use these temporary scheduler values:

```json
{
  "minimumAdventurerLevel": 0,
  "initialDelayMinutes": 1,
  "checkIntervalSeconds": 10,
  "triggerChancePerCheck": 1.0,
  "minimumMinutesBetweenEvents": 1,
  "warningSeconds": 5
}
```

Keep the rest of the generated file. To test one event, set its `Enabled` field to `true` and weight above zero, then set every other event weight to `0`. Restart after each config change.

## Required conditions

| Event | Required test position |
|---|---|
| Blood Moon | Eligible player in the Overworld |
| Severe Thunderstorm | Eligible player in the Overworld |
| Cave Tremor | Overworld, Y 48 or lower, with no direct view of the sky |
| Nether Surge | Eligible player in the Nether |
| Predator Migration | Eligible player in the Overworld |
| Long Night | Eligible player in the Overworld |
| Restless Dead | Any eligible player; kill supported undead during the event |

## Event checks

### Blood Moon

1. Confirm the warning appears before the event starts.
2. Confirm the Overworld clock remains around midnight.
3. Approach ordinary hostile mobs and confirm Speed and Strength pressure.
4. Confirm idle monsters acquire nearby players more aggressively.
5. Confirm the clock moves toward dawn when the event ends.

### Severe Thunderstorm

1. Confirm rain and thunder begin globally.
2. Confirm lightning strikes near, but not directly on, eligible players at every pulse opportunity.
3. Confirm ineligible Creative/Spectator players do not attract lightning by default.
4. Confirm normal weather resumes when the event ends.

### Cave Tremor

1. Confirm nothing happens above the configured Y threshold or beneath open sky.
2. Confirm underground players receive short Mining Fatigue pulses.
3. Confirm Silverfish and Cave Spiders appear within the configured event-mob cap.
4. Confirm spawned hazards disappear when the event ends.

### Nether Surge

1. Confirm only players in the Nether receive the Darkness pressure and spawn waves.
2. Confirm Blaze, Magma Cube, Piglin Brute and Wither Skeleton waves can appear.
3. Confirm nearby Nether monsters receive temporary Speed and Fire Resistance.
4. Confirm event-spawned mobs do not become elites.

### Predator Migration

1. Confirm Spider, Cave Spider and Wolf groups spawn away from players.
2. Confirm packs navigate across the occupied region instead of appearing directly beside the player.
3. Confirm the per-player event-mob cap is respected.
4. Confirm unloaded migration mobs are removed if they load after the event ends.

### Long Night

1. Confirm the Overworld remains near midnight for the configured duration.
2. Attempt sleeping and confirm dawn does not permanently skip the event.
3. Confirm daylight resumes when the event ends.

### Restless Dead

1. Kill Zombies, Zombie Villagers, Husks, Drowned, Skeletons, Strays, Wither Skeletons and Zombified Piglins.
2. Confirm some return after the configured delay.
3. Kill a revived undead and confirm it cannot revive a second time.
4. Confirm pending revivals are cancelled when the event ends.
5. Confirm the pending-revival cap is respected during mass combat.

## Shared safety checks

- No two world events overlap.
- Event mobs do not consume elite nearby/regional budgets.
- World event mobs are removed at event end, including mobs that unload and later reload.
- Disabling `world_events.json` cleans up pending and active event state.
- Disconnecting or changing dimension does not leave permanent event mobs behind.
- Two players in different dimensions receive only effects relevant to their dimension.
- Ordinary play remains smooth near mob-heavy bases, Nether fortresses and caves.

Restore the original configuration after testing or delete `world_events.json` and restart to regenerate defaults.
