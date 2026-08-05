# 0.1.0-dev.4 in-game testing checklist

## Startup and profile compatibility

1. Back up the test world, replace dev.3 with only `guilds-of-verra-0.1.0-dev.4.jar` and launch Minecraft 26.2.
2. Join an existing dev.3 world and confirm all levels, XP, prestige, nodes, discoveries and titles remain intact.
3. Confirm both `config/guildsofverra/progression.json` and `config/guildsofverra/elites.json` exist.
4. Run `/gv profile`, open the journal with `J`, leave and rejoin, and confirm synchronized values remain correct.

## Equipment and Elytra hard locks

5. Test locked iron, diamond and netherite tools while breaking blocks.
6. Test locked swords and axes against mobs.
7. Test bows, crossbows and shields through normal use and held-use actions.
8. Equip locked iron, diamond and netherite armour through inventory clicks and shift-clicking.
9. Inject locked armour with commands or a dispenser and confirm the recurring audit removes it safely.
10. Fill the inventory, inject locked armour and confirm the removed item drops instead of disappearing.
11. Equip a locked Elytra, attempt take-off and confirm it is removed and flight stops.
12. Purchase the required nodes and confirm every previously locked item works immediately.
13. Confirm repeated blocked actions show readable requirements without flooding chat.

## Dimension gates and safe returns

14. Enter a Nether portal below its requirements and confirm every missing requirement is listed.
15. Confirm the player returns roughly one second before the portal rather than inside it.
16. Repeat beside walls, pits, water and lava; confirm the return remains survivable.
17. Stand in an overlapping or rapid-reentry portal and confirm no transfer loop occurs.
18. Attempt Nether entry using commands or another transfer method and confirm the gate still applies.
19. Repeat the same checks for the End gate.
20. Meet all requirements and confirm legitimate Nether and End travel is unaffected.
21. Test two players using nearby portals with different progression and confirm return positions never cross.

## Elite spawning and budgets

22. Reach Adventurer Level 10 or temporarily lower `minimumAdventurerLevel` in `elites.json`.
23. Increase `baseSpawnChance` for testing, restart, and confirm supported hostile mobs can become elites.
24. Confirm names, scale, health, speed, armour, knockback resistance and damage differ by role.
25. Verify Tank Zombies, Bulwark Drowned, Armoured Skeletons and Marksmen receive their visible equipment.
26. Spawn many hostile mobs and confirm nearby and regional elite caps are respected.
27. Re-enter chunks containing elites and confirm they do not convert repeatedly or multiply their attributes.
28. Test at several Adventurer Levels and confirm stat scaling increases but stays below its configured cap.

## Elite abilities, rewards and collections

29. Let a Venom Spider hit a target and confirm poison duration follows configuration.
30. Let a Marksman Skeleton hit a target and confirm slowness.
31. Let a Bulwark Drowned hit a target and confirm weakness.
32. Take damage from a Volatile Creeper and confirm the target is ignited.
33. Disable `specialAbilitiesEnabled`, restart, and confirm those effects stop.
34. Defeat each elite and confirm Combat XP reflects its configured reward scale.
35. Defeat a variant for the first time and confirm a bestiary discovery and Exploration XP are awarded.
36. Defeat the same variant again and confirm discovery XP is not awarded twice.
37. Reconnect and confirm discoveries persist and remain synchronized.
38. Test two players defeating the same elite variant and confirm each profile unlocks independently.

## Regression and stability

39. Recheck Mining, Combat, Exploration, Fishing and Cooking XP from dev.3.
40. Recheck ore XP, extra fish and extra cooked-output passives.
41. Play a normal survival session and watch for repeated log errors, unexpected item loss or portal bouncing.
42. Test several players around many hostile mobs and inspect server tick time for elite-query regressions.
43. Restore ordinary spawn-chance and cap settings before keeping the world.
