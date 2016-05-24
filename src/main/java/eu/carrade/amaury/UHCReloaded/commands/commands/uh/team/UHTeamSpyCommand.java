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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.team;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;


@Command (name = "spy")
public class UHTeamSpyCommand extends AbstractCommand
{
    private final UHCReloaded p;

    public UHTeamSpyCommand(UHCReloaded plugin)
    {
        p = plugin;
    }


    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        Player target;

        if (args.length >= 1)
        {
            if (sender.hasPermission("uh.team.spy.others"))
            {
                target = Bukkit.getPlayer(args[0]);
                if (target == null)
                {
                    sender.sendMessage(I.t("{ce}Cannot toggle the spy mode of {0} because they are offline.", args[0]));
                    return;
                }
            }
            else
            {
                throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED, this);
            }
        }
        else
        {
            if (!(sender instanceof Player))
            {
                throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER, this);
            }

            target = (Player) sender;
        }


        String message;

        if (p.getTeamChatManager().isGlobalSpy(target.getUniqueId()))
        {
            p.getTeamChatManager().removeGlobalSpy(target.getUniqueId());
            message = I.t("{cs}Spy mode {darkred}disabled{cs} for {0}.", target.getName());
        }
        else
        {
            p.getTeamChatManager().addGlobalSpy(target.getUniqueId());
            message = I.t("{cs}Spy mode {darkgreen}enabled{cs} for {0}.", target.getName());
        }

        target.sendMessage(message);
        if (!sender.equals(target))
            sender.sendMessage(message);
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
        return Collections.singletonList(I.t("{cc}/uh team spy [player] {ci}: allows yourself (or the target player) to receive all the team chats (read-only). Execute again to stop."));
    }
}
