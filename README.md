# Plugin KTP

C'est très la source.

[![Build Status](https://travis-ci.org/AmauryCarrade/KTP.svg?branch=master)](https://travis-ci.org/AmauryCarrade/KTP)

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

### Permissions

 - `uh.build`:  allows an user to build before the beginning of the game. Default: operator.
 - `uh.<command>`: allows an user to use the command `/uh <command>`. Default: operator.

If you don't want to bother with permissions: the operators can do anything; the non-ops, nothing (except playing).


## Licence

GPLv3. Voir le fichier LICENSE pour détails.
