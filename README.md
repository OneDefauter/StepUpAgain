# StepUp

An updated Fabric build of **StepUp** for Minecraft 1.15.x.

Current release: `1.4.1+mc1.15`.

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

By default, the mode toggle is bound to `J`.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- If you disable the vanilla mode in the config, `J` switches only between `StepUp` and `Disabled`.

## Configuration

StepUp stores its settings in `config/stepup.json`.

The file keeps:

- a default state for new worlds and unknown servers,
- a toggle that decides whether `Vanilla Auto Jump` is included in the `J` cycle,
- and a list of server-specific overrides keyed by server address or singleplayer.

If you have both **Mod Menu** and **Cloth Config** installed, StepUp exposes this option through a config screen in Mod Menu. Without them, the mod still works normally and keeps using `config/stepup.json`.

## Requirements

- Minecraft Java Edition `1.15`, `1.15.1`, `1.15.2`
- Fabric Loader
- Fabric API
- Java 8 or newer

## Compatibility

This fork targets the full Minecraft 1.15.x line.

The jar is intended to run on:

- `1.15`
- `1.15.1`
- `1.15.2`

## Changelog

### `1.4.1+mc1.15`

- Added the Fabric `1.15.x` build of StepUpAgain with support from `1.15` through `1.15.2`.
- Included optional integration with Mod Menu and Cloth Config.
- Added a configuration option to remove `Vanilla Auto Jump` from the `J` toggle cycle.

## Credits

Full credit for the original public **StepUp** mod release and maintenance goes to **Giselbaer**.

- Original author profile: <https://modrinth.com/user/Giselbaer>
- Original mod page: <https://modrinth.com/mod/stepup>

This fork exists only to keep the mod usable on newer Fabric versions while preserving the upstream credits and license.

## License

This fork continues to respect the upstream project's `MIT` license.
