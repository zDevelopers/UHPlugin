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
import me.azenet.UHPlugin.commands.commands.uh.*;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.List;

@Command(name = "uh")
public class UHRootCommand extends AbstractCommand {

	private UHPlugin p;
	private I18n i;

	public UHRootCommand(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();

		// Game
		registerSubCommand(new UHStartCommand(p));
		registerSubCommand(new UHShiftCommand(p));
		registerSubCommand(new UHSpawnsCommand(p));
		registerSubCommand(new UHTeamCommand(p));
		registerSubCommand(new UHBorderCommand(p));
		registerSubCommand(new UHSpectatorsCommand(p));
		registerSubCommand(new UHGenerateWallsCommand(p));

		// Bugs
		registerSubCommand(new UHHealCommand(p));
		registerSubCommand(new UHHealAllCommand(p));
		registerSubCommand(new UHFeedCommand(p));
		registerSubCommand(new UHFeedAllCommand(p));
		registerSubCommand(new UHKillCommand(p));
		registerSubCommand(new UHResurrectCommand(p));
		registerSubCommand(new UHTPBackCommand(p));

		// Misc
		registerSubCommand(new UHFinishCommand(p));
		registerSubCommand(new UHFreezeCommand(p));
		registerSubCommand(new UHTimersCommand(p));
		registerSubCommand(new UHInfosCommand(p));
		registerSubCommand(new UHAboutCommand(p));
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
