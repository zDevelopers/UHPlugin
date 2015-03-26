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

import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;


/**
 * This command starts the game.
 *
 * Usage: /uh start [slow [go]]
 */
@Command(name = "start")
public class UHStartCommand extends AbstractCommand {

	private UHCReloaded p;
	private I18n i;

	public UHStartCommand(UHCReloaded plugin) {
		p = plugin;
		i = p.getI18n();
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
		if(args.length == 0) { // /uh start (standard mode)
			try {
				p.getGameManager().start(sender, false);
			} catch(IllegalStateException e) {
				sender.sendMessage(i.t("start.already"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		else if(args.length == 1 && args[0].equalsIgnoreCase("slow")) { // /uh start slow
			try {
				p.getGameManager().start(sender, true);
			} catch(IllegalStateException e) {
				sender.sendMessage(i.t("start.already"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		else if(args.length == 2 && args[0].equalsIgnoreCase("slow") && args[1].equalsIgnoreCase("go")) { // /uh start slow go
			p.getGameManager().finalizeStartSlow(sender);
		}

		else {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE);
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

		if(args.length == 1) { // /uh start <?>
			return CommandUtils.getAutocompleteSuggestions(args[0], Arrays.asList("slow"));
		}

		else if(args.length == 2 && args[0].equalsIgnoreCase("slow")) { // /uh start slow <?>
			return CommandUtils.getAutocompleteSuggestions(args[1], Arrays.asList("go"));
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpStart"), i.t("cmd.helpStartSlow"));
	}

	@Override
	public String getCategory() {
		return Category.GAME.getTitle();
	}
}
