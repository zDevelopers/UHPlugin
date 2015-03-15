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
 * This command feeds a player.
 * <p>
 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
 */
@Command(name = "healall")
public class UHHealAllCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHHealAllCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		double diffHealth = 0D;
		double health = 0D;
		boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode

		if(args.length == 0) { // /uh healall : full life for all players.
			diffHealth = 20D;
		}
		else { // /uh heal <player> <hearts>
			try {
				if(args[0].startsWith("+")) {
					diffHealth = Double.parseDouble(args[0].substring(1));
					add = true;
				}
				else if(args[0].startsWith("-")) {
					diffHealth = -1 * Double.parseDouble(args[0].substring(1));
					add = true;
				}
				else {
					diffHealth = Double.parseDouble(args[0]);
				}
			}
			catch(NumberFormatException e) {
				sender.sendMessage(i.t("heal.errorNaN"));
				return;
			}
		}

		if((!add && diffHealth <= 0) || diffHealth <= -20) {
			sender.sendMessage(i.t("heal.allErrorNoKill"));
			return;
		}

		for(final Player player : p.getServer().getOnlinePlayers()) {
			health = !add ? diffHealth : player.getHealth() + diffHealth;

			if(health <= 0D) {
				sender.sendMessage(i.t("heal.errorHealthNotUpdatedNoKill", player.getName()));
				continue;
			}
			else if(health > 20D) {
				health = 20D;
			}

			player.setHealth(health);
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
		return Arrays.asList(i.t("cmd.helpHealall"));
	}

	@Override
	public String getCategory() {
		return Category.BUGS.getTitle();
	}
}
