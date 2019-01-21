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
package eu.carrade.amaury.UHCReloaded.modules.utilities.runtimeCommandsExecutor;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * This will execute the commands to be executed during runtime, as configured in the config file
 * or added through the API.
 */
@ModuleInfo (
        name = "Runtime Commands Executor",
        description = "Executes commands during runtime, at specific times of the game, automatically. " +
                "This is a powerful tool to schedule any command at any time relative to the game progression.",
        when = ModuleLoadTime.STARTUP,
        category = ModuleCategory.UTILITIES,
        icon = Material.COMMAND,
        settings = Config.class
)
public class RuntimeCommandsExecutor extends UHModule
{
    /**
     * Stores the commands to be executed later.
     * <p>
     * The first map (String->HashMap) associates a key to a group of commands launched at the same time.<br>
     * The sub-map (Integer->HashSet) associates a delay, in seconds, with a set containing the commands to be
     * executed this number of seconds after the call of the {@link #runCommands(String)}
     * method.
     */
    private final Map<String, HashMap<Integer, HashSet<String>>> scheduled = new HashMap<>();

    /**
     * Stores the running tasks, to be able to cancel them.
     */
    private final Map<String, Set<BukkitTask>> runningTasks = new HashMap<>();


    /**
     * Register the commands registered under the given key in the Bukkit' scheduler.
     * <p>
     * Delays are from the execution of this method.
     *
     * @param key The key to schedule. All commands previously registered under this key will be executed.
     */
    public void runCommands(final String key)
    {
        runCommands(key, scheduled.get(key));
    }

    /**
     * Register the commands registered under the given phase in the Bukkit' scheduler.
     * <p>
     * Delays are from the execution of this method.
     *
     * @param phase The phase to schedule. All commands previously registered under this phase will be executed.
     */
    public void runCommands(final GamePhase phase)
    {
        runCommands(getPhaseKey(phase));
    }

    /**
     * Register the given commands in the Bukkit' scheduler.
     *
     * Delays are from the execution of this method.
     * @param key The key to store the tasks under.
     * @param scheduledCommands The commands to schedule
     */
    private void runCommands(final String key, final Map<Integer, HashSet<String>> scheduledCommands)
    {
        if (scheduledCommands != null)
        {
            final Set<BukkitTask> tasks = runningTasks.computeIfAbsent(key, k -> new HashSet<>());
            for (Entry<Integer, HashSet<String>> scheduledCommandsStack : scheduledCommands.entrySet())
            {
                tasks.add(RunTask.later(
                        new ScheduledCommandsExecutorTask(scheduledCommandsStack.getValue()),
                        scheduledCommandsStack.getKey() * 20L
                ));
            }
        }
    }


    /**
     * Cancels all tasks currently running for the given key.
     * @param key The key.
     */
    public void cancelTasks(final String key)
    {
        if (runningTasks.containsKey(key))
        {
            runningTasks.get(key).forEach(BukkitTask::cancel);
            runningTasks.get(key).clear();
        }
    }

    /**
     * Cancels all tasks currently running for the given phase.
     * @param phase The phase.
     */
    public void cancelTasks(final GamePhase phase)
    {
        cancelTasks(getPhaseKey(phase));
    }

    /**
     * Schedules a command.
     *
     * Commands scheduled with any non-standard key will not be executed automatically. You'll have
     * to call {@link #runCommands(String)} to schedule them.
     *
     * @param key The command will be stored under this key.
     * @param command The command to add.
     * @param delay The delay.
     * @see #scheduleCommand(GamePhase, String, TimeDelta) to schedule a command from one game phase change.
     */
    public void scheduleCommand(final String key, final String command, final TimeDelta delay)
    {
        final Map<Integer, HashSet<String>> commandsMap = scheduled.computeIfAbsent(key, k -> new HashMap<>());
        scheduleCommand(commandsMap, command, delay);
    }

    /**
     * Schedule a command to be run when the given phase starts.
     *
     * @param phase The phase
     * @param command The command to execute
     * @param delay The delay after the phase's beginning.
     */
    public void scheduleCommand(final GamePhase phase, String command, final TimeDelta delay)
    {
        scheduleCommand(getPhaseKey(phase), command, delay);
    }

    /**
     * Schedules a command.
     *
     * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
     * @param command The command to add.
     * @param delay The delay (seconds).
     */
    private void scheduleCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, TimeDelta delay)
    {
        final Set<String> list = scheduledCommands.computeIfAbsent((int) delay.getSeconds(), k -> new HashSet<>());
        list.add(clearCommandName(command));
    }


    /**
     * Removes the given command from everywhere.
     *
     * @param key The command will be stored under this key.
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
            for (String scheduledCommand : new HashSet<>(commands))
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
     * @param key The key the command was registered under.
     * @param command The command. Not case-sensitive.
     */
    public void removeScheduledCommand(String key, String command, TimeDelta delay)
    {
        removeScheduledCommand(scheduled.get(key), command, delay);
    }

    /**
     * Removes the given command from everywhere.
     *
     * @param phase The phase the command was registered under.
     * @param command The command. Not case-sensitive.
     */
    public void removeScheduledCommand(GamePhase phase, String command, TimeDelta delay)
    {
        removeScheduledCommand(getPhaseKey(phase), command, delay);
    }

    /**
     * Removes the given command from everywhere.
     *
     * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
     * @param command The command. Not case-sensitive.
     */
    private void removeScheduledCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, TimeDelta delay)
    {
        HashSet<String> commands = scheduledCommands.get(((int) delay.getSeconds()));

        if (commands != null)
        {
            commands.stream()
                    .filter(scheduledCommand -> scheduledCommand.equalsIgnoreCase(clearCommandName(command)))
                    .forEach(commands::remove);
        }
    }

	
	/* Utilities */


    private String clearCommandName(String command)
    {
        if (command.startsWith("/"))
        {
            command = command.substring(1);
        }

        return command;
    }

    private String getPhaseKey(final GamePhase phase)
    {
        return "internal." + phase.name().toLowerCase().replace('_', '-');
    }


    /* Events to launch tasks */

    @EventHandler
    protected void onGamePhaseChanged(final GamePhaseChangedEvent ev)
    {
        if (!ev.isRunningForward())
        {
            cancelTasks(ev.getNewPhase());
            return;
        }

        // We load the commands to run
        final ConfigurationList<Map> commands;

        switch (ev.getNewPhase())
        {
            case WAIT:     commands = Config.WAIT; break;
            case STARTING: commands = Config.STARTING; break;
            case IN_GAME:  commands = Config.IN_GAME; break;
            case END:      commands = Config.END; break;
            default: return;
        }

        commands.stream()
            .filter(command -> command.containsKey("exec") && !command.get("exec").toString().isEmpty())
            .forEach(command -> {
                try
                {
                    scheduleCommand(ev.getNewPhase(), command.get("exec").toString(), new TimeDelta(command.get("delay").toString()));
                }
                catch (IllegalArgumentException e)
                {
                    PluginLogger.error(
                            "Invalid delay “{0}” in scheduled command “{1}” for phase {2}",
                            command.get("delay").toString(),
                            command.get("exec").toString(),
                            ev.getNewPhase()
                    );
                }
            });

        // And we schedule all of them.
        runCommands(ev.getNewPhase());
    }
}
