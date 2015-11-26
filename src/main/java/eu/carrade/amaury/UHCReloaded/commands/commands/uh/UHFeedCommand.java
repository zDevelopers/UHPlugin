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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;


/**
 * This command feeds a player.
 *
 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
 */
@Command (name = "feed")
public class UHFeedCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHFeedCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length < 1)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        Player target = p.getServer().getPlayer(args[0]);
        if (target == null || !target.isOnline())
        {
            sender.sendMessage(i.t("feed.offline"));
            return;
        }

        int foodLevel = 20;
        float saturation = 20f;

        if (args.length > 1) // /uh feed <player> <foodLevel>
        {
            try
            {
                foodLevel = Integer.valueOf(args[1]);
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(i.t("feed.errorNaN"));
                return;
            }

            if (args.length > 2) // /uh feed <player> <foodLevel> <saturation>
            {
                try
                {
                    // The saturation value cannot be more than the food level.
                    saturation = Math.min(foodLevel, Float.valueOf(args[2]));
                }
                catch (NumberFormatException e)
                {
                    sender.sendMessage(i.t("feed.errorNaN"));
                    return;
                }
            }
        }

        target.setFoodLevel(foodLevel);
        target.setSaturation(saturation);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.helpFeed"));
    }

    @Override
    public String getCategory()
    {
        return Category.BUGS.getTitle();
    }
}
