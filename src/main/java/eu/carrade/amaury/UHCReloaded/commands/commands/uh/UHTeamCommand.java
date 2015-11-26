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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamAddCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamJoinCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamLeaveCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamListCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamRemoveCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.team.UHTeamResetCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * This command is used to manage the teams.
 *
 * Usage: /uh team (for the doc).
 * Usage: /uh team <add|remove|join|leave|list|reset> (see doc for details).
 */
@Command (name = "team")
public class UHTeamCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHTeamCommand(UHCReloaded plugin)
    {
        p = plugin;
        i = plugin.getI18n();

        registerSubCommand(new UHTeamAddCommand(p));
        registerSubCommand(new UHTeamRemoveCommand(p));
        registerSubCommand(new UHTeamJoinCommand(p));
        registerSubCommand(new UHTeamLeaveCommand(p));
        registerSubCommand(new UHTeamListCommand(p));
        registerSubCommand(new UHTeamResetCommand(p));
    }

    /**
     * This will be executed if this command is called without argument,
     * or if there isn't any sub-command executor registered.
     *
     * @param sender The sender.
     * @param args   The arguments passed to the command.
     */
    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
    }

    /**
     * The result of this method will be added to the tab-complete suggestions for this command.
     *
     * @param sender The sender.
     * @param args   The arguments.
     *
     * @return The suggestions to add.
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return Arrays.asList(
                i.t("cmd.teamHelpTitle"),
                i.t("cmd.teamHelpJoinCmd"),
                i.t("cmd.teamHelpLeaveCmd")
        );
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.helpTeam"));
    }

    @Override
    public String getCategory()
    {
        return Category.GAME.getTitle();
    }
}
