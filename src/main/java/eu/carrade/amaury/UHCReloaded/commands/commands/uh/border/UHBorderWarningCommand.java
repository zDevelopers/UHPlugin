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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.border;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "warning")
public class UHBorderWarningCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHBorderWarningCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
        { // /uh border warning
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }
        else if (args[0].equalsIgnoreCase("cancel"))
        { // /uh border warning cancel
            p.getBorderManager().cancelWarning();
            sender.sendMessage(i.t("borders.warning.canceled"));
        }
        else
        { // /uh border warning <?>
            try
            {
                Integer warnDiameter = Integer.valueOf(args[0]);

                Integer warnTime = 0;
                if (args.length >= 4)
                { // /uh border warning <?> <?>
                    warnTime = Integer.valueOf(args[1]);
                }

                p.getBorderManager().setWarningSize(warnDiameter, warnTime, sender);
                sender.sendMessage(i.t("borders.warning.set", p.getConfig().getString("map.border.warningInterval", "90")));

            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(i.t("borders.NaN", args[0]));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return CommandUtils.getAutocompleteSuggestions(args[0], Collections.singletonList("cancel"));
        }

        else return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Arrays.asList(i.t("cmd.borderHelpWarning"), i.t("cmd.borderHelpWarningCancel"));
    }
}
