# Building StepUp for Minecraft 26.3

Use Java 25 and the included Gradle 9.6.0 wrapper. The first build needs internet access.

```powershell
.\gradlew.bat build
```

On Linux/macOS, use `./gradlew build`. Build runs the key-mapping regression tests and writes the mod JAR to `build/libs/`. The test report is in `build/reports/tests/test/index.html`.

To start a separate development client:

```powershell
.\gradlew.bat runClient
```

The development client uses the local `run/` directory. It does not install the mod into your launcher profile. See [the compatibility report](COMPATIBILITY-26.3.md) for verification scope and manual checks.