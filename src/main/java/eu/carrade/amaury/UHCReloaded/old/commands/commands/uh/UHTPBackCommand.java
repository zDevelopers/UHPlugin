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
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Command (name = "tpback")
public class UHTPBackCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHTPBackCommand(UHCReloaded p)
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

        Player player = p.getServer().getPlayer(args[0]);
        if (player == null || !player.isOnline())
        {
            sender.sendMessage(I.t("{ce}The player {0} is not online.", args[0]));
            return;
        }
        else if (!p.getGameManager().hasDeathLocation(player))
        {
            sender.sendMessage(I.t("{ce}No death location available for the player {0}.", args[0]));
            return;
        }


        Location deathLocation = p.getGameManager().getDeathLocation(player);

        if (args.length >= 2 && args[1].equalsIgnoreCase("force"))
        {
            UHUtils.safeTP(player, deathLocation, true);
            sender.sendMessage(I.t("{cs}The player {0} was teleported back.", args[0]));
            p.getGameManager().removeDeathLocation(player);
        }
        else if (UHUtils.safeTP(player, deathLocation))
        {
            sender.sendMessage(I.t("{cs}The player {0} was teleported back.", args[0]));
            p.getGameManager().removeDeathLocation(player);
        }
        else
        {
            sender.sendMessage(I.t("{ce}The player {0} was NOT teleported back because no safe spot was found.", args[0]));
            sender.sendMessage(I.t("{ci}Use {cc}/uh tpback {0} force{ci} to teleport the player regardless this point.", args[0]));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            List<String> suggestions = new ArrayList<>();

            for (Player player : p.getServer().getOnlinePlayers())
            {
                if (p.getGameManager().hasDeathLocation(player))
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
        return Collections.singletonList(I.t("{cc}/uh tpback <player> [force] {ci}: safely teleports back a player to his death location."));
    }
}
