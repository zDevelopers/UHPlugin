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
import me.azenet.UHPlugin.commands.commands.categories.Category;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This command freezes the players.
 *
 * Usage: /uh freeze <on [player]|off [player]|all|none>
 *  - on [player]: freezes the given player, or the sender if no player was provided.
 *  - off [player]: unfreezes the given player (or the sender, same condition).
 *  - all: freezes all the alive players, the mobs and the timer.
 *  - none: unfreezes all the alive players (even if there where frozen before using
 *          /uh freeze all), the mobs and the timer.
 */
@Command(name = "freeze")
public class UHFreezeCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHFreezeCommand(UHPlugin plugin) {
		p = plugin;
		i = plugin.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
		}

		String subcommand = args[0];

		if(subcommand.equalsIgnoreCase("on") || subcommand.equalsIgnoreCase("off")) {

			boolean on = subcommand.equalsIgnoreCase("on");

			if(args.length == 1) { // /uh freeze on: freezes the sender
				if(sender instanceof Player) {
					p.getFreezer().setPlayerFreezeState((Player) sender, on);

					if(on) {
						sender.sendMessage(i.t("freeze.frozen", sender.getName()));
					}
					else {
						sender.sendMessage(i.t("freeze.unfrozen", sender.getName()));
					}
				}
				else {
					throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
				}
			}
			else if(args.length == 2) { // /uh freeze on <player>: freezes <player>.
				Player player = p.getServer().getPlayer(args[1]);
				if(player == null) {
					sender.sendMessage(i.t("freeze.offline", args[1]));
				}
				else {
					p.getFreezer().setPlayerFreezeState(player, on);
					if(on) {
						player.sendMessage(i.t("freeze.frozen", sender.getName()));
						sender.sendMessage(i.t("freeze.playerFrozen", player.getName()));
					}
					else {
						player.sendMessage(i.t("freeze.unfrozen", sender.getName()));
						sender.sendMessage(i.t("freeze.playerUnfrozen", player.getName()));
					}
				}
			}
		}

		else if(subcommand.equalsIgnoreCase("all") || subcommand.equalsIgnoreCase("none")) {

			boolean on = subcommand.equalsIgnoreCase("all");

			p.getFreezer().setGlobalFreezeState(on);

			if(on) {
				p.getServer().broadcastMessage(i.t("freeze.broadcast.globalFreeze"));
			}
			else {
				p.getServer().broadcastMessage(i.t("freeze.broadcast.globalUnfreeze"));
			}

		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			return CommandUtils.getAutocompleteSuggestions(
					args[0], Arrays.asList("on", "off", "all", "none")
			);
		}

		else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("off")) {
				List<String> suggestions = new ArrayList<>();

				for (Player player : p.getFreezer().getFrozenPlayers()) {
					suggestions.add(player.getName());
				}

				return CommandUtils.getAutocompleteSuggestions(args[1], suggestions);
			}

			else return null;
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(
				i.t("cmd.freezeHelpTitle"),
				i.t("cmd.freezeHelpOn"),
				i.t("cmd.freezeHelpOff"),
				i.t("cmd.freezeHelpAll"),
				i.t("cmd.freezeHelpNone")
		);
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpFreeze"));
	}

	@Override
	public String getCategory() {
		return Category.MISC.getTitle();
	}
}
