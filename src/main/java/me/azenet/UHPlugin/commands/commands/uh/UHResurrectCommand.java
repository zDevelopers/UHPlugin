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
package me.azenet.UHPlugin.commands.commands.uh;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This command resurrects a player.
 *
 * Usage: /uh resurrect <player>
 */
@Command(name = "resurrect")
public class UHResurrectCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHResurrectCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length != 1) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}

		boolean success = p.getGameManager().resurrect(args[0]);

		Player player = p.getServer().getPlayer(args[0]);
		if(player == null || !player.isOnline()) {
			if(!success) { // Player does not exists or is nod dead.
				sender.sendMessage(i.t("resurrect.unknownOrDead"));
			}
			else { // Resurrected
				sender.sendMessage(i.t("resurrect.offlineOk", args[0]));
			}
		}
		else {
			if(!success) { // The player is not dead
				sender.sendMessage(i.t("resurrect.notDead", args[0]));
			}
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			List<String> suggestions = new ArrayList<>();

			// TODO can be optimized
			for(String playerName : p.getGameManager().getPlayers()) {
				OfflinePlayer player = p.getServer().getOfflinePlayer(playerName);
				if(player != null && p.getGameManager().isPlayerDead(player.getUniqueId())) {
					suggestions.add(playerName);
				}
			}

			return CommandUtils.getAutocompleteSuggestions(args[0], suggestions);
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpResurrect"));
	}
}
