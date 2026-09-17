# StepUpAgain — Minecraft 26.3 compatibility

Version: `1.4.6+mc26.3`. Reviewed on 2026-09-16.

## Dependencies

| Component | Fabric | NeoForge |
| --- | --- | --- |
| Minecraft | 26.3 | 26.3 |
| Java | 25 | 25 |
| Loader | Fabric Loader 0.19.5 | NeoForge 26.3.0.3-beta |
| API | Fabric API 0.160.6+26.3 | bundled with NeoForge |
| Build plugin | Loom 1.17.21 | ModDevGradle 2.0.147 |
| Gradle wrapper | 9.6.0 | 9.6.0 |

## Breaking changes affecting this mod

Minecraft 26.3 replaces GLFW with SDL. The old `org.lwjgl.glfw.GLFW` imports cannot be carried forward. Keyboard mappings now use `InputConstants.Type.KEYBOARD`; `KEYSYM` no longer exists. Both ports use `InputConstants.KEY_J`, `KEY_K`, and `MOUSE_BUTTON_LEFT`.

Inspection of the official 26.3 client confirmed that J/K have values 13/14 and left mouse has value 1. Hardcoding the old GLFW numbers (74/75 and 0) would therefore be incorrect even if the imports were removed. Production code uses named Minecraft constants, never these numeric literals.

Both actions still register with the loader's key-mapping API under the StepUp category and consume clicks through `KeyMapping.consumeClick()`. Rebinding stays under Minecraft's control, including mouse bindings and the unbound state. Existing translation identifiers are preserved, but fixed `(J)`/`(K)` suffixes were removed from action labels so they do not contradict a custom binding.

The Auto-Jump control still checks the actual vanilla option widget; its mouse-button comparison now uses the SDL-compatible Minecraft constant. `OptionsList.findOption/resetOption`, the step-height attribute, and per-server config code are also compiled against 26.3. This does not establish compatibility with future 26.4 releases or server-specific movement restrictions.

The old Fabric overview incorrectly claimed support through 26.3 even though its 26.2 manifest excluded that version. The overview now matches the old manifest, and separate 26.3 projects preserve the previous versions.

## Validation

- Fabric and NeoForge: `gradlew.bat build` passed with Java 25; three key-mapping regression tests passed for each loader (six total).
- Tests instantiate each production mapping factory against Minecraft classes and check default key matching, restoring defaults, keyboard/mouse rebinding, unbinding, and key serialization round trips. Fabric additionally tests click dispatch and consumption. NeoForge unit tests check event matching because its global click dispatcher requires a live client for modifier state.
- Fabric: `gradlew.bat runClient` loaded Minecraft 26.3, Fabric API, and StepUp; runtime log includes a singleplayer join and mode/cycle messages.
- NeoForge: `gradlew.bat runClient` loaded Minecraft 26.3 and StepUp and completed normal client shutdown. Its startup identified the deprecated `logoFile` metadata property; the final JAR uses `iconFile` for the existing square icon and was rebuilt with all tests passing.
- Packaged JARs were checked for the correct version, Minecraft range, entry classes, and absence of test classes.
- These unit tests do not automate the Controls screen, exercise a physical non-US keyboard, or prove movement behavior on multiplayer servers.

The inherited ModDevGradle 2.0.141 failed while preparing vanilla `HolderSet` sources for 26.3, before compiling the mod. Updating to 2.0.147 resolved that build-tool incompatibility. NeoForge tests use the official ModDevGradle JUnit integration.

## Manual checks before publishing

1. In Options > Controls > Key Binds > StepUp, redefine each action, reset it, bind it to a mouse button, and unbind it. Check the displayed labels and conflicts.
2. Restart the client and check that Minecraft preserved custom bindings.
3. In a world, cycle modes with J and toggle the vanilla-mode inclusion with K. Check that the old key no longer triggers an action after rebinding.
4. In Controls, cycle the Auto-Jump button with both mouse and keyboard activation, with the vanilla mode included and excluded.
5. Walk against a full block; verify StepUp, Disabled, Vanilla, and sneaking behavior. Reconnect and check the per-server mode.

## Sources

- [Fabric 26.3 migration notes (SDL and build requirements)](https://www.fabricmc.net/2026/09/15/263.html)
- [Minecraft 26.3 release notes](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3)
- [Fabric loader metadata for 26.3](https://meta.fabricmc.net/v2/versions/loader/26.3)
- [Fabric API Maven metadata](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml)
- [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml)
- [ModDevGradle plugin metadata](https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml)

Input types/constants and option-widget method signatures were additionally inspected directly using `javap` on Mojang's official 26.3 client JAR, downloaded through the official version manifest.
