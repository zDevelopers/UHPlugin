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
package me.azenet.UHPlugin.commands.commands.uh;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.commands.categories.Category;
import me.azenet.UHPlugin.commands.commands.uh.timers.*;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;


/**
 * This command manages timers.
 *
 * Usage: /uh timers < add | set | display | hide | start | pause | resume | stop | remove | list >
 */
@Command(name = "timers")
public class UHTimersCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHTimersCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();

		registerSubCommand(new UHTimersAddCommand(p));
		registerSubCommand(new UHTimersSetCommand(p));
		registerSubCommand(new UHTimersDisplayCommand(p));
		registerSubCommand(new UHTimersHideCommand(p));
		registerSubCommand(new UHTimersStartCommand(p));
		registerSubCommand(new UHTimersPauseCommand(p));
		registerSubCommand(new UHTimersResumeCommand(p));
		registerSubCommand(new UHTimersStopCommand(p));
		registerSubCommand(new UHTimersRemoveCommand(p));
		registerSubCommand(new UHTimersListCommand(p));
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(i.t("cmd.timersHelpTitle"));
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpTimers"));
	}

	@Override
	public String getCategory() {
		return Category.MISC.getTitle();
	}
}
