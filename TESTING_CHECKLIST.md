# 0.1.0-dev.5 in-game testing checklist

Use a copied test world. Keep only one Guilds of Verra JAR in the `mods` folder.

## Phase 1 — Ten-minute smoke test

1. Replace the old test JAR with only `guilds-of-verra-0.1.0-dev.5.jar` and launch Minecraft 26.2.
2. Confirm the log reports `Guilds of Verra 0.1.0-dev.5 initialized.`
3. Join an existing dev.3 world and confirm levels, XP, prestige, nodes, discoveries and titles remain intact.
4. Press `J`; confirm the journal opens without a crash.
5. Open Overview, every skill, Collections, Gates and Unlocks.
6. Confirm all five skill maps appear and the Previous/Next, pan and zoom controls respond.
7. Purchase one valid cheap node and confirm points and node state update immediately.
8. Attempt one locked node and confirm the denial appears without changing the profile.
9. Leave and rejoin; confirm the purchased node remains.
10. Mine one block, damage one hostile mob, catch one fish or collect one cooked food and confirm progression still updates.

Stop here and report immediately if startup, profile loading, journal opening or saving fails.

## Phase 2 — Journal layout and navigation

11. Test the journal with small, normal and large GUI scale settings.
12. Test at a standard 16:9 resolution.
13. Test an ultrawide or resized window if available.
14. Confirm no tab, footer, page button or row action is outside the screen.
15. Confirm every skill map shows node boxes and prerequisite paths.
16. Pan left, right, up and down and confirm movement stops at sensible bounds.
17. Zoom repeatedly from the minimum to maximum and confirm nodes remain stable.
18. Switch between skills after panning/zooming and confirm each new tree starts in a usable position.
19. Confirm the paged node list remains readable beside the map.
20. Confirm purchased, purchasable, level-locked, prerequisite-locked and point-locked states are distinguishable without relying only on colour.

## Phase 3 — Node purchasing and action feedback

21. Purchase a node with sufficient level, points and prerequisites.
22. Confirm the button enters a pending state and then becomes owned.
23. Confirm available and spent points update immediately.
24. Attempt to buy the same node again and confirm it is rejected.
25. Attempt a node below the required level.
26. Attempt a node with a missing prerequisite.
27. Attempt a node without enough points.
28. Click purchase repeatedly and confirm duplicate requests are rate-limited.
29. Confirm success, denial and rate-limit messages appear clearly in chat; journal-inline feedback is prepared through the new result payload and should be checked once fully wired.
30. Disconnect during a pending action, reconnect and confirm the authoritative server profile is correct.
31. Open two clients and purchase different nodes simultaneously; confirm profiles never cross.

## Phase 4 — Prestige

32. Use `/gv` administration on a copied profile to reach level 100 in one skill if needed.
33. Confirm the prestige button is disabled below level 100.
34. At level 100, click Prestige once and confirm the consequence warning appears.
35. Let the confirmation window expire and confirm no prestige occurs.
36. Confirm prestige on the second click within the window.
37. Verify level and current XP reset as designed.
38. Verify purchased nodes, permanent earned points and highest-ever level remain.
39. Confirm the prestige rank updates immediately and persists after reconnecting.
40. Verify maximum prestige cannot be exceeded.

## Phase 5 — Collections and titles

41. Cycle through Titles, Discoveries and Bestiary.
42. Confirm each section uses its own correct page count.
43. Select an unlocked title and confirm it becomes active.
44. Select the clear-title entry and confirm the active title becomes empty.
45. Attempt rapid title changes and confirm requests are rate-limited.
46. Confirm unlocked discoveries are listed and internal IDs remain understandable.
47. Confirm undiscovered elite details are not exposed.
48. Defeat a new elite and confirm its bestiary entry appears after synchronization.
49. Reconnect and confirm title and bestiary state persist.
50. Test two players with different discoveries and confirm each sees only their own collection.

## Phase 6 — Passives, Unlocks and travel gates

51. Compare active-passive summaries before and after purchasing a passive node.
52. Confirm passive totals change immediately after purchase.
53. Open Unlocks and confirm all sixteen entries can be paged through.
54. Confirm iron, diamond and netherite tools show the correct Mining nodes.
55. Confirm swords, axes, bows, crossbows and shields show the correct Combat nodes.
56. Confirm iron, diamond and netherite armour show the correct Combat nodes.
57. Confirm Elytra shows `exploration:elytra_certification`.
58. Confirm each card shows required skill level, current level, node, effect and locked/unlocked state.
59. Purchase a required node and confirm its Unlocks card updates immediately.
60. Open Gates and compare Nether/End requirements with `/gv profile` and actual portal behavior.

## Phase 7 — Dev.4 regression: equipment and Elytra

61. Test locked iron, diamond and netherite tools while breaking blocks.
62. Test locked swords and axes against mobs.
63. Test bows, crossbows and shields through normal use.
64. Equip locked armour normally, through shift-clicking and through commands.
65. Fill the inventory and confirm removed locked armour drops instead of disappearing.
66. Equip a locked Elytra and attempt take-off; confirm equipment removal and flight cancellation.
67. Purchase the relevant node and confirm the item becomes usable immediately.
68. Confirm repeated blocked actions do not flood chat.

## Phase 8 — Dev.4 regression: dimensions and elites

69. Enter a Nether portal below requirements and confirm all missing requirements are listed.
70. Confirm the return is before the portal and does not loop.
71. Repeat near walls, drops, water and lava.
72. Repeat the same checks for the End.
73. Test two players using nearby portals with different progression.
74. Increase elite spawn chance temporarily and confirm all seven elite roles can appear.
75. Confirm names, equipment, scale, stats and nearby/regional caps.
76. Confirm Venom poison, Marksman slowness, Bulwark weakness and Volatile fire.
77. Confirm Combat XP scaling and one-time Exploration discovery XP.
78. Defeat the same elite twice and confirm discovery XP is not repeated.
79. Restore ordinary elite configuration values before keeping the world.

## Phase 9 — Progression and stability regression

80. Recheck Mining, Combat, Exploration, Fishing and Cooking XP.
81. Recheck ore-XP, extra-fish and extra-cooked-output passives.
82. Edit several progression and elite configuration values, restart and confirm they load.
83. Test several players around hostile mobs while journals are open.
84. Watch the latest log for repeated exceptions, packet errors or rendering errors.
85. Play a normal survival session and note unexpected item loss, portal bouncing, duplicate XP or tick-time spikes.
86. Reconnect all players and confirm every profile remains isolated and persistent.

## Report format

For every problem, record:

- checklist step number;
- what you expected;
- what happened;
- whether it happens every time;
- single-player or multiplayer;
- relevant screenshot;
- latest log or crash report when available.
