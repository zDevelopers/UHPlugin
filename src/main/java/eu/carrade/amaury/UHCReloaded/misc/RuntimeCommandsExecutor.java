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
