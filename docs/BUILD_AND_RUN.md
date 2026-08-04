# Build and run

## Requirements

- 64-bit JDK 25
- Internet access for the first Gradle dependency download
- Minecraft Java Edition 26.2 development dependencies

## Windows

```bat
set JAVA_HOME=C:\Path\To\jdk-25
gradlew.bat clean build
```

## Linux or macOS

```sh
export JAVA_HOME=/path/to/jdk-25
./gradlew clean build
```

The remapped mod JAR is created in `build/libs/`. Files ending in `-sources.jar` are source
archives and should not be installed as the playable mod.

## Development client

```sh
./gradlew runClient
```

## Dedicated development server

```sh
./gradlew runServer
```

Accept the generated EULA only when you are ready to start the local development server.

## Validation without Minecraft

```sh
python3 scripts/validate_project.py
```

This checks all JSON, node IDs, prerequisites, point totals and the approved XP curve.

The pure Java core can also be compiled independently:

```sh
mkdir -p build/core-validation
javac -d build/core-validation src/main/java/com/guildsofverra/core/*.java tools/CoreSelfTest.java
java -cp build/core-validation CoreSelfTest
```
