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
package me.azenet.UHPlugin.commands.core.commands;

import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Represents a command with subcommands.
 *
 * <p>
 *     Add the subcommands with the {@link #registerSubCommand} method in the constructor.<br />
 *     A subcommand is simply an {@link me.azenet.UHPlugin.commands.core.commands.UHCommand} (or
 *     {@link me.azenet.UHPlugin.commands.core.commands.UHComplexCommand}) object.
 * </p>
 *
 * @version 1.0
 * @author Amaury Carrade
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
	 * This will be executed if this command is called without argument,
	 * or if there isn't any sub-command executor registered.
	 *
	 * @param sender The sender.
	 * @param args The arguments passed to the command.
	 */
	public abstract void runRoot(CommandSender sender, String[] args);

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
	 * The result of this method will be added to the tab-complete suggestions for this command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments.
	 *
	 * @return The suggestions to add.
	 */
	public abstract List<String> tabCompleteRoot(CommandSender sender, String[] args);

	/**
	 * Registers a subcommand of this command.
	 * A subcommand can be a complex command.
	 *
	 * @param command The command to register.
	 *
	 * @throws IllegalArgumentException If the command object don't have
	 *                                  the {@link me.azenet.UHPlugin.commands.core.annotations.Command} annotation.
	 */
	public void registerSubCommand(UHCommand command) {
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);

		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without the @Command annotation.");
		}

		command.setParent(this);

		String name = commandAnnotation.name();
		String permission = commandAnnotation.permission();

		if(permission == null && !commandAnnotation.useParentPermission()) {
			permission = commandAnnotation.name();
		}

		if(permission != null && permission.isEmpty()) {
			permission = null;
		}

		if(commandAnnotation.inheritPermission() || commandAnnotation.useParentPermission()) {

			UHCommand parent = this;
			if(commandAnnotation.useParentPermission()) {
				// We starts at the parent to get the parent's permission.
				parent = this.getParent();
			}

			while(parent != null) {
				// The parent will always have the @Command annotation, because it is always
				// added in this method and the presence of the annotation is checked.
				Command parentAnnotation = parent.getClass().getAnnotation(Command.class);
				if(parentAnnotation.permission() != null && !parentAnnotation.permission().isEmpty()) {
					permission = parentAnnotation.permission();
					if(permission != null && !permission.isEmpty()) {
						permission += "." + permission;
					}
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
			runRoot(sender, new String[0]);
		}
		else {
			UHCommand cmd = subcommands.get(args[0]);
			if(cmd != null) {
				// Allowed?
				String permission = permissions.get(args[0]);
				if(permission == null || sender.hasPermission(permission)) {
					cmd.run(sender, CommandUtils.getSubcommandArguments(args));
				}
				else {
					throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
				}
			}
			else {
				runRoot(sender, CommandUtils.getSubcommandArguments(args));
			}
		}
	}

	/**
	 * Autocompletes this command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// Autocompletion for this command
		if(args.length == 1) {
			List<String> suggestions = new LinkedList<>();

			for (String command : subcommands.keySet()) {
				if(sender.hasPermission(permissions.get(command))) {
					suggestions.add(command);
				}
			}

			suggestions.addAll(tabCompleteRoot(sender, args));

			return suggestions;
		}

		// Autocompletion for a subcommand
		else {
			UHCommand subcommand = subcommands.get(args[0]);
			if(subcommand != null) {
				return subcommand.tabComplete(sender, CommandUtils.getSubcommandArguments(args));
			}
			else {
				return tabCompleteRoot(sender, args);
			}
		}
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
