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
package me.azenet.UHPlugin.commands.commands.uh.timers;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.AbstractCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.timers.UHTimer;
import me.azenet.UHPlugin.utils.CommandUtils;
import me.azenet.UHPlugin.utils.UHUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Usage: /uh timers set <duration> <name ...>
 */
@Command(name = "set")
public class UHTimersSetCommand extends AbstractCommand {

	UHPlugin p;
	I18n i;

	public UHTimersSetCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {

		if(args.length < 2) {
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}

		else {
			try {
				Integer duration = UHUtils.string2Time(args[0]);
				String timerName = UHUtils.getStringFromCommandArguments(args, 1);

				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}

				timer.setDuration(duration);
				sender.sendMessage(i.t("timers.set", timer.getDisplayName(), args[0]));

			} catch(IllegalArgumentException e) {
				sender.sendMessage(i.t("timers.durationSyntaxError"));
			}
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if(args.length >= 2) {
			List<String> suggestions = new ArrayList<>();

			for(UHTimer timer : p.getTimerManager().getTimers()) {
				suggestions.add(timer.getName());
			}

			return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 1), suggestions, args.length - 2);
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.timersHelpSet"));
	}
}
