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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.spawns;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "remove")
public class UHSpawnsRemoveCommand extends AbstractCommand
{

    UHCReloaded p;
    I18n i;

    public UHSpawnsRemoveCommand(UHCReloaded plugin)
    {
        p = plugin;
        i = plugin.getI18n();
    }


    /**
     * Runs the command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments passed to the command.
     *
     * @throws eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
     */
    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
        { // /uh spawns remove
            if (!(sender instanceof Player))
            {
                throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
            }
            else
            {
                Player pl = (Player) sender; // Just a way to avoid casts everywhere.
                p.getSpawnsManager().removeSpawnPoint(pl.getLocation(), false);
                sender.sendMessage(i.t("spawns.remove.removed", pl.getWorld().getName(), String.valueOf(pl.getLocation().getBlockX()), String.valueOf(pl.getLocation().getBlockZ())));
            }
        }
        else if (args.length == 1)
        { // /uh spawns add <x>: Two coordinates needed!
            sender.sendMessage(i.t("spawns.error2Coords"));
        }
        else
        { // /uh spawns remove <x> <z>
            try
            {
                World world;
                if (sender instanceof Player)
                {
                    world = ((Player) sender).getWorld();
                }
                else
                {
                    world = p.getServer().getWorlds().get(0);
                }

                p.getSpawnsManager().removeSpawnPoint(new Location(world, Double.parseDouble(args[2]), 0, Double.parseDouble(args[3])), true);
                sender.sendMessage(i.t("spawns.remove.removed", p.getServer().getWorlds().get(0).getName(), args[2], args[3]));
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(i.t("spawns.NaN"));
            }
        }
    }

    /**
     * Tab-completes this command.
     *
     * @param sender The sender.
     * @param args   The arguments passed to the command.
     *
     * @return A list of suggestions.
     */
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
        return Collections.singletonList(i.t("cmd.spawnsHelpRemove"));
    }
}
