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
package me.azenet.UHPlugin.commands.core;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.commands.core.commands.UHCommand;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractCommandExecutor implements TabExecutor {

	private UHPlugin p;
	private I18n i;

	/**
	 * Stores the main commands, i.e. the commands registered in the {@code plugin.yml} file.
	 */
	private Map<String, UHCommand> mainCommands = new HashMap<>();

	/**
	 * Stores the base permissions of these commands.
	 */
	private Map<String, String> mainCommandsPermissions = new HashMap<>();

	public AbstractCommandExecutor(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();
	}

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
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);
		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without @Command annotation.");
		}

		mainCommands.put(commandAnnotation.name(), command);

		String permission = commandAnnotation.permission();
		if((permission != null && permission.isEmpty()) || commandAnnotation.useParentPermission()) {
			permission = null;
		}
		mainCommandsPermissions.put(commandAnnotation.name(), permission);
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		UHCommand uhCommand = mainCommands.get(command.getName());
		if(uhCommand == null) {
			return false;
		}
		try {
			String permission = mainCommandsPermissions.get(command.getName());
			if(permission != null && !sender.hasPermission(permission)) {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
			}

			uhCommand.run(sender, CommandUtils.getSubcommandArguments(args));

		} catch(CannotExecuteCommandException e) {
			switch(e.getReason()) {
				case NOT_ALLOWED:
					sender.sendMessage(i.t("cmd.errorUnauthorized"));
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
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		return null;
	}

}
