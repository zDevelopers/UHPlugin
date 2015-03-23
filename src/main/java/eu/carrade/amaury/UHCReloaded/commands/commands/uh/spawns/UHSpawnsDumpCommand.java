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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.spawns;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.*;

@Command(name = "dump")
public class UHSpawnsDumpCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHSpawnsDumpCommand(UHCReloaded plugin) {
		p = plugin;
		i = plugin.getI18n();
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

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.spawnsHelpDump"));
	}
}
