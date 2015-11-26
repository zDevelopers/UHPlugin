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
package eu.carrade.amaury.UHCReloaded.commands.commands;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHAboutCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHBorderCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFeedAllCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFeedCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFinishCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFreezeCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHGenerateWallsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHHealAllCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHHealCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHInfosCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHKillCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHResurrectCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHShiftCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHSpawnsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHSpectatorsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHStartCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTPBackCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTPCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTeamCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTimersCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.List;


@Command (name = "uh")
public class UHRootCommand extends AbstractCommand
{

    private UHCReloaded p;
    private I18n i;

    public UHRootCommand(UHCReloaded plugin)
    {
        p = plugin;
        i = p.getI18n();

        // Game
        registerSubCommand(new UHStartCommand(p));
        registerSubCommand(new UHShiftCommand(p));
        registerSubCommand(new UHSpawnsCommand(p));
        registerSubCommand(new UHTeamCommand(p));
        registerSubCommand(new UHBorderCommand(p));
        registerSubCommand(new UHSpectatorsCommand(p));
        registerSubCommand(new UHGenerateWallsCommand(p));

        // Bugs
        registerSubCommand(new UHHealCommand(p));
        registerSubCommand(new UHHealAllCommand(p));
        registerSubCommand(new UHFeedCommand(p));
        registerSubCommand(new UHFeedAllCommand(p));
        registerSubCommand(new UHKillCommand(p));
        registerSubCommand(new UHResurrectCommand(p));
        registerSubCommand(new UHTPBackCommand(p));

        // Misc
        registerSubCommand(new UHFinishCommand(p));
        registerSubCommand(new UHFreezeCommand(p));
        registerSubCommand(new UHTimersCommand(p));
        registerSubCommand(new UHTPCommand(p));
        registerSubCommand(new UHInfosCommand(p));
        registerSubCommand(new UHAboutCommand(p));
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }
}
