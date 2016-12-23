# UltraHardcore, reloaded [![Build Status](http://jenkins.carrade.eu/job/UltraHardcoreReloaded/badge/icon)](http://jenkins.carrade.eu/job/UltraHardcoreReloaded/)


C'est très la source.

Development builds available for each commit [on our Jenkins server](http://jenkins.carrade.eu/job/UltraHardcoreReloaded/).

This plugin is [available on BukkitDev](http://dev.bukkit.org/bukkit-plugins/uhc/).  
Please note, this README reflects the last development version. Some features listed here may not be in the version published on BukkitDev.


1. [Features](#features)
   1. [Teams](#manages-teams)
   1. [Teleportation](#automatic-teleportation-of-the-players)
   1. [Scoreboards](#scoreboards)
   1. [Episodes](#episodes-management)
   1. [Border & wall](#border-manager-and-wall-generator-included)
   1. [Gameplay tweaks](#gameplay-tweaks)
   1. [Death](#death)
   1. [Timers](#timers)
   1. [Other plugins](#integration-into-other-plugins)
   1. [Options against lag & bugs](#provided-with-options-to-deal-with-lag-and-other-bugs)
   1. [Miscellaneous](#miscellaneous-features)
1. [Commands & permissions](#commands--permissions)
1. [Installation](#installation)
1. [How to translate the plugin](#how-to-translate-the-plugin-or-change-strings-in-your-own-language)
   1. [If you want to translate it for yourself](#if-you-want-to-translate-it-for-yourself)
   1. [If you want to translate it for everyone](#if-you-want-to-translate-it-for-everyone)
   1. [Inside the translations](#inside-the-translations)
1. [Metrics](#metrics)
1. [Thanks](#thanks)
   1. [Translators](#translators)
1. [License](#license)


## Features

For available configuration, please [see directly the configuration file](https://github.com/AmauryCarrade/UHPlugin/blob/master/src/main/resources/config.yml).

 - **Manages an UHC match.**
 - Cancels any regeneration by food (just like `/gamerule naturalRegeneration false`).

### Manages teams

- Creation/deletion of colored teams, with or without name. Spaces allowed in the names.
- You can create a team with a random color using `?` as the color.
- Players can be added to a team, or not (solo mode supported).
- The names of the players can be colored, according to the team, in the chat.
- The teams can be registered in the config file, in game, or both.
- The teams are managed through commands (to allow the use of command blocks).
- You can set a maximum size for the teams (to be able to give players command blocks to join the teams, without manual check, as example).
- Players can join a team using `/join <teamName>` or a nice chat-based GUI.  
  ![Chat-based GUI to select a team](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_TeamsMenu.png)

### Automatic teleportation of the players

- Manual teleportation spots set in the config, in game, or both.
- Automatic set of teleportation spots following various shapes: `random`, `grid` or `circular`.  
  All these shapes come with a minimal distance between two points, and the spots can be generated with any center, in any world (default: the sender's world, centered on the spawn of this world).
- You can remove a spawn point, or all.
- The teams (or players, if solo) are automatically teleported to a random teleportation spot.
- Two teleportation modes available:
   - a “direct” one, all players are teleported in the same time;
   - a “slow” one, the teams are teleported with a delay, and the game is launched after; useful for smaller servers.
- The players state is reset when they are teleported: inventory cleaned, max food and health, no XP, no potion effects...
- Some players can be marked as “spectators”: they are not teleported and not counted as a player.
- Before the teleportation:
   - players can't build in the world (except with a permission/op mode);
   - the time is frozen at noon;
   - the mobs don't spawn;
   - the players can't be damaged.
- After the teleportation:
   - 30 seconds after, the players can be damaged; before they are invulnerable.

### Scoreboards

- Displayed match info with a custom title
   - Shows the number of the episode and the number of alive players/teams.
   - Shows the time left in the current episode.  
      ![Scoreboard](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Scoreboard.png)
   - You can choose what is displayed in the scoreboard, or disable it.
- The health of the players is displayed in the `Tab` list. This can be disabled.

### Episodes management

- Configurable duration of an episode.
- A timer is displayed on the left of the player's screen.
- The timer can be synchronized with a clock (enabled by default, avoid long episodes due to the lag) or simply decremented every second.
- The end of an episode is broadcast in the chat to all players.
- An episode can be shifted using `/uh shift`.
- You can disable this feature.

### Border manager and wall generator included

- You can generate a wall around the map following the size of the map set in the config file, centered on the world' spawn point.
   - The generated wall is made of two blocks: one replaces the “air-like” blocks and the trees, and the other replaces solid blocks. This is useful to have a glass wall without light gaps in the caverns.
   - Illustrations:  
     ![viewed from the surface](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Wall_Surface.png)  
     ![in a cave](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Wall_Cave.png)  
     (using the default configuration).
   - The generated wall can be either circular or squared.
- The players are blocked inside the wall, even if the wall is broken or not generated, if WorldBorder is present (automatic configuration of the plugin).
- The border can easily be set during the game with `/uh border set <diameter>`
- To anticipate a new border size, a warning can be send to all players out of this future border with `/uh border warning <futureDiameter>` every 90 seconds (by default, you can change the delay in the config).
   - The time left to go inside the next border can be included in this message (`/uh border warning <futureDiameter> [minutesBeforeReduction]`).  
     ![message broadcasted to the players outside](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Border_Warning.png)
- You can list the players outside a given border with `/uh border check <diameter>`; this command gives a very light info (list of players outside plus "far", "close" or "very close") because the administrator can play.
 
### Gameplay tweaks
All these gameplay tweaks are optional — see [the configuration file](https://github.com/AmauryCarrade/UHPlugin/blob/master/src/main/resources/config.yml).
    
- The golden melon is crafted using a gold block instead of eight gold nuggets.  
![Craft golden melon](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_GlisteringMelon.png)
- When a player die, his head is dropped (pvp-only flag available); this head can be used to craft a golden apple.
   - The craft is the same as the normal golden apple, with a head instead of an apple.  
     You can use a player head or a wither head, and configure these two crafts independently.  
     ![Craft golden Apple](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_GoldenHead.gif)
   - You can configure the number of apples crafted, the type (normal or Notch apple), and if a lore is added (saying “Made from the fallen head of *ThePlayer*”).  
     ![Lure of a golden apple crafted from a Wither](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_LoreGoldenApple.png)
   - There is a way to remove the lore (because two golden apple with a different lore are not stackable), either in the config (lore not added) or using a craft:  
     ![Craft - Lore removal](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Craft_LoreGoldenAppleRemoval.png)
- You can configure the number of half-hearts healed by a golden apple, enchanted golden apple, and the same for “golden heads”, independantely.
- You can disable the enchanted golden apples.
- The enderpearl damages can be removed.
- The ghast tears can be replaced by gold, to make Regeneration potions uncraftable.
- The level-II potions can be disabled.
- The compass behavior can be changed.
   - New craft: an ingredient in the center; four iron ingots like in the vanilla compass recipe; bone, gunpowder, rotten flesh and spider eye in the corners.  
     ![Craft for the special compass](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_CraftCompass_Names.gif)
   - The four loots in the corners can be in any configuration (*semi-shapeless* recipe).
   - There are three different ingredients in the center, for three levels of difficulty.
       - `Simple`: redstone dust (just like the vanilla compass).
       - `Medium`: ender pearl.
       - `Hard`: eye of ender.
   - If a player right-click with a compass, the compass shows the nearest player (team excluded).
   - This consumes a rotten flesh.
- The Witch spawn can be disabled (either the natural one, or the spawn from a villagers struck by a lightning bolt).
- The time can be frozen to any hour (eternal day/night), or not.
- The “bad” weather (rain, thunder) can be disabled.

### Death
 
 - The players can be kicked (and eventually banned) after their death, with a configurable delay.
 - The killer can receive some XP (other-team-only flag available).
 - Announcements:
   - The death messages can be more visible (format can be set in the config, including color).
   - A team-death message can be sent when an entire team is dead.
   - A death sound can be played (by default, WITHER_SPAWN), or not.
   - You can send a lightning strike (only an effect, not a “real” one) when a player die, at the location of his death point.

### Timers

 - You can add arbitrary countdowns in game, displayed in the scoreboard or hidden.
 - The timers are added in one time; started in another. So you can prepare all timers before the game.
 - Each timer come with a title, displayed in the scoreboard; this title can be in colors using Minecraft's color codes with `&` (e.g. `&4` for dark red).
 - You can pause a timer, resume it after; if you freeze the entire game all timers are paused.

### Integration into other plugins

 - Integration of WorldBorder, as said in the *Border* section.
 - Integration of SpectatorPlus for the spectators' management, if the plugin is present.
 - **Dynmap integration**
    - Can display the spawn points of the teams, using colored flags.
    - Can display the death points of the players.  
      ![Dynmap illustration](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_Dynmap.png)

### Provided with options to deal with lag and other bugs

- `/uh heal <player> [half-hearts=20|±diff]` to heal a player to the exact number of hearts given, or to add/remove the given number of life points. Avoid trying to find the good health effect level/duration, the id of the effect, etc.
- `/uh healall [half-hearts=20|±diff]`: the same, for all players, because Bukkit does not allow the use of the @a selector out of a command block.
- You cannot kill a player with these commands.
- With `/uh resurrect <player>`, you can resurrect (and deban, if he was banned) a player killed by lag (or other bug).
- With `/uh tpback <player>`, you can teleport a player back to his death location.

### Miscellaneous features

 - **The players can send a private message to their team**
    - Use `/t <message>` to send such a private message.
    - Use `/togglechat` to lock this team-chat and use it without `/t`.
    - From the locked team-chat, you can use `/g <message>` if you want to send a message to the main (global) chat.
    - An administrator can use `/togglechat <team ...>` to enter into the private chat of another team.
 - You can broadcast the winner(s) of the game in the chat and launch some fireworks; either automatically, after the last death, or using `/uh finish`.
 - You can display hardcore hearts instead of normal ones.
   ![Hardcore hearts](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_HardcoreHearts.png)
 - Possible automatic respawn after a delay, to avoid the interrogation about “Can I respawn?” (the client thinks the server is in hardcore mode, and so displays an hardcore death screen).
 - ProTips can be sent automatically, at the good moment, to the players. A sound can be added (default: high-pitched `NOTE_PLING`)  
   *Currently, ProTips about the team-chat, the crafts and the startup invincibility are sent.*
   ![ProTips](http://amaury.carrade.eu/files/Minecraft/Plugins/UH/UHPlugin_ProTips.png)
 - The commands can be accessed using permissions (see subsection below).
 - Autocompletion everywhere.
 - Fully translated into English, French, Portuguese and Brazilian.
 - For developers:
    - the game can be controlled through the GameManager (`UHPlugin.getGameManager()`); 
    - the teams, using the TeamManager (`UHPlugin.getTeamManager()`);
    - the scoreboard, using the... ScoreboardManager, yes (`UHPlugin.getGameManager().getScoreboardManager()`);
    - the wall generator, using the `UHWallGenerator` class;
    - the border, through the BorderManager (`UHPlugin.getBorderManager()`);
    - the freezer, using the Freezer (...yes) (`UHPlugin.getFreezer()`).
 - Compatible with Bukkit 1.7.9+.
 - Lightweight (as much as possible).


## Commands & permissions

Documentation available via `/uh`.  
You can see the documentation of a command with sub-commands by executing the command without subcommand.  
*Example: `/uh team` for the documentation of the team command.*

Legend: `/uh command <required> [optional=default]`.

### Game-related commands
 - `/uh start` : launches the game.
 - `/uh start slow` : launches the game slowly, in two steps, for smaller servers.
 - `/uh shift` : shifts an episode.
 - `/uh team` : manages the teams (add/remove team, add/remove a player into a team, list, reset).
 - `/uh spawns` : manages the spawn points (add, generate, list, dump, remove, reset).
 - `/uh spec` : manages the startup spectators (aka players ignored by /uh start) (add, remove, list).
 - `/uh border`: manages the border (current, set, warning, check)
 - `/uh generatewalls` : generates the walls according to the configuration.

### Bugs-related commands
 - `/uh heal <player> [half-hearts=20|±diff]` : heals a player to the number of half-hearts provided (default 20).
 - `/uh healall [half-hearts=20|±diff]` : heals all players instead of only one.
 - `/uh resurrect <player>` : resurrects a player.
 - `/uh tpback <player> [force]` : safely teleports back a player to his death point.

### Others

 - `/uh freeze` : used to (un)freeze the entire game or some players.
 - `/uh tp <team|spectators>` : teleports a team or the spectators to a given location.
 - `/uh finish` : broadcasts the winner(s) of the game, and sends some fireworks!
 - **Team-chat commands**
    - `/t <message>` : sends a private message to the entire team of the sender.
    - `/togglechat` : toggles the chat between the main chat and the private team chat.
    - `/togglechat <team ...>` : enters the private team chat of another team.
    - `/g <message>` : sends a message to the main chat.
 - `/uh about` : prints informations about the plugin and the translations.

### Permissions

 - `uh.build`:  allows an user to build before the beginning of the game. Default: operator.
 - `uh.teamchat.self`: allows an user to send a private message to his own team. Default: everyone.
 - `uh.teamchat.others`: allows an user to enter into the private chat of another team. Default: operator.
 - `uh.<command>`: allows an user to use the command `/uh <command>`. Default: everyone for `uh.about`, operator else.

If you don't want to bother with permissions: the operators can do anything; the non-ops, nothing (except play and use the team-chat with `/t`, `/togglechat` and `/g` — not `/togglechat <team ...>`).


## Installation

Download the latest version in [the “releases” section](https://github.com/AmauryCarrade/UHPlugin/releases) and put the downloaded JAR file into the `plugins/` folder of your Minecraft server.

This plugin **is not compatible with Minecraft 1.7.4**; Minecraft 1.7.9+ is needed.  
The plugin was tested and works with both CraftBukkit and Spigot.

You can also install these plugins:

 - [WorldBorder](http://dev.bukkit.org/plugin/worldborder/), for all border-check-related tasks;
 - [SpectatorPlus](http://dev.bukkit.org/plugin/spectator/), if you want to enable a spectator mode for dead players;
 - [dynmap](http://dev.bukkit.org/plugin/dynmap/), because the plugin can display the spawn & death points on the map.

This plugin is needed for some options:

 - [ProtocolLib](http://dev.bukkit.org/bukkit-plugins/protocollib/), for the hardcore hearts, the automatic respawn and the team-selector GUI.

After the installation, I recommend you to:

1. teleport yourself to 0,0 (in creative, `/tp 0 100 0`);
2. set the world spawn point here (`/setworldspawn`);
3. reload the server (for the plugin to take into account the change) (`/rl`);
4. pregenerate the entire world (WorldBorder is preconfigured, just execute `/wb fill`);
5. only after that, generate the wall (`/uh generatewalls`) (don't forget to configure the shape and the size in the config file before). Otherwise, holes will be formed in the wall when Minecraft will populate the terrain.


## How to translate the plugin (or change strings) in your own language

### If you want to translate it for yourself

1. Go to the `plugins/UHPlugin/i18n` folder in your server directory. You have to start the server with this plugin at least one time before.
2. Locate your language file. The name is `language_COUNTRY.po`.
3. Open it, either using a simple text editor, or, if you like GUIs, with POEdit, a translation files editor.
4. Add or update translations, save the file, reload the server.

If you update the plugin to a new version, the translation files will be replaced by the new ones, but a backup of your changes will be automatically made in the `plugins/UHPlugin/i18n/backups/<version>/` folder.


### If you want to translate it for everyone

We use [Transifex](https://www.transifex.com/zdevelopers/uhcreloaded/dashboard/) to translate the plugin.

1. Go to [the Transifex project page of this plugin](https://www.transifex.com/zdevelopers/uhcreloaded/dashboard/). Create an account if you don't have one.
2. Click « **Join team** » on the top left of the page.
3. Select your langage in the list.
    - If your language is not listed, request it by clicking on [`... » Request language`](http://raw.carrade.eu/s/1453385835.png).
4. Translate.

When translated, we update the translations in the plugin as soon as possible.

You can also open a Pull-Request with a new or updated `po` file, if you want.


### Inside the translations

While translating, you will see some special keys, like `{0}` or `{blue}`.  
These values are replaced by the plugin; see below.

 - **`{0}`, `{1}`, etc.**: these values will be replaced with specific values by the plugin, depending on the context.  
   Example: in this string:
   
   ```yml
   unfrozen: "{cst}You where unfrozen by {0}."
   ```
   
   you can guess that `{0}` will be replaced by the name of the player who unfrozen the recipient of this message.
 
 - **`{blue}`, `{white}`, etc.**: these values will be replaced with Minecraft color codes. Available colors/formatting codes:
   
    Color | Color | Color | Color | Color 
   -------|-------|-------|-------|-------
   `{black}` | `{darkblue}` | `{darkgreen}` | `{darkaqua}` | `{darkred}`
   `{darkpurple}` | `{gold}` | `{gray}` | `{darkgray}` | `{blue}`
   `{yellow}` | `{green}` | `{aqua}` | `{red}` | `{lightpurple}`
    `{bold}` | `{strikethrough}` | `{underline}` | `{italic}` | `{obfuscated}`
      | `{white}` |   | `{reset}` | 

 - **`{ce}`, `{cc}`, etc.**: these values will be replaced by standardized colors for specific actions.
   
   Code | Signification | Color
   -----|---------------|------
   `{ce}` | Error | Red
   `{ci}` | Information | White
   `{cs}` | Success | Green
   `{cst}` | Status | Dark gray
   `{cc}` | Command | Gold


## Metrics

This plugin uses metrics, [which can be found here](http://mcstats.org/plugin/UHPlugin).

To opt out of metrics you can edit the `config.yml` file in the `PluginMetrics` folder inside your plugins folder (to opt-out for all plugins), or set the `metrics` option to `false` in the UltraHardcoreReloaded configuration file (to opt-out only for this plugin).

## Thanks

 - This work is a fork of [the KTP plugin](https://github.com/Azenet/KTP) made by [@Azenet](https://github.com/Azenet).
   *The original project was published under GPLv3, but without any piece of code in common between this original and the current project, the license was changed to CeCILL-B.*
 - Special thanks to [@jonyroda97](https://github.com/jonyroda97) and [@Amauryroco](https://github.com/Amauryroco), for many interesting suggestions.


### Translators

 - **English** *(original)*
    - [Amaury Carrade](https://github.com/AmauryCarrade)
 - **French**
    - [Amaury Carrade](https://github.com/AmauryCarrade)
 - **Portuguese (Portugal)**
    - [João Roda](https://github.com/jonyroda97)
 - **Portuguese (Brazil)**
    - [eraizel](https://github.com/eraizel)
 - **Czech**
    - [Nojp (Amunak)](https://github.com/Amunak)
 - **Simplified Chinese**
    - [h404bi](https://github.com/h404bi)


## License

This software is published under the **CeCILL-B** license. See the `LICENSE` file for details.