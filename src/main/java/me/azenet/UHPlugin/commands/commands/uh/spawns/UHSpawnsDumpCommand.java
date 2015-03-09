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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.*;

@Command(name = "dump")
public class UHSpawnsDumpCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHSpawnsDumpCommand(UHPlugin plugin) {
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
		// We want one list per world
		Map<World,List<Location>> spanwsInWorlds = new HashMap<World,List<Location>>();
		for(World world : p.getServer().getWorlds()) {
			spanwsInWorlds.put(world, new LinkedList<Location>());
		}

		for(Location spawn : p.getSpawnsManager().getSpawnPoints()) {
			spanwsInWorlds.get(spawn.getWorld()).add(spawn);
		}

		String dump = "";

		for(Map.Entry<World, List<Location>> spanwsInWorld : spanwsInWorlds.entrySet()) {
			if(spanwsInWorld.getValue().size() == 0) {
				continue;
			}

			dump += "\n* " + spanwsInWorld.getKey().getName() + "\n";

			for(Location spawn : spanwsInWorld.getValue()) {
				dump += spawn.getBlockX() + "," + spawn.getBlockZ() + "\n";
			}
		}

		sender.sendMessage(dump);
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

	/**
	 * Returns the help of this command.
	 * <p/>
	 * <p>
	 * The first line should describe briefly the command, as this line is displayed as
	 * a line of the help of the parent command.
	 * </p>
	 * <p>
	 * The other lines will only be displayed if the {@link me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException}
	 * is caught by the command executor.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(i.t("cmd.spawnsHelpDump"));
	}
}
