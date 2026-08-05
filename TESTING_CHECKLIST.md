# 0.1.0-dev.3 in-game testing checklist

## Startup and existing profiles

1. Replace the old JAR with only `guilds-of-verra-0.1.0-dev.3.jar` and launch Minecraft 26.2.
2. Join an existing dev.2 world and confirm levels, purchased nodes and prestige remain intact.
3. Confirm `config/guildsofverra/progression.json` is generated after startup.
4. Open the journal with `J`; verify all five XP bars, points, prestige and totals render correctly.

## Fishing

5. Catch a normal fish and confirm immediate Fishing XP feedback.
6. Catch treasure and junk; confirm their XP rewards differ from normal fish.
7. Cast and reel in without a catch; confirm no Fishing XP is awarded.
8. Leave and rejoin; confirm Fishing progress is preserved and old vanilla catches are not awarded again.
9. Purchase an `extra_fish` node, make repeated catches and verify occasional bonus fish without duplicate XP.

## Cooking

10. Cook food in a furnace and smoker, collect it and confirm Cooking XP.
11. Collect campfire-cooked food and verify whether vanilla records it for the configured Cooking system.
12. Craft bread, cookies, soup, pie and cake; confirm configured prepared-food XP.
13. Move already-cooked food between inventories; confirm no extra Cooking XP.
14. Purchase an `extra_cooked_output` node and verify occasional bonus output.

## Existing systems and passives

15. Mine stone and ores; verify Mining XP still works and ore rewards respect configuration.
16. Purchase an `ore_xp` node and confirm ore XP increases while ordinary stone XP does not.
17. Damage and kill hostile mobs; verify Combat XP and level-up messages.
18. Cross configured exploration-cell boundaries and verify Exploration XP.
19. Verify equipment locks, Nether/End requirements and Elytra requirements remain enforced.
20. Reconnect and verify every new skill value and purchased node is preserved.

## Multiplayer and stability

21. Test two players fishing/cooking simultaneously and confirm progress never leaks between profiles.
22. Change several XP values in `progression.json`, restart and verify the edited rewards are used.
23. Test the journal at small, standard and ultrawide GUI sizes.
24. Play a normal survival session and check logs for repeated errors or tick-time regressions.
