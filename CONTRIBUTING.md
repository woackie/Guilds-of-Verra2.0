# Contributing

Use JDK 25. Run `python3 scripts/validate_project.py` and `./gradlew test build` before opening
a pull request. Keep common code free of client-only imports, preserve stable node IDs and add
a saved-data migration for any schema change.
