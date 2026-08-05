# 0.1.0-dev.4 — Progression Gates & Elite Encounters

## Goal

Turn the existing restriction and elite foundations into reliable, understandable gameplay systems while preserving all 0.1.0-dev.3 profiles and progression data.

## Progression gates

- Enforce tool, weapon, shield and armour restrictions consistently across use, attack, mining, equipping and inventory interactions.
- Show clear denial feedback containing the missing skill level and required purchased node.
- Harden Nether, End and Elytra progression checks against common bypass routes.
- Add safe return behavior when players enter a gated dimension without meeting its requirements.
- Keep all gate values configurable and server-authoritative.
- Add automated tests and an in-game bypass checklist.

## Elite encounters

- Finish vanilla-themed elite role identity and readable names.
- Add configurable equipment, attributes, rarity and progression scaling.
- Add spawn eligibility rules and per-area limits to avoid conversion loops or excessive density.
- Add configurable elite rewards.
- Keep elite logic event-driven and lightweight.

## Journal and feedback

- Surface equipment and travel requirements more clearly.
- Add basic elite information and progression hints.
- Improve messages for denied actions without creating chat spam.

## Branch policy

- `main` remains the playable 0.1.0-dev.3 build during development.
- Bugs found while testing dev.3 may be fixed on main as a hotfix and then carried into this branch.
- The branch will not be promoted until validation, Java 25/Fabric compilation, tests and the runtime checklist pass.
