/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.spawns;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Command (name = "list")
public class UHSpawnsListCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHSpawnsListCommand(UHCReloaded plugin)
    {
        p = plugin;
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
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        List<Location> spawnPoints = p.getSpawnsManager().getSpawnPoints();

        if (spawnPoints.size() == 0)
        {
            sender.sendMessage(I.t("spawns.list.nothing"));
        }
        else
        {
            sender.sendMessage(I.t("spawns.list.count", String.valueOf(spawnPoints.size())));

            // We want one list per world
            Map<World, List<Location>> spanwsInWorlds = new HashMap<World, List<Location>>();
            for (World world : p.getServer().getWorlds())
            {
                spanwsInWorlds.put(world, new LinkedList<Location>());
            }

            for (Location spawn : spawnPoints)
            {
                spanwsInWorlds.get(spawn.getWorld()).add(spawn);
            }

            for (Map.Entry<World, List<Location>> spanwsInWorld : spanwsInWorlds.entrySet())
            {
                if (spanwsInWorld.getValue().size() == 0)
                {
                    continue;
                }

                sender.sendMessage(I.t("spawns.list.world", spanwsInWorld.getKey().getName()));

                String itemDisplay;
                if (spanwsInWorld.getKey().getEnvironment() == World.Environment.NORMAL)
                {
                    itemDisplay = "spawns.list.item.overworld";
                }
                else if (spanwsInWorld.getKey().getEnvironment() == World.Environment.NETHER)
                {
                    itemDisplay = "spawns.list.item.nether";
                }
                else if (spanwsInWorld.getKey().getEnvironment() == World.Environment.THE_END)
                {
                    itemDisplay = "spawns.list.item.end";
                }
                else
                {
                    itemDisplay = "spawns.list.item.other";
                }

                // Displaying this number of spawn points per line
                final Integer spawnsPerLine = 5;

                for (int j = 0; j < Math.ceil((double) spanwsInWorld.getValue().size() / spawnsPerLine); j++)
                {
                    String line = "";

                    for (int k = 0; k < spawnsPerLine; k++)
                    {
                        if (spawnPoints.size() > j * spawnsPerLine + k)
                        {
                            line += I.t(itemDisplay, String.valueOf(spanwsInWorld.getValue().get(j * spawnsPerLine + k).getBlockX()), String.valueOf(spanwsInWorld.getValue().get(j * spawnsPerLine + k).getBlockZ())) + "  ";
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
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
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
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("cmd.spawnsHelpList"));
    }
}
