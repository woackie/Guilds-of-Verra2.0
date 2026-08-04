# First in-game testing checklist

1. Create a new Fabric 26.2 world and confirm a profile is attached on join.
2. Run `/gv profile` and confirm five skills start at level 0.
3. Mine natural stone and ores; verify Mining XP and level-up messages.
4. Damage and kill hostile mobs; verify Combat XP is server-authoritative.
5. Cross 128-block exploration-cell boundaries; verify Exploration XP.
6. Use `/gv xp <player> mining <amount>` to reach level 20.
7. Confirm an iron pickaxe remains locked until `iron_tool_mastery` is purchased.
8. Purchase the node and verify iron tools work immediately.
9. Verify sword, axe, bow, shield and armour tag restrictions independently.
10. Attempt Nether entry below and above its combined requirements.
11. Reconnect and verify levels, nodes and prestige are preserved.
12. Open the journal with `J` and confirm synchronized values.
13. Spawn hostile mobs at Adventurer Level 10+ and inspect rare elite conversion.
14. Test with two players at different levels to catch data leakage.
15. Run a profiler during combat/mining/exploration and check for tick-time regressions.
