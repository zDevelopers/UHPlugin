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
import eu.carrade.amaury.UHCReloaded.UHGameManager;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


/**
 * This commands broadcast the winner(s) of the game and sends some fireworks at these players.
 * It fails if there is more than one team alive.
 *
 * Usage: /uh finish
 */
@Command (name = "finish")
public class UHFinishCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHFinishCommand(UHCReloaded plugin)
    {
        this.p = plugin;
        this.i = plugin.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        try
        {
            p.getGameManager().finishGame();

        }
        catch (IllegalStateException e)
        {

            if (e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_STARTED))
            {
                sender.sendMessage(i.t("finish.notStarted"));
            }
            else if (e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_FINISHED))
            {
                sender.sendMessage(i.t("finish.notFinished"));
            }
            else
            {
                throw e;
            }
        }
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
        return Collections.singletonList(i.t("cmd.helpFinish"));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
