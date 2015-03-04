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


import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The base of a simple command.
 *
 * <p>
 *     This kind of command is just a “final” command, without any subcommand.
 * </p>
 * <p>
 *     If you want a command with sub-commands automatically managed, extend the
 *     {@link me.azenet.UHPlugin.commands.core.commands.UHComplexCommand} class instead.
 * </p>
 *
 * @version 1.0
 * @author AmauryCarrade
 */
public abstract class UHCommand {

	/**
	 * The parent command.
	 *
	 * <p>
	 *     Example, for <code>/cmd foo bar</code>, the parent command of <code>bar</code>
	 *     is the command <code>foo</code>.
	 * </p>
	 * <p>
	 *     Without parent, <code>null</code>.
	 * </p>
	 */
	private UHCommand parent = null;

	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args The arguments passed to the command.
	 *
	 * @throws CannotExecuteCommandException If the command cannot be executed.
	 */
	public abstract void run(CommandSender sender, String[] args) throws CannotExecuteCommandException;

	/**
	 * Tab-completes this command.
	 *
	 * @param sender The sender.
	 * @param args The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	public abstract List<String> tabComplete(CommandSender sender, String[] args);

	/**
	 * Returns the help of this command.
	 *
	 * <p>
	 *     The first line should describe briefly the command, as this line is displayed as
	 *     a line of the help of the parent command.
	 * </p>
	 * <p>
	 *     The other lines will only be displayed if the {@link CannotExecuteCommandException}
	 *     is caught by the command executor.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	public abstract List<String> help(CommandSender sender);

	/**
	 * Sets the parent command of this command. Can be set only one time.
	 *
	 * @param parent The parent.
	 * @throws IllegalArgumentException If the parent command is already set.
	 */
	public void setParent(UHCommand parent) {
		if(this.parent != null) {
			throw new IllegalArgumentException("The parent command is already set!");
		}

		this.parent = parent;
	}

	/**
	 * Returns the parent command.
	 *
	 * @return The parent; {@code null} if this command is a root one.
	 */
	public UHCommand getParent() {
		return parent;
	}
}
