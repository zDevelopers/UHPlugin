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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This command freezes the players.
 *
 * Usage: /uh freeze <on [player]|off [player]|all|none>
 *  - on [player]: freezes the given player, or the sender if no player was provided.
 *  - off [player]: unfreezes the given player (or the sender, same condition).
 *  - all: freezes all the alive players, the mobs and the timer.
 *  - none: unfreezes all the alive players (even if there where frozen before using
 *          /uh freeze all), the mobs and the timer.
 */
@Command (name = "freeze")
public class UHFreezeCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHFreezeCommand(UHCReloaded plugin)
    {
        p = plugin;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
        }

        String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("on") || subCommand.equalsIgnoreCase("off"))
        {
            final boolean on = subCommand.equalsIgnoreCase("on");

            // /uh freeze on: freezes the sender
            if (args.length == 1)
            {
                if (sender instanceof Player)
                {
                    p.getFreezer().setPlayerFreezeState((Player) sender, on);

                    if (on)
                    {
                        sender.sendMessage(I.t("{cst}You where frozen by {0}.", sender.getName()));
                    }
                    else
                    {
                        sender.sendMessage(I.t("{cst}You where unfrozen by {0}.", sender.getName()));
                    }
                }
                else
                {
                    throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
                }
            }

            // /uh freeze on <player>: freezes <player>.
            else if (args.length == 2)
            {
                Player player = p.getServer().getPlayer(args[1]);
                if (player == null)
                {
                    sender.sendMessage(I.t("{ce}{0} is offline!", args[1]));
                }
                else
                {
                    p.getFreezer().setPlayerFreezeState(player, on);
                    if (on)
                    {
                        player.sendMessage(I.t("{cst}You where frozen by {0}.", sender.getName()));
                        sender.sendMessage(I.t("{cs}{0} is now frozen.", player.getName()));
                    }
                    else
                    {
                        player.sendMessage(I.t("{cst}You where unfrozen by {0}.", sender.getName()));
                        sender.sendMessage(I.t("{cs}{0} is now unfrozen.", player.getName()));
                    }
                }
            }
        }

        else if (subCommand.equalsIgnoreCase("all") || subCommand.equalsIgnoreCase("none"))
        {
            final boolean on = subCommand.equalsIgnoreCase("all");

            p.getFreezer().setGlobalFreezeState(on);

            if (on)
            {
                p.getServer().broadcastMessage(I.t("{darkaqua}The entire game is now frozen."));
            }
            else
            {
                p.getServer().broadcastMessage(I.t("{darkaqua}The game is now unfrozen."));
            }

        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return CommandUtils.getAutocompleteSuggestions(
                    args[0], Arrays.asList("on", "off", "all", "none")
            );
        }

        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("off"))
            {
                List<String> suggestions = new ArrayList<>();

                for (Player player : p.getFreezer().getFrozenPlayers())
                {
                    suggestions.add(player.getName());
                }

                return CommandUtils.getAutocompleteSuggestions(args[1], suggestions);
            }

            else return null;
        }

        else return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return Arrays.asList(
                I.t("{aqua}------ Freeze commands ------"),
                I.t("{cc}/uh freeze on [player]{ci}: freezes a player, or the sender without a specified player."),
                I.t("{cc}/uh freeze off [player]{ci}: unfreezes a player (or the sender), even if the entire game is frozen."),
                I.t("{cc}/uh freeze all{ci}: freezes the entire game (players, mobs, timer)."),
                I.t("{cc}/uh freeze none{ci}: unfreezes the entire game. You NEED to execute this in order to relaunch the timer.")
        );
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh freeze {ci}: (un)freezes the entire game, or a player. See /uh freeze for details."));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
