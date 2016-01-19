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
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;


/**
 * This command heals a player.
 *
 * Usage: /uh heal <player> <half-hearts>
 */
@Command (name = "heal")
public class UHHealCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHHealCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length < 1 || args.length > 2)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        Player player = p.getServer().getPlayer(args[0]);
        if (player == null || !player.isOnline())
        {
            sender.sendMessage(I.t("heal.offline"));
            return;
        }

        double health = 0D;
        boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode

        if (args.length == 1)
        { // /uh heal <player> : full life for player.
            health = 20D;
        }
        else
        { // /uh heal <player> <hearts>
            double diffHealth;

            try
            {
                if (args[1].startsWith("+"))
                {
                    diffHealth = Double.parseDouble(args[1].substring(1));
                    add = true;
                }
                else if (args[1].startsWith("-"))
                {
                    diffHealth = -1 * Double.parseDouble(args[1].substring(1));
                    add = true;
                }
                else
                {
                    diffHealth = Double.parseDouble(args[1]);
                }
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(I.t("heal.errorNaN"));
                return;
            }

            health = !add ? diffHealth : player.getHealth() + diffHealth;

            if (health <= 0D)
            {
                sender.sendMessage(I.t("heal.errorNoKill"));
                return;
            }
            else if (health > 20D)
            {
                health = 20D;
            }
        }

        player.setHealth(health);
        UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId()).updateHealth(health);
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
        return Collections.singletonList(I.t("cmd.helpHeal"));
    }

    @Override
    public String getCategory()
    {
        return Category.BUGS.getTitle();
    }
}
