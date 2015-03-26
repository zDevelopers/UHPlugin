/**
 * Plugin UltraHardcore Reloaded (UHPlugin)
 * Copyright (C) 2013 azenet
 * Copyright (C) 2014-2015 Amaury Carrade
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This command teleports a team or the spectators to a given location.
 *
 * Usage: /uh tp team <x> <y> <z> <team name ...>
 * Usage: /uh tp team <target> <team name...>
 * Usage: /uh tp spectators <x> <y> <z>
 * Usage: /uh tp spectators <target>
 */
@Command(name = "tp")
public class UHTPCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHTPCommand(UHCReloaded plugin) {
		this.p = plugin;
		this.i = plugin.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0) { // No action provided: doc
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
		}

		else {
			String subcommand = args[0];

			World targetWorld;
			if(sender instanceof Player) {
				targetWorld = ((Player) sender).getWorld();
			}
			else if(sender instanceof BlockCommandSender) {
				targetWorld = ((BlockCommandSender) sender).getBlock().getWorld();
			}
			else {
				targetWorld = p.getServer().getWorlds().get(0);
			}

			if(subcommand.equalsIgnoreCase("team")) {
				boolean mayBeNaNError = false;

				if(args.length >= 6) { // possibly /uh tp team <x> <y> <z> <team ...>
					String teamName = UHUtils.getStringFromCommandArguments(args, 4);
					UHTeam team = p.getTeamManager().getTeam(teamName);

					if(team != null) { // ok, the team exists.
						try {
							double x = Integer.parseInt(args[1]) + 0.5;
							double y = Integer.parseInt(args[2]) + 0.5;
							double z = Integer.parseInt(args[3]) + 0.5;

							team.teleportTo(new Location(targetWorld, x, y, z));

							return;
						} catch(NumberFormatException e) {
							// It can be either another name for the team, starting by "<y> <z> the name"
							// or a formatting error.
							// The possibility of an error is saved.
							mayBeNaNError = true;
						}
					}
				}
				if(args.length >= 3) { // /uh tp team <target> <team ...>
					String teamName = UHUtils.getStringFromCommandArguments(args, 2);
					UHTeam team = p.getTeamManager().getTeam(teamName);

					if(team == null) {
						if(mayBeNaNError) {
							sender.sendMessage(i.t("tp.NaN"));
						}
						else {
							sender.sendMessage(i.t("tp.teamDoesNotExists"));
						}
					}
					else {
						Player target = p.getServer().getPlayer(args[1]);

						if(target == null) {
							sender.sendMessage(i.t("tp.targetOffline", args[1]));
						}
						else {
							team.teleportTo(target.getLocation());
						}
					}
				}
			}

			else if(subcommand.equalsIgnoreCase("spectators")) {
				if(args.length == 4) { // /uh tp spectators <x> <y> <z>
					try {
						double x = Integer.parseInt(args[1]) + 0.5;
						double y = Integer.parseInt(args[2]) + 0.5;
						double z = Integer.parseInt(args[3]) + 0.5;

						for(Player player : p.getServer().getOnlinePlayers()) {
							if(p.getGameManager().isPlayerDead(player)) {
								player.teleport(new Location(targetWorld, x, y, z), PlayerTeleportEvent.TeleportCause.PLUGIN);
							}
						}
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("tp.NaN"));
					}
				}
				else if(args.length == 2) { // /uh tp spectators <target>
					Player target = p.getServer().getPlayer(args[1]);

					if(target == null) {
						sender.sendMessage(i.t("tp.targetOffline", args[1]));
					}
					else {
						for(Player player : p.getServer().getOnlinePlayers()) {
							if(p.getGameManager().isPlayerDead(player)) {
								player.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if(args.length == 1) {
			return CommandUtils.getAutocompleteSuggestions(
					args[0],
					Arrays.asList("team", "spectators")
			);
		}

		else if(args.length > 1 && args[0].equalsIgnoreCase("team")) {
			ArrayList<String> teamNames = new ArrayList<>();
			for(UHTeam team : this.p.getTeamManager().getTeams()) {
				teamNames.add(team.getName());
			}

			// /uh tp team <x> <y> <z> <?>: autocompletion for team names – multiple words autocompletion
			if(args.length >= 5) {
				return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 4), teamNames, args.length - 5);
			}

			// /uh tp team <target> <?>: autocompletion for team names – multiple words autocompletion
			if(args.length >= 3) {
				try {
					return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 2), teamNames, args.length - 3);
				} catch(IllegalArgumentException ignored) {
					// Temp workaround for an unknown bug.
				}
			}
		}

		return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(
				i.t("cmd.tpHelpTitle"),
				i.t("cmd.tpHelpTeam"),
				i.t("cmd.tpHelpSpectators")
		);
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpTP"));
	}

	@Override
	public String getCategory() {
		return Category.MISC.getTitle();
	}
}
