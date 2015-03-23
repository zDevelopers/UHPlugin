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

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Command(name = "tpback")
public class UHTPBackCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHTPBackCommand(UHCReloaded p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length < 1) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}

		Player player = p.getServer().getPlayer(args[0]);
		if(player == null || !player.isOnline()) {
			sender.sendMessage(i.t("tpback.offline", args[0]));
			return;
		}
		else if(!p.getGameManager().hasDeathLocation(player)) {
			sender.sendMessage(i.t("tpback.noDeathLocation", args[0]));
			return;
		}


		Location deathLocation = p.getGameManager().getDeathLocation(player);

		if(args.length >= 2 && args[1].equalsIgnoreCase("force")) {
			UHUtils.safeTP(player, deathLocation, true);
			sender.sendMessage(i.t("tpback.teleported", args[0]));
			p.getGameManager().removeDeathLocation(player);
		}
		else if(UHUtils.safeTP(player, deathLocation)) {
			sender.sendMessage(i.t("tpback.teleported", args[0]));
			p.getGameManager().removeDeathLocation(player);
		}
		else {
			sender.sendMessage(i.t("tpback.notTeleportedNoSafeSpot", args[0]));
			sender.sendMessage(i.t("tpback.notTeleportedNoSafeSpotCmd", args[0]));
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if(args.length == 1) {
			List<String> suggestions = new ArrayList<>();

			for(Player player : p.getServer().getOnlinePlayers()) {
				if(p.getGameManager().hasDeathLocation(player)) {
					suggestions.add(player.getName());
				}
			}

			return CommandUtils.getAutocompleteSuggestions(args[0], suggestions);
		}

		else if(args.length == 2) {
			return CommandUtils.getAutocompleteSuggestions(args[1], Arrays.asList("force"));
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpTpback"));
	}
}
