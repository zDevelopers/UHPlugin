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
	 * @throws me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
	 */
	public abstract void run(CommandSender sender, String[] args) throws CannotExecuteCommandException;

	/**
	 * Auto-completes ths command.
	 *
	 * @param sender The sender.
	 * @param args The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	public abstract List<String> autocomplete(CommandSender sender, String[] args);

	/**
	 * Returns the help of this command.
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
