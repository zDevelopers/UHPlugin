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

package eu.carrade.amaury.UHCReloaded.misc;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.task.ScheduledCommandsExecutorTask;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This will execute the commands to be executed during runtime, as configured in the config.yml file
 * or added through the API.
 */
public class RuntimeCommandsExecutor
{

    private UHCReloaded p = null;

    /**
     * Stores the commands to be executed later.
     * <p>
     * The first map (String->HashMap) associates a key to a group of commands launched at the same time.<br>
     * The sub-map (Integer->HashSet) associates a delay, in seconds, with a set containing the commands to be
     * executed this number of seconds after the call of the {@link #registerCommandsInScheduler(String)}
     * method.
     */
    private Map<String, HashMap<Integer, HashSet<String>>> scheduled = new HashMap<String, HashMap<Integer, HashSet<String>>>();


    /**
     * The key for the commands executed when the server starts.
     */
    public final static String AFTER_SERVER_START = "internal.server-start";

    /**
     * The key for the commands executed after the beginning of the game.
     */
    public final static String AFTER_GAME_START = "internal.game-start";

    /**
     * The key for the commands executed after the end of the game.
     */
    public final static String AFTER_GAME_END = "internal.game-end";


    public RuntimeCommandsExecutor(UHCReloaded plugin)
    {
        p = plugin;

        importFromConfig("commands.execute-server-start", AFTER_SERVER_START);
        importFromConfig("commands.execute-start", AFTER_GAME_START);
        importFromConfig("commands.execute-end", AFTER_GAME_END);
    }

    /**
     * Register the commands registered under the given key in the Bukkit' scheduler.
     * <p>
     * Delays are from the execution of this method.
     *
     * @param key The key to schedule. All commands previously registered under this key will be executed.
     */
    public void registerCommandsInScheduler(String key)
    {
        registerCommandsInScheduler(scheduled.get(key));
    }

    /**
     * Register the given commands in the Bukkit' scheduler.
     *
     * Delays are from the execution of this method.
     * @param scheduledCommands
     */
    private void registerCommandsInScheduler(Map<Integer, HashSet<String>> scheduledCommands)
    {
        if (scheduledCommands != null)
        {
            for (Entry<Integer, HashSet<String>> scheduledCommandsStack : scheduledCommands.entrySet())
            {
                p.getServer().getScheduler().runTaskLater(
                        p,
                        new ScheduledCommandsExecutorTask(p, scheduledCommandsStack.getValue()),
                        scheduledCommandsStack.getKey() * 20l
                );
            }
        }
    }


    /**
     * Schedules a command.
     * <p>
     * To schedule a command executed by the plugin, like in the configuration file, you will have
     * to use the keys defined as static attributes of this class:
     * {@link #AFTER_SERVER_START}, {@link #AFTER_GAME_END} and {@link #AFTER_GAME_START}.
     *
     * @param key The command will be stored under this key.
     *    The keys internally used by the plugin start by "{@code internal.}".
     * @param command The command to add.
     * @param delay The delay (seconds).
     */
    public void scheduleCommand(String key, String command, Integer delay)
    {
        if (!scheduled.containsKey(key))
        {
            scheduled.put(key, new HashMap<Integer, HashSet<String>>());
        }

        scheduleCommand(scheduled.get(key), command, delay);
    }

    /**
     * Schedules a command.
     *
     * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
     * @param command The command to add.
     * @param delay The delay (seconds).
     */
    private void scheduleCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, Integer delay)
    {
        HashSet<String> list = scheduledCommands.get(delay);

        if (list == null)
        {
            list = new HashSet<>();
            scheduledCommands.put(delay, list);
        }

        list.add(clearCommandName(command));
    }


    /**
     * Removes the given command from everywhere.
     *
     * @param key The command will be stored under this key.
     *    The keys internally used by the plugin start by "{@code internal.}".
     * @param command The command. Not case-sensitive.
     */
    public void removeScheduledCommand(String key, String command)
    {
        removeScheduledCommand(scheduled.get(key), command);
    }

    /**
     * Removes the given command from everywhere.
     *
     * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
     * @param command The command. Not case-sensitive.
     */
    private void removeScheduledCommand(Map<Integer, HashSet<String>> scheduledCommands, String command)
    {
        for (HashSet<String> commands : scheduledCommands.values())
        {
            for (String scheduledCommand : new HashSet<String>(commands))
            {
                if (scheduledCommand.equalsIgnoreCase(clearCommandName(command)))
                {
                    commands.remove(scheduledCommand);
                }
            }
        }
    }


    /**
     * Removes the given command from everywhere.
     *
     * @param key The command will be stored under this key.
     *    The keys internally used by the plugin start by "{@code internal.}".
     * @param command The command. Not case-sensitive.
     */
    public void removeScheduledCommand(String key, String command, Integer delay)
    {
        removeScheduledCommand(scheduled.get(key), command, delay);
    }

    /**
     * Removes the given command from everywhere.
     *
     * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
     * @param command The command. Not case-sensitive.
     */
    private void removeScheduledCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, Integer delay)
    {
        HashSet<String> commands = scheduledCommands.get(delay);

        if (commands != null)
        {
            for (String scheduledCommand : commands)
            {
                if (scheduledCommand.equalsIgnoreCase(clearCommandName(command)))
                {
                    commands.remove(scheduledCommand);
                }
            }
        }
    }

	
	/* Utilities */

    /**
     * Imports the commands stored in the configuration.
     *
     * @param path The path in the config file.
     * @param key The command will be stored under this key.
     *    The keys internally used by the plugin start by "{@code internal.}".
     */
    private void importFromConfig(String path, String key)
    {
        List<Map<?, ?>> rawCommands = p.getConfig().getMapList(path);

        if (rawCommands != null)
        {
            for (Map<?, ?> rawCommand : rawCommands)
            {
                String cmd = String.valueOf(rawCommand.get("exec"));
                Integer delay;

                if (cmd == null || cmd.isEmpty()) continue;

                try
                {
                    delay = UHUtils.string2Time(String.valueOf(rawCommand.get("delay")));
                }
                catch (IllegalArgumentException e)
                {
                    delay = 0;
                }

                scheduleCommand(key, cmd, delay);
            }
        }
    }


    private String clearCommandName(String command)
    {
        if (command.startsWith("/"))
        {
            command = command.substring(1);
        }

        return command;
    }
}
