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
package me.azenet.UHPlugin.commands.commands;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.commands.UHComplexCommand;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.List;

@Command(name = "uh", permission = "uh")
public class UH extends UHComplexCommand {

	private UHPlugin p;
	private I18n i;

	public UH(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();
	}

	/**
	 * This will be executed if this command is called without argument.
	 *
	 * @param sender The sender.
	 */
	@Override
	public void runRoot(CommandSender sender, String[] args) {
		sender.sendMessage("Hi");
	}

	/**
	 * Returns the general help for this command.
	 * <p/>
	 * <p>
	 * This help should be a one-line help, as it's displayed as the help
	 * for the parent commands.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help, one line per entry in the list.
	 */
	@Override
	public List<String> helpRoot(CommandSender sender) {
		return null;
	}
}
