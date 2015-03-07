/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package me.azenet.UHPlugin.commands.commands.uh.team;


import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.commands.UHCommand;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.teams.UHTeam;
import me.azenet.UHPlugin.utils.CommandUtils;
import me.azenet.UHPlugin.utils.UHUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Command(name = "join")
public class UHTeamJoinCommand extends UHCommand {

	UHPlugin p;
	I18n i;

	public UHTeamJoinCommand(UHPlugin plugin) {
		p = plugin;
		i = plugin.getI18n();
	}

	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 *
	 * @throws me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length >= 2) { // /uh team join <player> <teamName>

			OfflinePlayer player = p.getServer().getOfflinePlayer(args[0]);
			String teamName = UHUtils.getStringFromCommandArguments(args, 1);

			if(player == null || !player.isOnline()) {
				sender.sendMessage(i.t("team.addplayer.disconnected", args[0], teamName));
			}
			else {
				try {
					p.getTeamManager().addPlayerToTeam(teamName, player);
				} catch(IllegalArgumentException e) {
					sender.sendMessage(i.t("team.addplayer.doesNotExists"));
					return;
				}
				catch(RuntimeException e) {
					sender.sendMessage(i.t("team.addplayer.full", teamName));
					return;
				}
				UHTeam team = p.getTeamManager().getTeam(teamName);
				sender.sendMessage(i.t("team.addplayer.success", args[0], team.getDisplayName()));
			}
		}
		else {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}
	}

	/**
	 * Tab-completes this command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if(args.length >= 2) {
			ArrayList<String> teamNames = new ArrayList<>();

			for (UHTeam team : this.p.getTeamManager().getTeams()) {
				teamNames.add(team.getName());
			}

			return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 1), teamNames, args.length - 2);
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(i.t("cmd.teamHelpJoin"));
	}
}
