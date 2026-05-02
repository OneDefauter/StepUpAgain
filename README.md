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

StepUp has its own category in Minecraft's Controls menu.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- Press `K` to include or remove `Vanilla Auto Jump` from the `J` toggle cycle.
- Both keys can be changed in Minecraft's Controls menu.
- Minecraft's vanilla Auto-Jump option in Controls cycles between `StepUp`, `Vanilla`, and `Off`.
- If `Vanilla Auto Jump` is removed from the cycle, the Controls option only cycles between `StepUp` and `Off`.
- The mod shows a short status message in chat after every mode change.

## Configuration

StepUp stores its settings in:

```text
config/stepup.json
```

The configuration is saved per server, so singleplayer and each multiplayer server can keep different movement modes.

Forge's Mods screen also exposes a StepUp config screen. Use it to include or remove `Vanilla Auto Jump` from the mode toggle cycle.

## Requirements

- Minecraft Java Edition `26.1.x`
- Minecraft Forge `62.0.2` or newer for `26.1`
- Java 25

## Installation

1. Install Minecraft Forge for version `26.1.x`.
2. Put the StepUp jar in your `mods` folder.
3. Start the game.

Because this mod is client-side, it does not require a server-side installation for normal use.

## Compatibility

This port targets Forge on Minecraft `26.1.x` through the dependency range `[26.1,26.2)` and uses Mojang's official mappings through ForgeGradle.

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
