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

package eu.carrade.amaury.UHCReloaded.task;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.command.CommandException;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.logging.Level;


/**
 * Schedules a stack of commands executed at the same time.
 */
public class ScheduledCommandsExecutorTask extends BukkitRunnable
{

    UHCReloaded p = null;
    HashSet<String> commands = null;

    public ScheduledCommandsExecutorTask(UHCReloaded plugin, HashSet<String> commands)
    {
        this.p = plugin;
        this.commands = commands;
    }

    @Override
    public void run()
    {
        for (String command : commands)
        {
            try
            {
                p.getServer().dispatchCommand(p.getServer().getConsoleSender(), command);
            }
            catch (CommandException e)
            {
                p.getLogger().log(Level.WARNING, "The scheduled command '" + command + "' failed.", e);
            }
        }
    }
}
