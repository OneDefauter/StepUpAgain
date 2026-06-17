# StepUp Fabric

An updated Fabric build of **StepUp** for Minecraft `26.2`.

Current release: `1.4.5+mc26.2`.

## Release Info

- Loader: `Fabric`
- Minecraft: `26.2`
- Version: `1.4.5+mc26.2`
- Environment: `Client-side`

StepUp increases the player's step height so you can walk up blocks smoothly instead of jumping into them. The result feels similar to vanilla auto-jump at first glance, but the movement is more direct and avoids the awkward timing and hunger cost of repeated jumps.

This makes the mod especially useful for everyday traversal, base building, path walking, caves, stairs, uneven terrain, and situations where vanilla auto-jump feels slow or intrusive.

## What the mod does

- Lets you walk up full blocks more smoothly.
- Keeps movement feeling closer to normal walking than to jumping.
- Gives you three movement modes that can be switched at any time.
- Works entirely on the client, so it can be used in singleplayer and on vanilla-compatible servers.
- Saves its mode per server, so you can enable it where it works well and keep different behavior elsewhere.

In practice, the mod changes how movement feels when you approach blocks that would normally require a jump. Instead of repeatedly hopping up terrain, StepUp raises the player's step height and lets the character climb over the block edge in a more continuous way.

## Why use it instead of vanilla auto-jump

Vanilla auto-jump can be useful, but many players dislike it because it:

- triggers when you do not want it to,
- changes movement timing,
- makes traversal feel less precise,
- adds repeated jump behavior,
- and applies the normal hunger penalty of jumping.

StepUp aims to keep the useful part of auto-jump, walking over obstacles, without inheriting those downsides.

## Modes

By default, the mod cycles through three modes:

### `StepUp`

This is the main feature mode.

- The mod raises the player's step height above vanilla.
- You can walk over full blocks without jumping.
- Movement feels smooth and continuous.
- While sneaking, the mod temporarily falls back to normal step height for better control.

### `Disabled`

- StepUp is turned off.
- Vanilla auto-jump is also turned off.
- Movement returns to standard non-autojump behavior.

### `Vanilla Auto Jump`

- StepUp's custom step-height behavior is disabled.
- Vanilla auto-jump is turned on through the normal Minecraft option.
- This is useful if you want to compare the two behaviors quickly.

## Controls

By default, `J` toggles the active movement mode and `K` toggles whether `Vanilla Auto Jump` is included in that cycle.

- Press `J` to switch between `StepUp`, `Disabled`, and `Vanilla Auto Jump`.
- Press `K` to add or remove `Vanilla Auto Jump` from the `J` cycle.
- Both keys can be changed in Minecraft's Controls menu under the `StepUp` category.
- Minecraft's vanilla Auto-Jump option in Controls cycles between `StepUp`, `Vanilla`, and `Off`.
- If `Vanilla Auto Jump` is removed from the cycle, the Auto-Jump option only cycles between `StepUp` and `Off`.
- When you switch modes, the mod shows a short status message in chat so you can see the active mode immediately.
- If you disable the vanilla mode in the config, `J` switches only between `StepUp` and `Disabled`.

## Configuration

StepUp stores its settings in:

```text
config/stepup.json
```

The configuration is saved per server. That means:

- singleplayer can have one default behavior,
- one multiplayer server can use `StepUp`,
- another can stay on `Disabled`,
- and another can stay on `Vanilla Auto Jump`.

This is useful because some servers may feel better with the feature enabled, while others may have movement rules, plugins, or anti-cheat setups where you prefer to leave it off.

The file keeps:

- a default state for new worlds and unknown servers,
- a toggle that decides whether `Vanilla Auto Jump` is included in the key toggle cycle,
- and a list of server-specific overrides keyed by server address or singleplayer.

Use `K` to include or remove `Vanilla Auto Jump` from the `J` cycle. The same setting is still saved in `config/stepup.json`, so it can also be edited manually while the game is closed.

## Typical use cases

- Walking around bases with slabs, stairs, and small elevation changes.
- Exploring hills, caves, and uneven terrain without constant hopping.
- Building while moving across partial structures and rough surfaces.
- Playing with auto-jump-like convenience without wanting the normal vanilla behavior.
- Keeping different movement preferences for different servers.

## Requirements

- Minecraft Java Edition `26.2`
- Fabric Loader
- Fabric API
- Java 25

## Installation

1. Install Fabric Loader for your Minecraft version.
2. Install Fabric API for the same version.
3. Put the StepUp jar in your `mods` folder.
4. Start the game.

Because this mod is client-side, it does not require a server-side installation for normal use.

## Compatibility

This Fabric fork was updated specifically for Minecraft `26.2`.

This port includes the required 26.2 migration work:

- migration from Yarn to Mojang's official mappings,
- the unobfuscated `net.fabricmc.fabric-loom` build setup,
- Java 25 / Fabric 26.2 API compatibility,
- Fabric Loader `0.19.3`,
- and Fabric API `0.152.1+26.2`.

## Notes

- This mod is designed to work on the client.
- It should work with vanilla-compatible servers in normal cases.
- Some servers may still interfere with the effect if they change movement behavior or enforce custom restrictions.

## Changelog

### `1.4.5+mc26.2`

- Updated to Minecraft `26.2`, Fabric Loader `0.19.3`, Fabric API `0.152.1+26.2`, Loom `1.17.11`, and Gradle `9.5.1`.
- Removed external config-screen integrations.
- Kept configuration through Minecraft key mappings and `config/stepup.json`.
- Added the Auto-Jump Controls button cycle for `StepUp`, `Vanilla`, and `Off`, respecting the Vanilla cycle setting.

## Credits

Full credit for the original public **StepUp** mod release and maintenance goes to **Giselbaer**.

- Original author profile: <https://modrinth.com/user/Giselbaer>
- Original mod page: <https://modrinth.com/mod/stepup>

This fork exists only to keep the mod usable on newer Fabric versions.

The upstream history of the project also includes earlier StepUp/StepUpNext work reflected in the archived repository and license notices. This fork keeps that lineage intact while updating compatibility for current versions.

## License

This fork continues to respect the upstream project's `MIT` license.
