/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Command (name = "tpspawn")
public class UHTPSpawnCommand extends AbstractCommand
{
    private final UHCReloaded p;
    private final I18n i;

    public UHTPSpawnCommand(UHCReloaded plugin)
    {
        p = plugin;
        i = p.getI18n();
    }


    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        // Spawns not assigned
        if (p.getGameManager().getTeleporter() == null)
        {
            sender.sendMessage(i.t("tpspawn.notAssigned"));
            return;
        }


        if (args.length < 1)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        Player player = p.getServer().getPlayer(args[0]);
        if (player == null || !player.isOnline())
        {
            sender.sendMessage(i.t("tpspawn.offline", args[0]));
            return;
        }

        Location spawnLocation = p.getGameManager().getTeleporter().getSpawnForPlayer(player.getUniqueId());

        if (spawnLocation == null)
        {
            sender.sendMessage(i.t("tpspawn.noSpawnLocation", args[0]));
            return;
        }


        if (args.length >= 2 && args[1].equalsIgnoreCase("force"))
        {
            p.getGameManager().getTeleporter().teleportPlayer(player.getUniqueId(), true);
            sender.sendMessage(i.t("tpspawn.teleported", args[0]));
        }
        else if (UHUtils.safeTP(player, spawnLocation))
        {
            sender.sendMessage(i.t("tpspawn.teleported", args[0]));
        }
        else
        {
            sender.sendMessage(i.t("tpspawn.notTeleportedNoSafeSpot", args[0]));
            sender.sendMessage(i.t("tpspawn.notTeleportedNoSafeSpotCmd", args[0]));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        // Spawns not attributed
        if (p.getGameManager().getTeleporter() == null)
            return null;


        if (args.length == 1)
        {
            List<String> suggestions = new ArrayList<>();

            for (Player player : p.getServer().getOnlinePlayers())
            {
                if (p.getGameManager().getTeleporter().hasSpawnForPlayer(player.getUniqueId()))
                {
                    suggestions.add(player.getName());
                }
            }

            return CommandUtils.getAutocompleteSuggestions(args[0], suggestions);
        }

        else if (args.length == 2)
        {
            return CommandUtils.getAutocompleteSuggestions(args[1], Collections.singletonList("force"));
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
        return Collections.singletonList(i.t("cmd.helpTpspawn"));
    }
}
