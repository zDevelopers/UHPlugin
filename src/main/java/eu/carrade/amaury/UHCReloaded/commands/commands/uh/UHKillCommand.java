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
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This command marks a player as dead, even if he is offline.
 *
 * If the player is online, this has the same effect as {@code /kill}.
 *
 * Usage: /uh kill &lt;player>
 */
@Command (name = "kill")
public class UHKillCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHKillCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length < 1)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        OfflinePlayer player = p.getServer().getOfflinePlayer(args[0]);

        if (player == null)
        {
            sender.sendMessage(I.t("{ce}This player was never seen on this server."));
            return;
        }

        if (!p.getGameManager().isPlayerDead(player.getUniqueId()))
        {
            if (player.isOnline())
            {
                ((Player) player).setHealth(0);
            }
            else
            {
                p.getGameManager().addDead(player.getUniqueId());
            }

            sender.sendMessage(I.t("{cs}The player {0} is now marked as dead.", player.getName()));
        }
        else
        {
            sender.sendMessage(I.t("{ce}{0} is not an alive player.", player.getName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            List<String> suggestions = new ArrayList<>();

            for (OfflinePlayer player : p.getGameManager().getAlivePlayers())
            {
                suggestions.add(player.getName());
            }

            return CommandUtils.getAutocompleteSuggestions(args[0], suggestions);
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
        return Collections.singletonList(I.t("{cc}/uh kill <player> {ci}: mark a player as dead, even if he is offline."));
    }
}
