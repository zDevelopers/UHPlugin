/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.UHCReloaded.modules.core.spawns.commands;

import eu.carrade.amaury.UHCReloaded.modules.core.border.BorderModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.SpawnsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.exceptions.UnknownGeneratorException;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CommandInfo(
        name = "spawns",
        usageParameters = "[generate | add [<x> <z>] | remove [<x> <z>] | dump | help]",
        aliases = {"spawn", "s"}
)
public class SpawnsCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        if (args.length == 0)
        {
            list();
            return;
        }

        switch (args[0].toLowerCase())
        {
            case "list":
                list();
                return;

            case "dump":
                dump();
                return;

            case "add":
                add();
                return;

            case "remove":
                remove();
                return;

            case "generate":
                generate();
                return;

            case "reset":
                reset();

            default:
                help();
        }
    }

    protected void help()
    {
        info(I.t("{blue}{bold}Command help for {cc}{bold}/uh spawns"));
        info(I.t("{cc}/uh spawns list {ci}: lists the registered spawn points."));
        info(I.t("{cc}/uh spawns dump {ci}: displays the registered spawn points in an exportable format. {gray}Use this to plot the spawn points, as example."));
        info(I.t("{cc}/uh spawns add {ci}: adds a spawn point for a team or a player, at the current location of the sender or at the provided coordinates."));
        info(I.t("{cc}/uh spawns remove [<x> <z>] {ci}: removes the spawn points at the specified coordinates, or at the current location if the sender without coordinates."));
        info(I.t("{cc}/uh spawns generate {ci}: automagically generates spawn points. See /uh spawns generate for details."));
        info(I.t("{cc}/uh spawns reset {ci}: removes all registered spawn points."));
    }

    protected void list()
    {
        final List<Location> spawnPoints = UR.module(SpawnsModule.class).getSpawnPoints();

        if (spawnPoints.size() == 0)
        {
            if (sender instanceof Player) info("");
            info(I.t("{ce}There isn't any registered spawn point."));

            if (args.length == 0)
            {
                info("");
                help();
            }
        }
        else
        {
            if (sender instanceof Player) info("");
            info(I.t("{ci}There are {0} registered spawn points.", String.valueOf(spawnPoints.size())));

            // We want one list per world
            final Map<World, List<Location>> spawnsInWorlds = new HashMap<>();
            spawnPoints.forEach(spawn -> {
                spawnsInWorlds.putIfAbsent(spawn.getWorld(), new LinkedList<>());
                spawnsInWorlds.get(spawn.getWorld()).add(spawn);
            });

            for (Map.Entry<World, List<Location>> spanwsInWorld : spawnsInWorlds.entrySet())
            {
                if (spanwsInWorld.getValue().size() == 0)
                {
                    continue;
                }

                info("");
                info(I.t("{lightpurple}World {0}", spanwsInWorld.getKey().getName()));


                // Displaying this number of spawn points per line
                final int spawnsPerLine = 5;

                for (int j = 0; j < Math.ceil((double) spanwsInWorld.getValue().size() / spawnsPerLine); j++)
                {
                    final StringBuilder line = new StringBuilder();

                    for (int k = 0; k < spawnsPerLine; k++)
                    {
                        if (spanwsInWorld.getValue().size() > j * spawnsPerLine + k)
                        {
                            line.append(
                                    getSpawnItem(
                                            spanwsInWorld.getValue().get(j * spawnsPerLine + k).getBlockX(),
                                            spanwsInWorld.getValue().get(j * spawnsPerLine + k).getBlockZ(),
                                            spanwsInWorld.getKey().getEnvironment()
                                    )
                            ).append("  ");
                        }
                    }

                    info(line.toString());
                }
            }
        }
    }

    protected void dump()
    {
        // We want one list per world
        final Map<World, List<Location>> spawnsInWorlds = new HashMap<>();

        UR.module(SpawnsModule.class)
                .getSpawnPoints()
                .forEach(spawn -> {
                    spawnsInWorlds.putIfAbsent(spawn.getWorld(), new LinkedList<>());
                    spawnsInWorlds.get(spawn.getWorld()).add(spawn);
                });

        StringBuilder dump = new StringBuilder();

        for (Map.Entry<World, List<Location>> spawnsInWorld : spawnsInWorlds.entrySet())
        {
            if (spawnsInWorld.getValue().size() == 0)
            {
                continue;
            }

            dump.append("\n* ").append(spawnsInWorld.getKey().getName()).append("\n");

            for (Location spawn : spawnsInWorld.getValue())
            {
                dump.append(spawn.getBlockX()).append(",").append(spawn.getBlockZ()).append("\n");
            }
        }

        sender.sendMessage(dump.toString());
    }

    protected void add() throws CommandException
    {
        // World?
        final World world;
        if (sender instanceof Player)
        {
            world = ((Player) sender).getWorld();
        }
        else if (sender instanceof BlockCommandSender)
        {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        else if (args.length >= 4)
        {
            world = Bukkit.getWorld(args[3]);

            if (world == null) throwInvalidArgument(I.t("There is no world named {0}.", args[3]));
        }
        else
        {
            world = UR.get().getWorld(Environment.NORMAL);
        }

        // /uh spawns add
        if (args.length == 1)
        {
            try
            {
                UR.module(SpawnsModule.class).addSpawnPoint(playerSender().getLocation());
                success(I.t("{cs}Spawn added in the world {0}: {1};{2}", world.getName(), String.valueOf(playerSender().getLocation().getBlockX()), String.valueOf(playerSender().getLocation().getBlockZ())));
            }
            catch (IllegalArgumentException e)
            {
                error(I.t("{ce}You cannot add a spawn point out of the borders."));
            }
            catch (RuntimeException e)
            {
                error(I.t("{ce}Unable to add this spawn point: no safe spot found in the Nether."));
            }
        }

        // /uh spawns add <x>: Two coordinates needed!
        else if (args.length == 2)
        {
            throwInvalidArgument(I.t("{ce}You need to specify two coordinates."));
        }

        // /uh spawns add <x> <z> [world]
        else
        {
            try
            {
                UR.module(SpawnsModule.class).addSpawnPoint(world, Double.parseDouble(args[1]), Double.parseDouble(args[2]));
                success(I.t("{cs}Spawn added in the world {0}: {1};{2}", world.getName(), args[1], args[2]));
            }
            catch (NumberFormatException e)
            {
                throwInvalidArgument(I.t("{ce}This is not a number!"));
            }
            catch (IllegalArgumentException e)
            {
                error(I.t("{ce}You cannot add a spawn point out of the borders."));
            }
            catch (RuntimeException e)
            {
                error(I.t("{ce}Unable to add this spawn point: no safe spot found in the Nether."));
            }
        }
    }

    protected void remove() throws CommandException
    {
        // /uh spawns remove
        if (args.length == 1)
        {
            UR.module(SpawnsModule.class).removeSpawnPoint(playerSender().getLocation(), false);
            success(I.t("{cs}The spawn point {1};{2} in the world {0} was removed.", playerSender().getWorld().getName(), playerSender().getLocation().getBlockX(), playerSender().getLocation().getBlockZ()));

        }

        // /uh spawns add <x>: Two coordinates needed!
        else if (args.length == 2)
        {
            throwInvalidArgument(I.t("{ce}You need to specify two coordinates."));
        }

        // /uh spawns remove <x> <z>
        else
        {
            try
            {
                final World world;
                if (sender instanceof Player)
                {
                    world = ((Player) sender).getWorld();
                }
                else if (args.length >= 4)
                {
                    world = Bukkit.getWorld(args[3]);
                    if (world == null) throwInvalidArgument(I.t("There is no world named {0}.", args[3]));
                }
                else
                {
                    world = UR.get().getWorld(Environment.NORMAL);
                }

                UR.module(SpawnsModule.class).removeSpawnPoint(
                        new Location(
                                world, Double.parseDouble(args[1]), 0, Double.parseDouble(args[2])
                        ),
                        true
                );
                success(I.t("{cs}The spawn point {1};{2} in the world {0} was removed.", world, args[1], args[2]));
            }
            catch (NumberFormatException e)
            {
                throwInvalidArgument(I.t("{ce}This is not a number!"));
            }
        }
    }

    protected void reset()
    {
        UR.module(SpawnsModule.class).reset();
        success(I.t("{cs}All the spawn points were removed."));
    }

    protected void generate() throws CommandException
    {
        // /uh spawns generate
        if (args.length == 1)
        {
            if (sender instanceof Player) info("");

            info(I.t("{aqua}Command"));
            info(I.t("{cc}/uh spawns generate <circular|grid|random> [size] [distanceMin] [count] [xCenter] [zCenter] [world]"));
            info("");
            info(I.t("{aqua}Shapes"));
            info(I.t(" - {cc}random{ci}: generates random spawn points on the map, with a minimal distance between them."));
            info(I.t(" - {cc}grid{ci}: generates the spawn points on concentric squares, with a constant distance between two generated points."));
            info(I.t(" - {cc}circular{ci}: generates the spawn points on concentric circles, with a minimal distance between two generated points. In each circle, the angle (and the distance) between two spawn points is constant."));
            info("");
            info(I.t("{aqua} Arguments "));
            info(I.t(" - {cc}size{ci}: the size of the region where the spawn points will be generated. Squared or circular, following the shape of the map. Default: map' size."));
            info(I.t(" - {cc}distanceMin{ci}: the minimal distance between two spawn points. Default: 250 blocks."));
            info(I.t(" - {cc}count{ci}: the number of spawn points to generate. Default: the number of players or teams."));
            info(I.t(" - {cc}xCenter{ci}, {cc}zCenter{ci}: the center of the region where the points are generated. Default: world' spawn point."));
            info(I.t(" - {cc}world{ci}: the world where the spawn points will be generated."));

            return;
        }

        final String generationMethod = args[1];

        // Default values
        int size = UR.module(BorderModule.class).getCurrentBorderDiameter() - 25; // Avoid spawn points being too close to the border
        int distanceMinBetweenTwoPoints = 250;
        World world = UR.get().getWorld(Environment.NORMAL);
        double xCenter = world.getSpawnLocation().getX();
        double zCenter = world.getSpawnLocation().getZ();

        int spawnsCount = 0;
        for (final ZTeam team : ZTeams.get().getTeams())
        {
            if (!team.isEmpty()) spawnsCount++;
        }

        if (args.length < 8)
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
        if (args.length < 6)
        {
            // Solo mode?
            if (spawnsCount == 0)
            {
                spawnsCount = Bukkit.getServer().getOnlinePlayers().size() - 0/* TODO UR.module(GameModule.class).getStartupSpectators().size()*/;
                info(I.t("{ci}No team found: assuming the game is a solo game."));
            }
            else
            {
                // Trying to find players without team
                int playersWithoutTeam = 0;
                for (Player player : Bukkit.getServer().getOnlinePlayers())
                {
                    if (ZTeams.get().getTeamForPlayer(player) == null)
                    {
                        playersWithoutTeam++;
                    }
                }

                if (playersWithoutTeam != 0)
                {
                    spawnsCount += playersWithoutTeam;
                    info(I.t("{ci}Some players are not in a team; their number was added to the spawn count."));
                }
            }
        }

        try
        {
            // size included
            if (args.length >= 3)
            {
                size = Integer.parseInt(args[2]);

                // distance minimal included
                if (args.length >= 4)
                {
                    distanceMinBetweenTwoPoints = Integer.parseInt(args[3]);

                    // spawn count included
                    if (args.length >= 5)
                    {
                        spawnsCount = Integer.parseInt(args[4]);

                        // xCenter included
                        if (args.length >= 6)
                        {
                            xCenter = Double.parseDouble(args[5]);

                            // zCenter included
                            if (args.length >= 7)
                            {
                                zCenter = Double.parseDouble(args[6]);

                                // world included
                                if (args.length >= 8)
                                {
                                    final World inputWorld = Bukkit.getServer().getWorld(args[7]);

                                    if (inputWorld != null)
                                    {
                                        world = inputWorld;
                                    }
                                    else
                                    {
                                        error(I.t("{ce}The world {0} doesn't exists.", args[7]));
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
            info(I.t("{ce}This is not a number!"));
            info(I.t("{cc}/uh spawns generate <circular|grid|random> [size] [distanceMin] [count] [xCenter] [zCenter] [world]"));
            return;
        }


        if (spawnsCount <= 0)
        {
            sender.sendMessage(I.t("{ci}You asked for a void generation. Thus, the generation is empty."));
            return;
        }


        try
        {
            UR.module(SpawnsModule.class)
                    .generateSpawnPoints(generationMethod, world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);
        }
        catch (UnknownGeneratorException e)
        {
            throwInvalidArgument(I.t("{ce}The generation method “{0}” is not (yet?) supported.", generationMethod));

        }
        catch (CannotGenerateSpawnPointsException e)
        {
            error(I.t("{ce}You asked for the impossible: there are too many spawn points on a too small surface. Decrease the spawn count or the minimal distance between two points."));
        }


        success(I.t("{cs}Successfully generated the asked spawn points."));
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        if (args.length == 1)
        {
            return getMatchingSubset(args[0], "add", "remove", "generate", "list", "dump", "reset");
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("generate"))
        {
            return getMatchingSubset(args[1], "circular", "grid", "random");
        }

        else if (args.length == 4 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")))
        {
            return getMatchingSubset(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()), args[3]);
        }

        else if (args.length == 8 && args[0].equalsIgnoreCase("generate"))
        {
            return getMatchingSubset(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()), args[7]);
        }

        else return null;
    }


    private String getSpawnItem(int x, int z, Environment environment)
    {
        switch (environment)
        {
            case NORMAL:
                /// A spawn point in the /uh spawns list command (in the overworld)
                return I.t("{green}{0}{darkgreen};{green}{1}", x, z);

            case NETHER:
                /// A spawn point in the /uh spawns list command (in the Nether)
                return I.t("{red}{0}{darkred};{red}{1}", x, z);

            case THE_END:
                /// A spawn point in the /uh spawns list command (in the End)
                return I.t("{yellow}{0}{gold};{yellow}{1}", x, z);

            default:
                /// A spawn point in the /uh spawns list command (in a custom world)
                return I.t("{gray}{0}{darkgray};{gray}{1}", x, z);
        }
    }
}
