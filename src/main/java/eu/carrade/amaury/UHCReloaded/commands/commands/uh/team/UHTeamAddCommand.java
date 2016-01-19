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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.team;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.teams.TeamColor;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "add")
public class UHTeamAddCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHTeamAddCommand(UHCReloaded plugin)
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
        if (args.length == 1)
        { // /uh team add <color>

            TeamColor color = TeamColor.fromString(args[0]);
            UHTeam team;

            if (color == null)
            {
                sender.sendMessage(I.t("team.add.errorColor"));
            }
            else
            {
                try
                {
                    team = p.getTeamManager().addTeam(color);
                }
                catch (IllegalArgumentException e)
                {
                    sender.sendMessage(I.t("team.add.errorExists"));
                    return;
                }

                sender.sendMessage(I.t("team.add.added", team.getDisplayName()));
            }

        }
        else if (args.length >= 2)
        { // /uh team add <color> <name ...>

            TeamColor color = TeamColor.fromString(args[0]);
            UHTeam team;

            if (color == null)
            {
                sender.sendMessage(I.t("team.add.errorColor"));
            }
            else
            {
                String name = UHUtils.getStringFromCommandArguments(args, 1);

                try
                {
                    team = p.getTeamManager().addTeam(color, name);
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    sender.sendMessage(I.t("team.add.errorExists"));
                    return;
                }

                sender.sendMessage(I.t("team.add.added", team.getDisplayName()));
            }

        }
        else
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
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
        if (args.length == 1)
        {
            List<String> colors = Arrays.asList("aqua", "black", "blue", "darkaqua",
                    "darkblue", "darkgray", "darkgreen", "darkpurple", "darkred",
                    "gold", "gray", "green", "lightpurple", "red", "white", "yellow", "?");

            return CommandUtils.getAutocompleteSuggestions(args[0], colors);
        }

        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("cmd.teamHelpAdd"));
    }
}
