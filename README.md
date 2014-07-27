# Plugin KTP

C'est très la source.

[![Build Status](https://travis-ci.org/AmauryCarrade/KTP.svg?branch=master)](https://travis-ci.org/AmauryCarrade/KTP)

## Features

 - **Manages an UHC match.**
 - Cancels any regeneration by food (just like `/gamerule naturalRegeneration false`).
 - **Manages teams**
    - Creation/deletion of colored teams, with or without name.
    - Players can be added to a team, or not (solo mode supported).
    - The names of the players can be colored according to the team in the chat.
    - The teams can be registered in the config file, in game, or both.
    - The friendly fire is enabled, as well as the « can see friendly invisible » option.
    - The teams are managed through commands (to allow the use of command blocks).
 - **Automatic teleportation** of the players
    - Manual teleportation spots set in the config, in game, or both.
    - Automatic set of teleportation spots is not supported.
    - The teams (or players, if solo) are automatically teleported to a random teleportation spot.
    - Two teleportation modes available:
       -  a “direct” one, all players are teleported in the same time;
       - a “slow” one, the teams are teleported with a delay, and the game is launched after; useful for smaller servers.
    - The players state is reset when they are teleported: inventory cleaned, max food and health, no XP, no potion effects...
    - Some players can be marked as “spectators”: they are not teleported and not counted as a player.
    - Before the teleportation:
       - players can't build in the world (except with a permission/op mode);
       - the time is frozen at noon;
       - the mobs don't spawn;
       - the players can't be damaged.
    - After the teleportation:
       - 30 seconds after, the players can be damaged, before they are invulnerable. 
 - **Displayed match info** with a custom title
    - Shows the number of the episode and the number of alive players/teams.
    - Shows the time left in the current episode.  
      ![Scoreboard](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Scoreboard.png)
    - You can choose what is displayed in the scoreboard, or disable it.
 - The health of the players is displayed in the `Tab` list.
 - **Episodes management**
    - Configurable duration of an episode.
    - A timer is displayed on the left of the player's screen.
    - The timer can be synchronized with a clock (enabled by default, avoid long episodes due to the lag) or simply decremented every second.
    - The end of an episode is broadcast in the chat to all players.
    - An episode can be shifted using `/uh shift`.
    - You can disable this feature.
 - **Wall generator included**
    - Generates a wall around the map following the size of the map set in the config file, centered on the world' spawn point.
    - The generated wall is made of two blocks: one replaces the “air-like” blocks and the trees, and the other replaces solid blocks. This is useful to have a glass wall without light gaps in the caverns.
       - Illustrations:  
         ![viewed from the surface](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Wall_Surface.png)  
         ![in a cave](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Wall_Cave.png)  
         (using the default configuration).
    - The players are blocked inside the wall, even if the wall is broken or not generated.
       - If WorldBorder is present, it is automatically configured.
       - Else, the plugin will check itself if a player is outside the border (not recommended, use WorldBorder if possible!).
 - **Gameplay tweaks** (all optional, see configuration file)
    - The golden melon is crafted using a gold block instead of eight gold nuggets.  
       ![Craft golden melon](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_GlisteringMelon.png)
    - When a player die, his head is dropped (pvp-only flag available); this head can be used to craft a golden apple.
       - The craft is the same as the normal golden apple, with a head instead of an apple.  
         ![Craft golden Apple from human](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_GoldenAppleFromHuman.png)
       - You can configure the number of apples crafted, the type (normal or Notch apple), and if a lore is added (saying “Made from the fallen head of *ThePlayer*”).  
         ![Lure of a golden apple crafted from a Wither](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_LoreGoldenApple.png)
       - You can also do the same from wither heads.  
         ![Craft from a wither head](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_GoldenAppleFromWither.png)
       - There is a way to remove the lore (because two golden apple with a different lore are not stackable), either in the config (lore not added) or using a craft:  
         ![Craft - Lore removal](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_LoreGoldenAppleRemoval.png)
    - The enderpearl damages can be removed.
    - The ghast tears can be replaced by gold, to make Regeneration potions uncraftable.
    - The level-II potions can be disabled.
    - The compass behavior can be changed.
       - New craft: redstone in center; then from the top, clockwise: iron, spider eye, iron, rotten flesh, iron, bone, iron, gunpowder.  
         ![Craft for the special compass](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_Compass.png)
       - If a player right-click with a compass, the compass shows the nearest player (team excluded).
       - This consumes a rotten flesh.
    - The time can be frozen to any hour (eternal day/night), or not.
    - The “bad” weather (rain, thunder) can be disabled.
 - The players can be kiked (and eventually banned) after their death, with a configurable delay.
 - The death messages can be more visible (format can be set in the config, including color).
 - A team-death message can be sent when an entire team is dead.
 - Integration of SpectatorPlus for the management of the spectators, if the plugin is present.
 - **Dynmap integration**
    - Can display the spawn points of the teams, using colored flags.
    - Can display the death points of the players.  
      ![Dynmap illustration](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Dynmap.png)
 - **Some commands can handle bugs** (like lag)
    - `/uh heal <player> [half-hearts=20]` to heal a player to the exact number of hearts given. Avoid trying to find the good health effect level/duration, the id of the effect, etc.
    - `/uh healall [half-hearts=20]`: the same, for all players, because Bukkit does not allow the use of the @a selector out of a command block.
    - You cannot kill a player with these commands.
    - With `/uh resurrect <player>`, you can resurrect (and deban, if he was banned) a player killed by lag (or other bug).
    - With `/uh tpback <player>`, you can teleport a player back to his death location.
 - The commands can be accessed using permissions (see subsection below).
 - Autocompletion everywhere.
 - Fully translated into English and French.
 - For developers:
    - the game can be controlled through the GameManager (`UHPlugin.getGameManager()`); 
    - the teams, using the TeamManager (`UHPlugin.getTeamManager()`);
    - the scoreboard, using the... ScoreboardManager, yes (`UHPlugin.getScoreboardManager()`);
    - the wall generator, using the `UHWallGenerator` class.
 - Compatible with Bukkit 1.7.9+.
 - Lightweight (as much as possible).


## Documentation

Documentation available via `/uh`.

Legend: `/uh command <required> [optional=default]`.

### Game-related commands
 - `/uh start` : launches the game.
 - `/uh start slow` : launches the game slowly, in two steps, for smaller servers.
 - `/uh shift` : shifts an episode.
 - `/uh team` : manages the teams (add/remove team, add/remove a player into a team, list, reset).
 - `/uh addspawn` : adds a spawn point for a team or a player, at the current location of the sender.
 - `/uh addspawn <x> <z>` : adds a spawn point for a team or a player, at the provided coordinates.
 - `/uh spec` : manages the spectators (aka players ignored by /uh start) (add, remove, list).
 - `/uh generatewalls` : generates the walls according to the configuration.

### Bugs-related commands
 - `/uh heal <player> [half-hearts=20]` : heals a player to the number of half-hearts provided (default 20).
 - `/uh healall [half-hearts=20]` : heals all players instead of only one.
 - `/uh resurrect <player>` : resurrects a player.
 - `/uh tpback <player> [force]` : safely teleports back a player to his death point.

### Permissions

 - `uh.build`:  allows an user to build before the beginning of the game. Default: operator.
 - `uh.<command>`: allows an user to use the command `/uh <command>`. Default: operator.

If you don't want to bother with permissions: the operators can do anything; the non-ops, nothing (except playing).


## Licence

GPLv3. Voir le fichier LICENSE pour détails.
