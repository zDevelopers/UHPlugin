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
import java.util.HashSet;
import java.util.List;

/**
 * This command manages startup spectators (aka ignored players).
 *
 * Usage: /uh spec (doc)
 * Usage: /uh spec <add|remove|list>
 */
@Command (name = "spec")
public class UHSpectatorsCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHSpectatorsCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
        { // /uh spec
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
        }

        else
        {
            String subcommand = args[0];

            if (subcommand.equalsIgnoreCase("add"))
            {
                if (args.length == 1)
                { // /uh spec add
                    throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
                }
                else
                { // /uh spec add <player>
                    Player newSpectator = p.getServer().getPlayer(args[1]);
                    if (newSpectator == null)
                    {
                        sender.sendMessage(I.t("{ce}The player {0} is not online.", args[1]));
                    }
                    else
                    {
                        p.getGameManager().addStartupSpectator(newSpectator);
                        sender.sendMessage(I.t("{cs}The player {0} is now a spectator.", args[1]));
                    }
                }
            }

            else if (subcommand.equalsIgnoreCase("remove"))
            {
                if (args.length == 1)
                { // /uh spec remove
                    throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
                }
                else
                { // /uh spec remove <player>
                    Player oldSpectator = p.getServer().getPlayer(args[1]);
                    if (oldSpectator == null)
                    {
                        sender.sendMessage(I.t("{ce}The player {0} is not online.", args[1]));
                    }
                    else
                    {
                        p.getGameManager().removeStartupSpectator(oldSpectator);
                        sender.sendMessage(I.t("{cs}The player {0} is now a player.", args[1]));
                    }
                }
            }

            else if (subcommand.equalsIgnoreCase("list"))
            {
                HashSet<String> spectators = p.getGameManager().getStartupSpectators();
                if (spectators.size() == 0)
                {
                    sender.sendMessage(I.t("{ce}There isn't any spectator to list."));
                }
                else
                {
                    sender.sendMessage(I.t("{ci}{0} registered spectator(s).", String.valueOf(spectators.size())));
                    sender.sendMessage(I.t("{ci}This count includes only the initial spectators."));
                    for (String spectator : spectators)
                    {
                        /// A list item in the startup spectators list
                        sender.sendMessage(I.tc("startup_specs", "{lightpurple} - {0}", spectator));
                    }
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        // Manual suggestions needed because we don't use sub-commands.
        if (args.length == 1)
        {
            return CommandUtils.getAutocompleteSuggestions(args[0], Arrays.asList("add", "remove", "list"));
        }

        // /... spec remove <?>
        else if (args.length == 2 && args[1].equalsIgnoreCase("remove"))
        {
            List<String> suggestions = new ArrayList<>();

            for (String spectatorName : p.getGameManager().getStartupSpectators())
            {
                suggestions.add(spectatorName);
            }

            return CommandUtils.getAutocompleteSuggestions(args[1], suggestions);
        }

        else return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        List<String> help = new ArrayList<>();

        help.add(I.t("{aqua}------ Startup spectators commands ------"));

        help.add(I.t("{cc}/uh spec add <player>{ci}: adds a startup spectator."));
        help.add(I.t("{cc}/uh spec remove <player>{ci}: removes a startup spectator."));
        help.add(I.t("{cc}/uh spec list{ci}: lists the startup spectators."));

        return help;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh spec {ci}: manages the spectators. Execute /uh spec for details."));
    }

    @Override
    public String getCategory()
    {
        return Category.GAME.getTitle();
    }
}
