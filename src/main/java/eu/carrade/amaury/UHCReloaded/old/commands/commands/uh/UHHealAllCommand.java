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
import eu.carrade.amaury.UHCReloaded.old.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * This command feeds a player.
 * <p>
 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
 */
@Command (name = "healall")
public class UHHealAllCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHHealAllCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        double diffHealth = 0D;
        double health = 0D;
        boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode


        if (args.length == 0) // /uh healall : full life for all players.
        {
            diffHealth = 20D;
        }
        else // /uh heal <player> <hearts>
        {
            try
            {
                if (args[0].startsWith("+"))
                {
                    diffHealth = Double.parseDouble(args[0].substring(1));
                    add = true;
                }
                else if (args[0].startsWith("-"))
                {
                    diffHealth = -1 * Double.parseDouble(args[0].substring(1));
                    add = true;
                }
                else
                {
                    diffHealth = Double.parseDouble(args[0]);
                }
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(I.t("{ce}Hey, this is not a number of half-hearts. It's a text. Pfff."));
                return;
            }
        }

        if ((!add && diffHealth <= 0) || diffHealth <= -20)
        {
            sender.sendMessage(I.t("{ce}Serial killer!"));
            return;
        }


        for (final Player player : p.getServer().getOnlinePlayers())
        {
            health = !add ? diffHealth : player.getHealth() + diffHealth;

            if (health <= 0D)
            {
                sender.sendMessage(I.t("{ce}The health of {0} was not updated to avoid a kill.", player.getName()));
                continue;
            }
            else if (health > 20D)
            {
                health = 20D;
            }

            player.setHealth(health);
            UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId()).updateHealth(health);
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
        return Collections.singletonList(I.t("{cc}/uh healall [half-hearts=20|±diff] {ci}: heals all players instead of only one."));
    }

    @Override
    public String getCategory()
    {
        return Category.BUGS.getTitle();
    }
}
