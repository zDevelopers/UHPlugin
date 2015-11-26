/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Command (name = "list")
public class UHTimersListCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHTimersListCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        Collection<UHTimer> timers = p.getTimerManager().getTimers();

        sender.sendMessage(i.t("timers.list.count", String.valueOf(timers.size())));

        for (UHTimer timer : timers)
        {
            if (timer.isRunning())
            {
                if (timer.isPaused())
                {
                    sender.sendMessage(i.t("timers.list.itemPaused",
                            timer.getDisplayName(),
                            String.valueOf(timer.getDuration()),
                            timer.toString()
                    ));
                }
                else
                {
                    sender.sendMessage(i.t("timers.list.itemRunning",
                            timer.getDisplayName(),
                            String.valueOf(timer.getDuration()),
                            timer.toString()
                    ));
                }
            }
            else
            {
                sender.sendMessage(i.t("timers.list.itemStopped",
                        timer.getDisplayName(),
                        String.valueOf(timer.getDuration())));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        List<String> suggestions = new ArrayList<>();

        for (UHTimer timer : p.getTimerManager().getTimers())
        {
            suggestions.add(timer.getName());
        }

        return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 0), suggestions, args.length - 1);
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.timersHelpList"));
    }
}
