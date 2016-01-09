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

package eu.carrade.amaury.UHCReloaded.commands.commands;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


/**
 * This command, /togglechat, is used to toggle the chat between the global chat and
 * the team chat.
 */
@Command (name = "togglechat", noPermission = true)
public class ToggleChatCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public ToggleChatCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (!(sender instanceof Player))
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
        }

        if (args.length == 0)
        { // /togglechat
            if (p.getTeamChatManager().toggleChatForPlayer((Player) sender))
            {
                sender.sendMessage(i.t("team.message.toggle.nowTeamChat"));
            }
            else
            {
                sender.sendMessage(i.t("team.message.toggle.nowGlobalChat"));
            }
        }
        else
        { // /togglechat <another team>
            String teamName = UHUtils.getStringFromCommandArguments(args, 0);
            UHTeam team = p.getTeamManager().getTeam(teamName);

            if (team != null)
            {
                if (p.getTeamChatManager().toggleChatForPlayer((Player) sender, team))
                {
                    sender.sendMessage(i.t("team.message.toggle.nowOtherTeamChat", team.getDisplayName()));
                }
            }
            else
            {
                sender.sendMessage(i.t("team.message.toggle.unknownTeam"));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (sender instanceof Player && sender.hasPermission("uh.teamchat.others"))
        {
            ArrayList<String> teamNames = new ArrayList<>();

            for (UHTeam team : p.getTeamManager().getTeams())
            {
                teamNames.add(team.getName());
            }

            return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 0), teamNames, args.length - 1);
        }

        else return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return null;
    }
}
