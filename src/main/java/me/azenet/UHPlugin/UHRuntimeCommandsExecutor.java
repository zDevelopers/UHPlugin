/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package me.azenet.UHPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.azenet.UHPlugin.task.ScheduledCommandsExecutorTask;


/**
 * This will execute the commands to be executed during runtime, as configured in the config.yml file
 * or added through the API.
 */
public class UHRuntimeCommandsExecutor {
	
	private UHPlugin p = null;
	
	private Map<Integer, HashSet<String>> scheduledAfterStart = new HashMap<Integer,HashSet<String>>();
	private Map<Integer, HashSet<String>> scheduledAfterEnd   = new HashMap<Integer,HashSet<String>>();
	
	private Boolean startCommandsScheduled = false;
	private Boolean endCommandsScheduled = false;
	
	
	public UHRuntimeCommandsExecutor(UHPlugin plugin) {
		p = plugin;
		
		importFromConfig("commands.execute-start", scheduledAfterStart);
		importFromConfig("commands.execute-end",   scheduledAfterEnd  );
	}
	
	/**
	 * Schedules the commands executed after the beginning of the game in the Bukkit' scheduler.
	 * 
	 * After that, these command cannot be changed.
	 */
	public void registerStartCommandsInScheduler() {
		registerCommandsInScheduler(scheduledAfterStart);
		
		startCommandsScheduled = true;
	}
	
	/**
	 * Schedules the commands executed after the end of the game in the Bukkit' scheduler.
	 * 
	 * After that, these command cannot be changed.
	 */
	public void registerEndCommandsInScheduler() {
		registerCommandsInScheduler(scheduledAfterEnd);
		
		endCommandsScheduled = true;
	}
	
	
	/**
	 * Schedules a command after the beginning of the game.
	 * 
	 * @param command The command to be executed (by the console).
	 * @param delay The delay between the beginning of the game and the execution (in seconds).
	 * 
	 * @throws IllegalStateException if the game is started (commands already scheduled).
	 */
	public void scheduleCommandAfterStart(String command, Integer delay) {
		if(startCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the beginning of the game are already scheduled");
		}
		
		scheduleCommand(scheduledAfterStart, command, delay);
	}
	
	/**
	 * Schedules a command after the end of the game.
	 * 
	 * @param command The command to be executed (by the console).
	 * @param delay The delay between the beginning of the game and the execution (in seconds).
	 * 
	 * @throws IllegalStateException if the game is finished (commands already scheduled).
	 */
	public void scheduleCommandAfterEnd(String command, Integer delay) {		
		if(endCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the end of the game are already scheduled");
		}
		
		scheduleCommand(scheduledAfterEnd, command, delay);
	}
	
	/**
	 * Removes the given command from the commands scheduled after the beginning of the game,
	 * for all delays.
	 * 
	 * @param command The command. Not case-sensitive.
	 * 
	 * @throws IllegalStateException if the game is started (commands already scheduled).
	 */
	public void removeScheduledStartCommand(String command) {
		if(startCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the beginning of the game are already scheduled");
		}
		
		removeScheduledCommand(scheduledAfterStart, command);
	}
	
	/**
	 * Removes the given command from the commands scheduled after the beginning of the game,
	 * with the given delay.
	 * 
	 * @param command The command. Not case-sensitive.
	 * @param delay The delay.
	 * 
	 * @throws IllegalStateException if the game is started (commands already scheduled).
	 */
	public void removeScheduledStartCommand(String command, Integer delay) {
		if(startCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the beginning of the game are already scheduled");
		}
		
		removeScheduledCommand(scheduledAfterStart, command, delay);
	}
	
	/**
	 * Removes the given command from the commands scheduled after the end of the game, for all delays.
	 * 
	 * @param command The command. Not case-sensitive.
	 * 
	 * @throws IllegalStateException if the game is finished (commands already scheduled).
	 */
	public void removeScheduledEndCommand(String command) {
		if(endCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the end of the game are already scheduled");
		}
		
		removeScheduledCommand(scheduledAfterEnd, command);
	}
	
	/**
	 * Removes the given command from the commands scheduled after the end of the game,
	 * with the given delay.
	 * 
	 * @param command The command. Not case-sensitive.
	 * @param delay The delay.
	 * 
	 * @throws IllegalStateException if the game is finished (commands already scheduled).
	 */
	public void removeScheduledEndCommand(String command, Integer delay) {
		if(endCommandsScheduled) {
			throw new IllegalStateException("The commands executed after the end of the game are already scheduled");
		}
		
		removeScheduledCommand(scheduledAfterEnd, command, delay);
	}
	
	
	/* Generic methods */
	
	
	/**
	 * Register the given commands in the Bukkit' scheduler.
	 * 
	 * Delay is from the execution of this method.
	 * @param scheduledCommands
	 */
	public void registerCommandsInScheduler(Map<Integer, HashSet<String>> scheduledCommands) {
		if(scheduledCommands != null) {
			for(Entry<Integer, HashSet<String>> scheduledCommandsStack : scheduledCommands.entrySet()) {
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
	 * 
	 * @param commands A map containing the scheduled commands, sorted by delay.
	 * @param command The command to add.
	 * @param delay The delay (seconds).
	 */
	public void scheduleCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, Integer delay) {
		HashSet<String> list = scheduledCommands.get(delay);
		
		if(list == null) {
			list = new HashSet<String>();
			scheduledCommands.put(delay, list);
		}
		
		list.add(clearCommandName(command));
	}
	
	/**
	 * Removes the given command from everywhere.
	 * 
	 * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
	 * @param command The command. Not case-sensitive.
	 */
	public void removeScheduledCommand(Map<Integer, HashSet<String>> scheduledCommands, String command) {
		for(HashSet<String> commands : scheduledCommands.values()) {
			for(String scheduledCommand : new HashSet<String>(commands)) {
				if(scheduledCommand.equalsIgnoreCase(clearCommandName(command))) {
					commands.remove(scheduledCommand);
				}
			}
		}
	}
	
	/**
	 * Removes the given command from everywhere.
	 * 
	 * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
	 * @param command The command. Not case-sensitive.
	 */
	public void removeScheduledCommand(Map<Integer, HashSet<String>> scheduledCommands, String command, Integer delay) {
		HashSet<String> commands = scheduledCommands.get(delay);
		
		if(commands != null) {
			for(String scheduledCommand : commands) {
				if(scheduledCommand.equalsIgnoreCase(clearCommandName(command))) {
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
	 * @param scheduledCommands A map containing the scheduled commands, sorted by delay.
	 */
	private void importFromConfig(String path, Map<Integer, HashSet<String>> scheduledCommands) {
		List<Map<?, ?>> rawCommands = p.getConfig().getMapList(path);
		
		if(rawCommands != null) {
			for(Map<?, ?> rawCommand : rawCommands) {
				String cmd = String.valueOf(rawCommand.get("exec"));
				Integer delay;
				
				if(cmd == null || cmd.isEmpty()) continue;
				
				try {
					delay = UHUtils.string2Time(String.valueOf(rawCommand.get("delay")));
				} catch(IllegalArgumentException e) {
					delay = 0;
				}
				
				scheduleCommand(scheduledCommands, cmd, delay);
			}
		}
	}
	
	
	
	private String clearCommandName(String command) {
		if(command.startsWith("/")) {
			command = command.substring(1);
		}
		
		return command;
	}
}
