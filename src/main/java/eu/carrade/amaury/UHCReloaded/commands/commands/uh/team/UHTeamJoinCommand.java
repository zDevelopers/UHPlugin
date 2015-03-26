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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.team;


import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for both /uh team join and /join commands.
 *
 * @see {@link eu.carrade.amaury.UHCReloaded.commands.commands.JoinCommand}.
 */
@Command(name = "join")
public class UHTeamJoinCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHTeamJoinCommand(UHCReloaded plugin) {
		p = plugin;
		i = plugin.getI18n();
	}

	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 *
	 * @throws eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {

		if(args.length == 0) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}

		UHTeam team;
		Player  target = null;
		Boolean self   = null;

		// /... join <team>?
		team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 0));
		if(team != null) {
			if(sender instanceof Player) {
				target = (Player) sender;
				self = true;
			}
			else {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
			}
		}
		else if(args.length >= 2) {
			// /... join <player> <team>?
			team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 1));
			if(team != null) {
				target = p.getServer().getPlayer(args[0]);
				self = false;
				if(target == null) {
					sender.sendMessage(i.t("team.addplayer.disconnected", args[0], team.getName()));
					return;
				}
			}
		}

		if(team == null) {
			sender.sendMessage(i.t("team.addplayer.doesNotExists"));
		}
		else {
			if(sender.hasPermission("uh.team.join")
					|| (self  && sender.hasPermission("uh.player.join.self"))
					|| (!self && sender.hasPermission("uh.player.join.others"))) {

				team.addPlayer(target);

				if(!sender.equals(target)) {
					sender.sendMessage(i.t("team.addplayer.success", target.getName(), team.getName()));
				}
			}
			else {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED, this);
			}
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
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.teamHelpJoin"));
	}
}
