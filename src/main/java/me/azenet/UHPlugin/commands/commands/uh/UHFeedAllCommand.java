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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;


/**
 * This command feeds all player.
 *
 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
 */
@Command(name = "feedall")
public class UHFeedAllCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHFeedAllCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {

		int   foodLevel  = 20;
		float saturation = 20f;

		if(args.length > 0) { // /uh feedall <foodLevel>
			try {
				foodLevel = Integer.valueOf(args[0]);
			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("feed.errorNaN"));
				return;
			}

			if(args.length > 1) { // /uh feedall <foodLevel> <saturation>
				try {
					// The saturation value cannot be more than the food level.
					saturation = Math.min(foodLevel, Float.valueOf(args[1]));
				} catch(NumberFormatException e) {
					sender.sendMessage(i.t("feed.errorNaN"));
					return;
				}
			}
		}

		for(Player player : p.getServer().getOnlinePlayers()) {
			player.setFoodLevel(foodLevel);
			player.setSaturation(saturation);
		}
	}

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
		return Arrays.asList(i.t("cmd.helpFeedall"));
	}

	@Override
	public String getCategory() {
		return Category.BUGS.getTitle();
	}
}
