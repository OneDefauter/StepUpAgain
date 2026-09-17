# StepUp NeoForge

An updated NeoForge build of **StepUp** for Minecraft `26.3`.

Current release: `1.4.6+mc26.3`.

## Release Info

- Loader: `NeoForge`
- Minecraft: `26.3`
- NeoForge: `26.3.0.3-beta`
- Version: `1.4.6+mc26.3`
- Environment: `Client-side`

StepUp increases the player's step height so you can walk up blocks smoothly instead of jumping into them. The result feels similar to vanilla auto-jump at first glance, but the movement is more direct and avoids the awkward timing and hunger cost of repeated jumps.

## Modes

- `StepUp`: raises the player's step height above vanilla and falls back to normal height while sneaking.
- `Disabled`: turns StepUp off and keeps vanilla auto-jump off.
- `Vanilla Auto Jump`: disables StepUp's custom step-height behavior and enables Minecraft's normal auto-jump option.

## Controls

StepUp has its own category in Minecraft's Controls menu.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- Press `K` to include or remove `Vanilla Auto Jump` from the `J` toggle cycle.
- Minecraft's vanilla Auto-Jump option in Controls cycles between `StepUp`, `Vanilla`, and `Off`.
- If `Vanilla Auto Jump` is removed from the cycle, the Auto-Jump option only cycles between `StepUp` and `Off`.
- The mod shows a short status message in chat after every mode change.

## Configuration

StepUp stores its settings in:

```text
config/stepup.json
```

The configuration is saved per server, so singleplayer and each multiplayer server can keep different movement modes.

## Requirements

- Minecraft Java Edition `26.3`
- NeoForge `26.3.0.3-beta`
- Java 25

## Installation

1. Install NeoForge for Minecraft `26.3`.
2. Put the StepUp jar in your `mods` folder.
3. Start the game.

Because this mod is client-side, it does not require a server-side installation for normal use.

## Compatibility

This port targets NeoForge on Minecraft `26.3` through the dependency range `[26.3,26.4)`.

## 26.3 input compatibility

Minecraft 26.3 replaces GLFW with SDL. This build uses Minecraft's `InputConstants` for keyboard and mouse input and the new `KEYBOARD` mapping type. J and K remain the defaults; both actions can be rebound in Options > Controls > Key Binds > StepUp, including mouse buttons.

See [the compatibility and validation report](COMPATIBILITY-26.3.md) for the checks performed and remaining manual checks.

## Changelog

### `1.4.6+mc26.3`

- Updated to Minecraft `26.3` and NeoForge `26.3.0.3-beta`.
- Migrated J/K key mappings and the Auto-Jump mouse handler from GLFW to Minecraft 26.3 SDL input.
- Preserved the Auto-Jump Controls cycle, per-server configuration, and sneaking behavior.

## License

This fork continues to respect the upstream project's `MIT` license.
