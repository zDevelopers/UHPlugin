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
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.spawns.Generator;
import eu.carrade.amaury.UHCReloaded.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.UHCReloaded.spawns.exceptions.UnknownGeneratorException;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "generate")
public class UHSpawnsGenerateCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHSpawnsGenerateCommand(UHCReloaded plugin)
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

        if (args.length == 0)
        { // Help
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
        }

        String generationMethod = args[0];

        // Default values
        Integer size = p.getBorderManager().getCurrentBorderDiameter() - 25; // Avoid spawn points being too close to the border
        Integer distanceMinBetweenTwoPoints = 250;
        World world = p.getServer().getWorlds().get(0);
        Double xCenter = world.getSpawnLocation().getX();
        Double zCenter = world.getSpawnLocation().getZ();

        Integer spawnsCount = 0;
        for (UHTeam team : p.getTeamManager().getTeams())
        {
            if (!team.isEmpty()) spawnsCount++;
        }

        if (args.length < 7)
        {
            if (sender instanceof Player)
            {
                world = ((Player) sender).getWorld();
            }
            else if (sender instanceof BlockCommandSender)
            {
                world = ((BlockCommandSender) sender).getBlock().getWorld();
            }

            xCenter = world.getSpawnLocation().getX();
            zCenter = world.getSpawnLocation().getZ();
        }

        // What if the game is in solo, or some players are out of all team?
        // Only if the spawn count is not provided of course. Else, we don't care, this count
        // will be overwritten.
        if (args.length < 5)
        {
            if (spawnsCount == 0)
            { // Solo mode?
                sender.sendMessage(I.t("{ci}No team found: assuming the game is a solo game."));
                spawnsCount = p.getServer().getOnlinePlayers().size() - p.getGameManager().getStartupSpectators().size();
            }
            else
            {
                // Trying to find players without team
                int playersWithoutTeam = 0;
                for (Player player : p.getServer().getOnlinePlayers())
                {
                    if (p.getTeamManager().getTeamForPlayer(player) == null)
                    {
                        playersWithoutTeam++;
                    }
                }

                if (playersWithoutTeam != 0)
                {
                    sender.sendMessage(I.t("{ci}Some players are not in a team; their number was added to the spawn count."));
                    spawnsCount += playersWithoutTeam;
                }
            }
        }

        try
        {
            if (args.length >= 2)
            { // size included
                size = Integer.valueOf(args[1]);

                if (args.length >= 3)
                { // distance minimal included
                    distanceMinBetweenTwoPoints = Integer.valueOf(args[2]);

                    if (args.length >= 4)
                    { // spawn count included
                        spawnsCount = Integer.valueOf(args[3]);

                        if (args.length >= 5)
                        { // xCenter included
                            xCenter = Double.parseDouble(args[4]);

                            if (args.length >= 6)
                            { // zCenter included
                                zCenter = Double.parseDouble(args[5]);

                                if (args.length >= 7)
                                { // world included
                                    World inputWorld = p.getServer().getWorld(args[6]);

                                    if (inputWorld != null)
                                    {
                                        world = inputWorld;
                                    }
                                    else
                                    {
                                        sender.sendMessage(I.t("{ce}The world {0} doesn't exists.", args[6]));
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            sender.sendMessage(I.t("{ce}This is not a number!"));
            return;
        }


        if (spawnsCount <= 0)
        {
            sender.sendMessage(I.t("{ci}You asked for a void generation. Thus, the generation is empty."));
            return;
        }


        try
        {
            p.getSpawnsManager().generateSpawnPoints(generationMethod, world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);

        }
        catch (UnknownGeneratorException e)
        {
            sender.sendMessage(I.t("{ce}The generation method “{0}” is not (yet?) supported.", generationMethod));
            return;

        }
        catch (CannotGenerateSpawnPointsException e)
        {
            sender.sendMessage(I.t("{ce}You asked for the impossible: there are too many spawn points on a too small surface. Decrease the spawn count or the minimal distance between two points."));
            return;
        }

        sender.sendMessage(I.t("{cs}Successfully generated the asked spawn points."));
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
        // Generation methods - /uh spawns generate <?>
        if (args.length == 1)
        {
            ArrayList<String> suggested = new ArrayList<String>();

            for (Generator generator : Generator.values())
            {
                suggested.add(generator.name().toLowerCase());
            }

            return CommandUtils.getAutocompleteSuggestions(args[0], suggested);
        }

        // Worlds - /uh spawns generate - - - - - - <?>
        else if (args.length == 7)
        {
            ArrayList<String> suggested = new ArrayList<String>();
            for (World world : p.getServer().getWorlds())
            {
                suggested.add(world.getName());
            }

            return CommandUtils.getAutocompleteSuggestions(args[6], suggested);
        }

        else return null;
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
        return Arrays.asList(
                I.t("{aqua}Command"),
                I.t("{cc}/uh spawns generate <circular|grid|random> [size] [distanceMin] [count] [xCenter] [zCenter] [world]"),
                I.t("{aqua}Shapes"),
                I.t(" - {cc}random{ci}: generates random spawn points on the map, with a minimal distance between them."),
                I.t(" - {cc}grid{ci}: generates the spawn points on concentric squares, with a constant distance between two generated points."),
                I.t(" - {cc}circular{ci}: generates the spawn points on concentric circles, with a minimal distance between two generated points. In each circle, the angle (and the distance) between two spawn points is constant."),
                I.t("{aqua}Arguments"),
                I.t(" - {cc}size{ci}: the size of the region where the spawn points will be generated. Squared or circular, following the shape of the map. Default: map' size."),
                I.t(" - {cc}distanceMin{ci}: the minimal distance between two spawn points. Default: 250 blocks."),
                I.t(" - {cc}count{ci}: the number of spawn points to generate. Default: the number of players or teams."),
                I.t(" - {cc}xCenter{ci}, {cc}zCenter{ci}: the center of the region where the points are generated. Default: world' spawn point."),
                I.t(" - {cc}world{ci}: the world where the spawn points will be generated.")
        );
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh spawns generate {ci}: automagically generates spawn points. See /uh spawns generate for details."));
    }
}
