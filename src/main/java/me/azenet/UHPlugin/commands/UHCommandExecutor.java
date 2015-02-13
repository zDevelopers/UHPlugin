/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
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
package me.azenet.UHPlugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UHCommandExecutor implements TabExecutor {

	/**
	 * Stores the main commands, i.e. the commands registered in the {@code plugin.yml} file.
	 */
	private Map<String, UHCommand> mainCommands = new HashMap<>();

	/**
	 * Stores the base permissions of these commands.
	 */
	private Map<String, String> mainCommandsPermissions = new HashMap<>();

	/**
	 * Registers a main, root command. This command must be in the {@code plugin.yml}, or
	 * it will never be called.
	 *
	 * @param command The command.
	 *
	 * @throws IllegalArgumentException If the command class doesn't have the @Command
	 *                                  annotation.
	 */
	public void registerCommand(UHCommand command) {
		me.azenet.UHPlugin.commands.Command commandAnnotation = command.getClass().getAnnotation(me.azenet.UHPlugin.commands.Command.class);
		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without @Command annotation.");
		}

		mainCommands.put(commandAnnotation.name(), command);
		mainCommandsPermissions.put(commandAnnotation.name(), commandAnnotation.permission());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		UHCommand uhCommand = mainCommands.get(command.getName());
		if(uhCommand == null) {
			return false;
		}
		try {
			if(!sender.hasPermission(mainCommandsPermissions.get(command.getName()))) {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
			}
			uhCommand.run(sender, Arrays.copyOfRange(args, 1, args.length - 1));

		} catch(CannotExecuteCommandException e) {
			switch(e.getReason()) {
				case NOT_ALLOWED:
					sender.sendMessage("");
					break;

				case ONLY_AS_A_PLAYER:
					break;

				case BAD_USE:
					for(String line : uhCommand.help(sender)) {
						sender.sendMessage(line);
					}
					break;

				case UNKNOWN:
					break;
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
