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
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;


@Command(name = "leave")
public class UHTeamLeaveCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHTeamLeaveCommand(UHPlugin plugin) {
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

		OfflinePlayer target;

		if(args.length == 0) {
			if(sender instanceof Player) {
				target = (OfflinePlayer) sender;
			}
			else {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
			}
		}
		else { // /uh team leave <player>
			target = p.getServer().getOfflinePlayer(args[0]);
		}


		if(target == null) {
			sender.sendMessage(i.t("team.removeplayer.disconnected", args[0])); // args.length >= 1 here.
		}

		else {

			// Permissions check
			if (sender.hasPermission("uh.team.leave")
					|| (target.equals(sender) && sender.hasPermission("uh.player.leave.self"))
					|| (!target.equals(sender) && sender.hasPermission("uh.player.leave.others"))) {


				p.getTeamManager().removePlayerFromTeam(target);

				if(!target.equals(sender)) {
					sender.sendMessage(i.t("team.removeplayer.success", target.getName()));
				}

			} else {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
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
		return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.teamHelpLeave"));
	}
}
