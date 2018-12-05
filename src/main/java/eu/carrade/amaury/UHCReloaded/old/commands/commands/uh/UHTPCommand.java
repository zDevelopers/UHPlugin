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

package eu.carrade.amaury.UHCReloaded.old.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.old.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This command teleports a team or the spectators to a given location.
 *
 * Usage: /uh tp team <x> <y> <z> <team name ...>
 * Usage: /uh tp team <target> <team name...>
 * Usage: /uh tp spectators <x> <y> <z>
 * Usage: /uh tp spectators <target>
 */
@Command (name = "tp")
public class UHTPCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHTPCommand(UHCReloaded plugin)
    {
        this.p = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        // No action provided: doc
        if (args.length == 0)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
        }

        else
        {
            String subcommand = args[0];

            World targetWorld;
            if (sender instanceof Player)
            {
                targetWorld = ((Player) sender).getWorld();
            }
            else if (sender instanceof BlockCommandSender)
            {
                targetWorld = ((BlockCommandSender) sender).getBlock().getWorld();
            }
            else
            {
                targetWorld = p.getServer().getWorlds().get(0);
            }

            if (subcommand.equalsIgnoreCase("team"))
            {
                boolean mayBeNaNError = false;

                // possibly /uh tp team <x> <y> <z> <team ...>
                if (args.length >= 6)
                {
                    final ZTeam team = ZTeams.get().getTeamByName(UHUtils.getStringFromCommandArguments(args, 4));

                    // ok, the team exists.
                    if (team != null)
                    {
                        try
                        {
                            double x = Integer.parseInt(args[1]) + 0.5;
                            double y = Integer.parseInt(args[2]) + 0.5;
                            double z = Integer.parseInt(args[3]) + 0.5;

                            team.teleportTo(new Location(targetWorld, x, y, z));

                            return;
                        }
                        catch (NumberFormatException e)
                        {
                            // It can be either another name for the team, starting by "<y> <z> the name"
                            // or a formatting error.
                            // The possibility of an error is saved.
                            mayBeNaNError = true;
                        }
                    }
                }

                // /uh tp team <target> <team ...>
                if (args.length >= 3)
                {
                    final ZTeam team = ZTeams.get().getTeamByName(UHUtils.getStringFromCommandArguments(args, 2));

                    if (team == null)
                    {
                        if (mayBeNaNError)
                        {
                            sender.sendMessage(I.t("{ce}The coordinates must be three valid numbers."));
                        }
                        else
                        {
                            sender.sendMessage(I.t("{ce}This team is not registered."));
                        }
                    }
                    else
                    {
                        Player target = p.getServer().getPlayer(args[1]);

                        if (target == null)
                        {
                            sender.sendMessage(I.t("{ce}{0} is offline!", args[1]));
                        }
                        else
                        {
                            team.teleportTo(target.getLocation());
                        }
                    }
                }
            }

            else if (subcommand.equalsIgnoreCase("spectators"))
            {
                // /uh tp spectators <x> <y> <z>
                if (args.length == 4)
                {
                    try
                    {
                        double x = Integer.parseInt(args[1]) + 0.5;
                        double y = Integer.parseInt(args[2]) + 0.5;
                        double z = Integer.parseInt(args[3]) + 0.5;

                        for (Player player : p.getServer().getOnlinePlayers())
                        {
                            if (p.getGameManager().isPlayerDead(player))
                            {
                                player.teleport(new Location(targetWorld, x, y, z), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        sender.sendMessage(I.t("{ce}The coordinates must be three valid numbers."));
                    }
                }

                // /uh tp spectators <target>
                else if (args.length == 2)
                {
                    Player target = p.getServer().getPlayer(args[1]);

                    if (target == null)
                    {
                        sender.sendMessage(I.t("{ce}{0} is offline!", args[1]));
                    }
                    else
                    {
                        for (Player player : p.getServer().getOnlinePlayers())
                        {
                            if (p.getGameManager().isPlayerDead(player))
                            {
                                player.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {

        if (args.length == 1)
        {
            return CommandUtils.getAutocompleteSuggestions(
                    args[0],
                    Arrays.asList("team", "spectators")
            );
        }

        else if (args.length > 1 && args[0].equalsIgnoreCase("team"))
        {
            ArrayList<String> teamNames = new ArrayList<>();
            for (ZTeam team : ZTeams.get().getTeams())
            {
                teamNames.add(team.getName());
            }

            // /uh tp team <x> <y> <z> <?>: autocompletion for team names – multiple words autocompletion
            if (args.length >= 5)
            {
                return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 4), teamNames, args.length - 5);
            }

            // /uh tp team <target> <?>: autocompletion for team names – multiple words autocompletion
            if (args.length >= 3)
            {
                try
                {
                    return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 2), teamNames, args.length - 3);
                }
                catch (IllegalArgumentException ignored)
                {
                    // Temp workaround for an unknown bug.
                }
            }
        }

        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return Arrays.asList(
                I.t("{aqua}------ Teleportation commands ------"),
                I.t("{cc}/uh tp team <x> <y> <z> | <target> <team ...> {ci}: teleports the team to the given location/target."),
                I.t("{cc}/uh tp spectators <x> <y> <z> | <target> {ci}: teleports the spectators (aka non-alive players) to the given location/target.")
        );
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh tp {ci}: teleports the spectators or an entire team. See /uh tp for details."));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
