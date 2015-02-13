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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a command with subcommands.
 */
public abstract class UHComplexCommand extends UHCommand {

	/**
	 * Stores the sub-commands of this complex command.
	 */
	private Map<String, UHCommand> subcommands = new HashMap<>();

	/**
	 * Stores the permissions of the sub-commands.
	 */
	private Map<String, String> permissions = new HashMap<>();

	/**
	 * This will be executed if this command is called without argument.
	 *
	 * @param sender The sender.
	 */
	public abstract void runRoot(CommandSender sender);

	/**
	 * Returns the general help for this command.
	 *
	 * <p>
	 *     This help should be a one-line help, as it's displayed as the help
	 *     for the parent commands.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help, one line per entry in the list.
	 */
	public abstract List<String> helpRoot(CommandSender sender);

	/**
	 * Registers a subcommand of this command.
	 * A subcommand can be a complex command.
	 *
	 * @param command The command to register.
	 *
	 * @throws IllegalArgumentException If the command object don't have
	 *                                  the {@code @}{@link Command} annotation.
	 */
	public void registerSubCommand(UHCommand command) {
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);

		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without the @Command annotation.");
		}

		command.setParent(this);

		String name = commandAnnotation.name();
		String permission = commandAnnotation.permission();

		if(commandAnnotation.inheritPermission()) {
			UHCommand parent = this;
			while(parent != null) {
				// The parent will always have the @Command annotation, because it is always
				// added in this method and the presence of the annotation is checked.
				Command parentAnnotation = parent.getClass().getAnnotation(Command.class);
				if(!parentAnnotation.permission().isEmpty()) {
					permission = parentAnnotation.permission() + "." + permission;
				}
				parent = parent.getParent();
			}
		}

		// Let's save these permissions and executors.
		subcommands.put(name, command);
		permissions.put(name, permission);
	}

	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0) {
			runRoot(sender);
		}
		else {
			UHCommand cmd = subcommands.get(args[0]);
			if(cmd != null) {
				// Allowed?
				String permission = permissions.get(args[0]);
				if(sender.hasPermission(permission)) {
					String[] subArgs = Arrays.copyOfRange(args,1, args.length - 1);
					cmd.run(sender, subArgs);
				}
				else {
					throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
				}
			}
		}
	}

	/**
	 * Autocompletes ths command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	@Override
	public List<String> autocomplete(CommandSender sender, String[] args) {
		return null;
	}

	/**
	 * Returns the help of this command.
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}
}
