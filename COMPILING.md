# Building StepUp

## Requirements

- Java 25
- Internet access for Gradle to download Minecraft/Forge dependencies on the first build

## Build

Use the Gradle wrapper that ships with the repository.

On Windows:

```powershell
.\gradlew.bat build
```

On macOS/Linux:

```sh
./gradlew build
```

The built jar will be written to `build/libs/`.

## Notes

- This branch targets Minecraft `26.1`.
- The build uses Minecraft Forge `62.0.2`.
- The wrapper downloads Gradle `9.3.1`.
- Minecraft `26.1` requires Java 25 for the Gradle JVM.

