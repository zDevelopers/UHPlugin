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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This command marks a player as dead, even if he is offline.
 *
 * If the player is online, this has the same effect as {@code /kill}.
 *
 * Usage: /uh kill &lt;player>
 */
@Command(name = "kill")
public class UHKillCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHKillCommand(UHCReloaded p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length < 1) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}

		OfflinePlayer player = p.getServer().getOfflinePlayer(args[0]);

		if(player == null) {
			sender.sendMessage(i.t("kill.neverPlayed"));
			return;
		}

		if(!p.getGameManager().isPlayerDead(player.getUniqueId())) {
			if(player.isOnline()) {
				((Player) player).setHealth(0);
			}
			else {
				p.getGameManager().addDead(player.getUniqueId());
			}

			sender.sendMessage(i.t("kill.killed", player.getName()));
		}
		else {
			sender.sendMessage(i.t("kill.notAlive", player.getName()));
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			List<String> suggestions = new ArrayList<>();

			for(OfflinePlayer player : p.getGameManager().getAlivePlayers()) {
				suggestions.add(player.getName());
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
		return Arrays.asList(i.t("cmd.helpKill"));
	}
}
