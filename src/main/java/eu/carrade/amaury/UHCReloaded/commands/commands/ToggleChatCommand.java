/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade <p/> This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version. <p/> This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details. <p/> You should have received a copy of the GNU General Public License along with
 * this program.  If not, see [http://www.gnu.org/licenses/].
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
