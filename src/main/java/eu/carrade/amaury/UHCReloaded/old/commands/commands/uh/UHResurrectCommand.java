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
package eu.carrade.amaury.UHCReloaded.old.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.old.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This command resurrects a player.
 *
 * Usage: /uh resurrect <player>
 */
@Command (name = "resurrect")
public class UHResurrectCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHResurrectCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length != 1)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        boolean success = p.getGameManager().resurrect(args[0]);

        Player player = p.getServer().getPlayer(args[0]);
        if (player == null || !player.isOnline())
        {
            if (!success)  // Player does not exists or is nod dead.
            {
                sender.sendMessage(I.t("{ce}This player is not playing or dead!"));
            }
            else  // Resurrected
            {
                sender.sendMessage(I.t("{cs}Because {0} is offline, he will be resurrected when he logins. If he was, he is no longer banned.", args[0]));
            }
        }
        else
        {
            if (!success)  // The player is not dead
            {
                /// Trying to resurrect an alive player
                sender.sendMessage(I.t("{ce}{0} is not dead!", args[0]));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            List<String> suggestions = new ArrayList<>();

            // TODO can be optimized
            for (String playerName : p.getGameManager().getPlayers())
            {
                OfflinePlayer player = p.getServer().getOfflinePlayer(playerName);
                if (player != null && p.getGameManager().isPlayerDead(player.getUniqueId()))
                {
                    suggestions.add(playerName);
                }
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
        return Collections.singletonList(I.t("{cc}/uh resurrect <player> {ci}: resurrects a player."));
    }
}
