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
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.*;

/**
 * The base of every command executor.
 *
 * <p>
 *     The command executors needs to extend this class and to register the commands in the constructor
 *     with the method {@link #registerCommand}.
 * </p>
 *
 * @version 1.0
 * @author Amaury Carrade
 */
public abstract class AbstractCommandExecutor implements TabExecutor {

	private UHPlugin p;
	private I18n i;

	/**
	 * Stores the main commands, i.e. the commands registered in the {@code plugin.yml} file.
	 */
	private Map<String, AbstractCommand> mainCommands = new LinkedHashMap<>();

	/**
	 * Stores the base permissions of these commands.
	 */
	private Map<String, String> mainCommandsPermissions = new LinkedHashMap<>();

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
	public void registerCommand(AbstractCommand command) {
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);
		if(commandAnnotation == null) {
			throw new IllegalArgumentException("Cannot register a command without @Command annotation. Class: " + command.getClass().getCanonicalName() + ".");
		}

		mainCommands.put(commandAnnotation.name(), command);

		String permission = commandAnnotation.permission();

		if(commandAnnotation.noPermission()) {
			permission = null;
		}
		else if(permission != null && permission.isEmpty()) {
			if(commandAnnotation.useParentPermission()) {
				permission = null;
			}
			else {
				permission = commandAnnotation.name();
			}
		}

		mainCommandsPermissions.put(commandAnnotation.name(), permission);
	}


	/**
	 * Displays the help of a command.
	 *
	 * <p>
	 *     If the command is a complex command, this will display the help of the complex command,
	 *     first line excepted, ath then the short help of all sub-commands.<br />
	 *     Else, this will display the full help for the command.
	 * </p>
	 *
	 * @param sender The sender.
	 * @param command The command.
	 * @param isAnError {@code true} if this is displayed due to an error.
	 */
	public void displayHelp(CommandSender sender, AbstractCommand command, boolean isAnError) {
		if(command.hasSubCommands()) {
			List<String> help = new LinkedList<>();

			// Root help: all the help defined, first line excepted.
			List<String> rootHelp = command.help(sender);
			if(rootHelp != null && rootHelp.size() >= 1) {
				help.addAll(rootHelp.subList(1, rootHelp.size()));
			}

			// Then, the help of the sub-commands sorted by category.
			// We first organize the commands per-category.
			Map<String, LinkedList<String>> helpPerCategory = new LinkedHashMap<>();

			for(Map.Entry<String, AbstractCommand> subCommand : command.getSubcommands().entrySet()) {
				List<String> subHelp = subCommand.getValue().onListHelp(sender);
				String permission = command.getSubcommandsPermissions().get(subCommand.getKey());
				String category = command.getSubcommandsCategories().get(subCommand.getKey());

				if(category == null) category = "";

				if(subHelp != null && subHelp.size() > 0 && (permission == null || sender.hasPermission(permission))) {

					LinkedList<String> helpForThisCategory = helpPerCategory.get(category);
					if(helpForThisCategory != null) {
						helpForThisCategory.addAll(subHelp);
					}
					else {
						helpForThisCategory = new LinkedList<>();
						helpForThisCategory.addAll(subHelp);
						helpPerCategory.put(category, helpForThisCategory);
					}
				}
			}

			// After, we add to the help to display these commands, with the titles of the
			// categories.
			for(Map.Entry<String, LinkedList<String>> category : helpPerCategory.entrySet()) {
				help.add(category.getKey());
				help.addAll(category.getValue());
			}

			displayHelp(sender, help, isAnError);
		}
		else {
			List<String> help = command.help(sender);
			if(help == null) help = command.onListHelp(sender);

			displayHelp(sender, help, isAnError);
		}
	}

	/**
	 * Displays the help of a command.
	 *
	 * @param sender The sender; this user will receive the help.
	 * @param help The help to display (one line per entry; raw display).
	 * @param isAnError {@code true} if this is displayed due to an error.
	 */
	public void displayHelp(CommandSender sender, List<String> help, boolean isAnError) {
		CommandUtils.displaySeparator(sender);

		if(!isAnError) {
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));
		}

		if(help != null) {
			for (String line : help) {
				if (line != null && !line.isEmpty())
					sender.sendMessage(line);
			}
		}

		CommandUtils.displaySeparator(sender);

		if(isAnError) {
			sender.sendMessage(i.t("cmd.errorBadUse"));
			sender.sendMessage(i.t("cmd.errorBadUseHelpAbove"));
			CommandUtils.displaySeparator(sender);
		}
	}


	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		AbstractCommand abstractCommand = mainCommands.get(command.getName());
		if(abstractCommand == null) {
			return false;
		}

		try {
			String permission = mainCommandsPermissions.get(command.getName());
			if(permission != null && !sender.hasPermission(permission)) {
				throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
			}

			abstractCommand.routeCommand(sender, args);

		} catch(CannotExecuteCommandException e) {
			switch(e.getReason()) {
				case NOT_ALLOWED:
					sender.sendMessage(i.t("cmd.errorUnauthorized"));
					break;

				case ONLY_AS_A_PLAYER:
					sender.sendMessage(i.t("cmd.errorOnlyAsAPlayer"));
					break;

				case BAD_USE:
				case NEED_DOC:
					displayHelp(sender, e.getOrigin() != null ? e.getOrigin() : abstractCommand, e.getReason() == CannotExecuteCommandException.Reason.BAD_USE);
					break;

				case UNKNOWN:
					break;
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		AbstractCommand abstractCommand = mainCommands.get(command.getName());
		return abstractCommand.routeTabComplete(sender, args);
	}

	public Map<String, AbstractCommand> getMainCommands() {
		return mainCommands;
	}
}
