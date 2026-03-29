# StepUp

An updated Fabric build of **StepUp** for Minecraft 1.14.x.

Current release: `1.4.1+mc1.14`.

StepUp increases the player's step height so you can walk up blocks smoothly instead of jumping into them. The result feels similar to vanilla auto-jump at first glance, but the movement is more direct and avoids the awkward timing and hunger cost of repeated jumps.

## What the mod does

- Lets you walk up full blocks more smoothly.
- Keeps movement feeling closer to normal walking than to jumping.
- Gives you three movement modes that can be switched at any time.
- Works entirely on the client, so it can be used in singleplayer and on vanilla-compatible servers.
- Saves its mode per server, so you can enable it where it works well and keep different behavior elsewhere.

## Modes

By default, the mod cycles through three modes:

- `StepUp`: raises the player's step height so full blocks can be walked over without jumping.
- `Disabled`: turns StepUp off and also disables vanilla auto-jump.
- `Vanilla Auto Jump`: disables StepUp and re-enables Minecraft's normal auto-jump.

By default, the main mode toggle is bound to `J`.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- Press `K` to enable or disable whether `Vanilla Auto Jump` is included in the `J` cycle.
- If you disable the vanilla mode, `J` switches only between `StepUp` and `Disabled`.
- Both key bindings are listed under their own `StepUp` category in Minecraft's Controls screen.

## Configuration

StepUp stores its settings in `config/stepup.json`.

The file keeps:

- a default state for new worlds and unknown servers,
- a toggle that decides whether `Vanilla Auto Jump` is included in the `J` cycle,
- and a list of server-specific overrides keyed by server address or singleplayer.

Cloth Config can be used across the `1.14.x` line, but Mod Menu integration is only available on `1.14.4`, because Mod Menu does not exist for `1.14` through `1.14.3`. On those earlier patch versions, the setting still works through `config/stepup.json` and through the in-game `K` key bind.

## Requirements

- Minecraft Java Edition `1.14`, `1.14.1`, `1.14.2`, `1.14.3`, `1.14.4`
- Fabric Loader
- Fabric API
- Java 8 or newer

## Compatibility

This fork targets the full Minecraft 1.14.x line.

The jar is intended to run on:

- `1.14`
- `1.14.1`
- `1.14.2`
- `1.14.3`
- `1.14.4`

## Changelog

### `1.4.1+mc1.14`

- Added the Fabric `1.14.x` build of StepUpAgain with support from `1.14` through `1.14.4`.
- Included optional integration with Mod Menu and Cloth Config.
- Added a configuration option to remove `Vanilla Auto Jump` from the `J` toggle cycle.
- Added a dedicated key bind to toggle that setting in-game and moved the StepUp key binds into their own Controls category.

## Credits

Full credit for the original public **StepUp** mod release and maintenance goes to **Giselbaer**.

- Original author profile: <https://modrinth.com/user/Giselbaer>
- Original mod page: <https://modrinth.com/mod/stepup>

This fork exists only to keep the mod usable on newer Fabric versions while preserving the upstream credits and license.

## License

This fork continues to respect the upstream project's `MIT` license.
