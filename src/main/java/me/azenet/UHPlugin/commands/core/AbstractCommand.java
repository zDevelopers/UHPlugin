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

import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.command.CommandSender;

import java.util.*;


/**
 * Represents a command with subcommands (or not).
 *
 * <p>
 *     Add the subcommands with the {@link #registerSubCommand} method in the constructor.<br />
 *     A subcommand is simply an {@link AbstractCommand} object.
 * </p>
 *
 * @version 1.0
 * @author Amaury Carrade
 */
public abstract class AbstractCommand {

	/**
	 * Stores the sub-commands of this complex command.
	 *
	 * <p>name → command.</p>
	 */
	private Map<String, AbstractCommand> subcommands = new LinkedHashMap<>();

	/**
	 * Stores the permissions of the sub-commands.
	 *
	 * <p>name → permission.</p>
	 */
	private Map<String, String> permissions = new LinkedHashMap<>();

	/**
	 * Stores the sub-commands per category.
	 *
	 * <p>name → category.</p>
	 */
	private Map<String, String> subcommandsCategories = new HashMap<>();

	/**
	 * The parent command.
	 *
	 * <p>
	 *     Example, for {@code /cmd foo bar}, the parent command of {@code bar}
	 *     is the command {@code foo}.
	 * </p>
	 * <p>
	 *     Without parent (root command), {@code null}.
	 * </p>
	 */
	private AbstractCommand parent = null;


	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 *
	 * @throws CannotExecuteCommandException If the command cannot be executed.
	 */
	public abstract void run(CommandSender sender, String[] args) throws CannotExecuteCommandException;

	/**
	 * Tab-completes this command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	public abstract List<String> tabComplete(CommandSender sender, String[] args);

	/**
	 * Returns the help of this command.
	 *
	 * <p>
	 *     These lines will only be displayed if the {@link CannotExecuteCommandException} is
	 *     caught by the command executor, with the reasons {@code BAD_USE} or {@code NEED_DOC}.
	 * </p>
	 * <p>
	 *     If this returns null, fallsback to {@link #onListHelp}.
	 * </p>
	 *
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	public abstract List<String> help(CommandSender sender);

	/**
	 * Returns the help displayed in the list of the commands, in the help
	 * of the parent command.
	 *
	 * <p>
	 *     You should return one single line here, except for special cases.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line displayed per entry in the list.
	 */
	public abstract List<String> onListHelp(CommandSender sender);

	/**
	 * Returns the title of the category of the command.
	 *
	 * <p>
	 *     This category will be displayed as a title in the commands' list.<br />
	 *     If the value defined is empty, or null, or the method not overwritten,
	 *     the command will be not categorized.
	 * </p>
	 *
	 * <p>
	 *     This category must be unique in the sub-commands of a command.
	 * </p>
	 *
	 * <p>
	 *     You should either use a category for all subcommands of a command, either no categories
	 *     at all for these commands. Else, the non-categorized commands will be displayed somewhere,
	 *     and this place may vary.
	 * </p>
	 */
	public String getCategory() {
		return null;
	}


	/**
	 * Sets the parent command of this command. Can be set only one time.
	 *
	 * @param parent The parent.
	 *
	 * @throws IllegalArgumentException If the parent command is already set.
	 */
	public void setParent(AbstractCommand parent) {
		if (this.parent != null) {
			throw new IllegalArgumentException("The parent command is already set!");
		}

		this.parent = parent;
	}

	/**
	 * Returns the parent command.
	 *
	 * @return The parent; {@code null} if this command is a root one.
	 */
	public AbstractCommand getParent() {
		return parent;
	}


	/**
	 * Registers a subcommand of this command.
	 * A subcommand can be a complex command.
	 *
	 * @param command The command to register.
	 *
	 * @throws IllegalArgumentException If the command object don't have the {@link Command} annotation.
	 */
	public void registerSubCommand(AbstractCommand command) {
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);

		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without @Command annotation. Class: " + command.getClass().getCanonicalName() + ".");
		}

		command.setParent(this);

		String name = commandAnnotation.name();
		String permission = commandAnnotation.permission();

		if(permission == null && !commandAnnotation.useParentPermission()) {
			permission = commandAnnotation.name();
		}

		if(permission != null && permission.isEmpty() || commandAnnotation.noPermission()) {
			permission = null;
		}

		if(commandAnnotation.inheritPermission() || commandAnnotation.useParentPermission()) {

			AbstractCommand parent = this;
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

		// Categories
		if(command.getCategory() != null && !command.getCategory().isEmpty()) {
			subcommandsCategories.put(name, command.getCategory());
		}
	}

	/**
	 * Routes the command, to a sub command, with a fallback to the
	 * {@link #run} method of this command if no subcommand matches
	 * or if there isn't any argument passed to this command.
	 *
	 * <p>
	 *     Internal use. Do not override this. Ignore this.
	 * </p>
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 */
	public void routeCommand(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0) {
			run(sender, new String[0]);
		}
		else {
			AbstractCommand cmd = subcommands.get(args[0]);
			if(cmd != null) {
				// Allowed?
				String permission = permissions.get(args[0]);
				if(permission == null || sender.hasPermission(permission)) {
					cmd.routeCommand(sender, CommandUtils.getSubcommandArguments(args));
				} else {
					throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
				}
			}
			else {
				run(sender, args);
			}
		}
	}

	/**
	 * Routes to the autocompleter of this command.
	 *
	 * <p>
	 *     Internal use. Do not override this. Ignore this.
	 * </p>
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	public List<String> routeTabComplete(CommandSender sender, String[] args) {
		// Autocompletion for this command
		if(args.length == 1) {
			List<String> suggestions = new LinkedList<>();

			for (String command : subcommands.keySet()) {
				String permission = permissions.get(command);
				if(permission == null || sender.hasPermission(permission)) {
					suggestions.add(command);
				}
			}

			suggestions = CommandUtils.getAutocompleteSuggestions(args[0], suggestions);

			List<String> suggestionsFromThisCommand = tabComplete(sender, args);
			if(suggestionsFromThisCommand != null) {
				suggestions.addAll(suggestionsFromThisCommand);
			}

			return suggestions;
		}

		// Autocompletion for a subcommand
		else {
			AbstractCommand subcommand = subcommands.get(args[0]);
			if(subcommand != null) {
				return subcommand.routeTabComplete(sender, CommandUtils.getSubcommandArguments(args));
			}
			else {
				return tabComplete(sender, args);
			}
		}
	}

	/**
	 * Returns the subcommands.
	 *
	 * <p>
	 *     Map: name of the command → UHCommand object.
	 * </p>
	 *
	 * @return the subcommands.
	 */
	public Map<String, AbstractCommand> getSubcommands() {
		return subcommands;
	}

	/**
	 * Returns the permissions of the subcommands.
	 *
	 * <p>
	 *     Map: name of the command → raw permission of this command.
	 * </p>
	 *
	 * @return the permissions of the subcommands.
	 */
	public Map<String, String> getSubcommandsPermissions() {
		return permissions;
	}

	/**
	 * Returns true if this command has subcommands.
	 */
	public boolean hasSubCommands() {
		return subcommands.size() > 0;
	}

	/**
	 * Returns the categories of the subcommands.
	 *
	 * <p>
	 *     Map: name of the command → title of the category of this command.
	 * </p>
	 */
	public Map<String, String> getSubcommandsCategories() {
		return subcommandsCategories;
	}
}
