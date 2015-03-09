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
package me.azenet.UHPlugin.commands.commands.uh.spawns;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;


@Command(name = "add")
public class UHSpawnsAddCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHSpawnsAddCommand(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();
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

		// World?
		World world;
		if(sender instanceof Player) {
			world = ((Player) sender).getWorld();
		}
		else if(sender instanceof BlockCommandSender) {
			world = ((BlockCommandSender) sender).getBlock().getWorld();
		}
		else {
			world = p.getServer().getWorlds().get(0);
		}

		if(args.length == 0) { // /uh spawns add
			if(!(sender instanceof Player)) {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
			}
			else {
				Player pl = (Player) sender; // Just a way to avoid casts everywhere.
				try {
					p.getSpawnsManager().addSpawnPoint(pl.getLocation());
					sender.sendMessage(i.t("spawns.add.added", world.getName(), String.valueOf(pl.getLocation().getBlockX()), String.valueOf(pl.getLocation().getBlockZ())));
				} catch(IllegalArgumentException e) {
					sender.sendMessage(i.t("spawns.add.outOfLimits"));
				} catch(RuntimeException e) {
					sender.sendMessage(i.t("spawns.add.noSafeSpot"));
				}
			}
		}
		else if(args.length == 1) { // /uh spawns add <x>: Two coordinates needed!
			sender.sendMessage(i.t("spawns.error2Coords"));
		}
		else { // /uh spawns add <x> <z>
			try {
				p.getSpawnsManager().addSpawnPoint(world, Double.parseDouble(args[0]), Double.parseDouble(args[1]));
				sender.sendMessage(i.t("spawns.add.added", world.getName(), args[0], args[1]));
			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("spawns.NaN"));
			} catch(IllegalArgumentException e) {
				sender.sendMessage(i.t("spawns.add.outOfLimits"));
			} catch(RuntimeException e) {
				sender.sendMessage(i.t("spawns.add.noSafeSpot"));
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
		return Arrays.asList(i.t("cmd.spawnsHelpAdd"));
	}
}
