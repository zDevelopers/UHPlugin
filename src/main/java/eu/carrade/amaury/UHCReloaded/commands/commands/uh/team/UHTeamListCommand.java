/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.team;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "list")
public class UHTeamListCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHTeamListCommand(UHCReloaded plugin)
    {
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
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (p.getTeamManager().getTeams().size() == 0)
        {
            sender.sendMessage(i.t("team.list.nothing"));
            return;
        }

        for (final UHTeam team : p.getTeamManager().getTeams())
        {
            sender.sendMessage(i.t("team.list.itemTeam", team.getDisplayName(), ((Integer) team.getSize()).toString()));
            for (final OfflinePlayer player : team.getPlayers())
            {
                String bullet;
                if (player.isOnline())
                {
                    bullet = i.t("team.list.bulletPlayerOnline");
                }
                else
                {
                    bullet = i.t("team.list.bulletPlayerOffline");
                }

                sender.sendMessage(bullet + i.t("team.list.itemPlayer", player.getName()));
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

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.teamHelpList"));
    }
}
