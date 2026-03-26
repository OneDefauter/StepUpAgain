# StepUp

A Forge port of **StepUp** for Minecraft `26.1`.

StepUp increases the player's step height so you can walk up blocks smoothly instead of jumping into them. The result feels similar to vanilla auto-jump at first glance, but the movement is more direct and avoids the awkward timing and hunger cost of repeated jumps.

## What the mod does

- Lets you walk up full blocks more smoothly.
- Keeps movement feeling closer to normal walking than to jumping.
- Gives you three movement modes that can be switched at any time.
- Works entirely on the client, so it can be used in singleplayer and on vanilla-compatible servers.
- Saves its mode per server, so you can enable it where it works well and keep different behavior elsewhere.

## Modes

The mod cycles through three modes:

### `StepUp`

- Raises the player's step height above vanilla.
- Lets you walk over full blocks without jumping.
- Falls back to normal step height while sneaking for better control.

### `Disabled`

- Turns StepUp off.
- Keeps vanilla auto-jump off.

### `Vanilla Auto Jump`

- Disables StepUp's custom step-height behavior.
- Re-enables Minecraft's normal auto-jump option.

## Controls

By default, the mode toggle is bound to `J`.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- The key can be changed in Minecraft's Controls menu.
- The mod shows a short status message in chat after every mode change.

## Configuration

StepUp stores its settings in:

```text
config/stepup.json
```

The configuration is saved per server, so singleplayer and each multiplayer server can keep different movement modes.

## Requirements

- Minecraft Java Edition `26.1`
- Minecraft Forge `62.0.2` or newer for `26.1`
- Java 25

## Installation

1. Install Minecraft Forge for version `26.1`.
2. Put the StepUp jar in your `mods` folder.
3. Start the game.

Because this mod is client-side, it does not require a server-side installation for normal use.

## Compatibility

This port targets Forge on Minecraft `26.1` and uses Mojang's official mappings through ForgeGradle.

## Notes

- This mod is designed to work on the client only.
- It should work with vanilla-compatible servers in normal cases.
- Some servers may still interfere with the effect if they change movement behavior or enforce custom restrictions.

## Credits

Full credit for the original public **StepUp** mod release and maintenance goes to **Giselbaer**.

- Original author profile: <https://modrinth.com/user/Giselbaer>
- Original mod page: <https://modrinth.com/mod/stepup>

The upstream history of the project also includes earlier StepUp/StepUpNext work reflected in the archived repository and license notices. This fork keeps that lineage intact while updating compatibility for current Forge versions.

## License

This fork continues to respect the upstream project's `MIT` license.
