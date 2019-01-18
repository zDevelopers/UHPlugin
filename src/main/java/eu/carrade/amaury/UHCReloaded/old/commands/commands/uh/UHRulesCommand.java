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
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UHRulesCommand// extends AbstractCommand
{
    private UHCReloaded p;

    public UHRulesCommand(UHCReloaded plugin)
    {
        p = plugin;
    }

    public void run(CommandSender sender, String[] args)// throws CannotExecuteCommandException
    {
        if (!p.getRulesManager().isEnabled())
        {
            sender.sendMessage(I.t("{ce}No rules are set in the config file."));
            return;
        }

        if (args.length >= 1)
        {
            Player player = Bukkit.getPlayer(args[0]);
            if (player != null)
            {
                p.getRulesManager().displayRulesTo(player);

                if (!sender.equals(player))
                    sender.sendMessage(I.t("{cs}Rules sent to {0}.", player.getName()));
            }
            else
            {
                sender.sendMessage(I.t("{ce}Cannot display the rules to {0} because he (or she) is offline.", args[0]));
            }
        }
        else
        {
            p.getRulesManager().broadcastRules();
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    public List<String> help(CommandSender sender)
    {
        return null;
    }

    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh rules [player] {ci}: sends the server rules to the server or the given player."));
    }
}
