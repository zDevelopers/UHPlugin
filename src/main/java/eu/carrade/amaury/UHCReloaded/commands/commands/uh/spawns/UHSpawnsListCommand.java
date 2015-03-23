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


@Command(name = "list")
public class UHSpawnsListCommand extends AbstractCommand
{

	UHCReloaded p;
	I18n i;

	public UHSpawnsListCommand(UHCReloaded plugin) {
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
		List<Location> spawnPoints = p.getSpawnsManager().getSpawnPoints();

		if(spawnPoints.size() == 0) {
			sender.sendMessage(i.t("spawns.list.nothing"));
		}
		else {
			sender.sendMessage(i.t("spawns.list.count", String.valueOf(spawnPoints.size())));

			// We want one list per world
			Map<World,List<Location>> spanwsInWorlds = new HashMap<World,List<Location>>();
			for(World world : p.getServer().getWorlds()) {
				spanwsInWorlds.put(world, new LinkedList<Location>());
			}

			for(Location spawn : spawnPoints) {
				spanwsInWorlds.get(spawn.getWorld()).add(spawn);
			}

			for(Map.Entry<World, List<Location>> spanwsInWorld : spanwsInWorlds.entrySet()) {
				if(spanwsInWorld.getValue().size() == 0) {
					continue;
				}

				sender.sendMessage(i.t("spawns.list.world", spanwsInWorld.getKey().getName()));

				String itemDisplay;
				if(spanwsInWorld.getKey().getEnvironment() == World.Environment.NORMAL) {
					itemDisplay = "spawns.list.item.overworld";
				} else if(spanwsInWorld.getKey().getEnvironment() == World.Environment.NETHER) {
					itemDisplay = "spawns.list.item.nether";
				} else if(spanwsInWorld.getKey().getEnvironment() == World.Environment.THE_END) {
					itemDisplay = "spawns.list.item.end";
				} else {
					itemDisplay = "spawns.list.item.other";
				}

				// Displaying this number of spawn points per line
				final Integer spawnsPerLine = 5;

				for(int j = 0; j < Math.ceil((double) spanwsInWorld.getValue().size() / spawnsPerLine); j++) {
					String line = "";

					for(int k = 0; k < spawnsPerLine; k++) {
						if(spawnPoints.size() > j*spawnsPerLine + k) {
							line += i.t(itemDisplay, String.valueOf(spanwsInWorld.getValue().get(j*spawnsPerLine + k).getBlockX()), String.valueOf(spanwsInWorld.getValue().get(j*spawnsPerLine + k).getBlockZ())) + "  ";
						}
					}

					sender.sendMessage(line);
				}
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

	/**
	 * Returns the help of this command.
	 * <p/>
	 * <p>
	 * The first line should describe briefly the command, as this line is displayed as
	 * a line of the help of the parent command.
	 * </p>
	 * <p>
	 * The other lines will only be displayed if the {@link eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException}
	 * is caught by the command executor.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.spawnsHelpList"));
	}
}
