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
package eu.carrade.amaury.UHCReloaded.old.commands.commands.uh.border;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.borders.MapShape;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.old.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

@Command (name = "set")
public class UHBorderSetCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHBorderSetCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        // /uh border set
        if (args.length == 0)
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }

        // /uh border set <?>
        else if (args.length == 1)
        {
            try
            {
                final int newDiameter = Integer.valueOf(args[0]);

                // Some players are outside
                if (p.getBorderManager().getPlayersOutside(newDiameter).size() != 0)
                {
                    sender.sendMessage(I.t("{ce}Some players are outside the future border, so this operation was cancelled."));
                    sender.sendMessage(I.t("{ci}Use {cc}/uh border set {0} force{ci} to resize the border regardless to this point.", args[0]));

                    if (!p.getWorldBorderIntegration().isWBIntegrationEnabled())
                    {
                        sender.sendMessage(I.t("{ce}WARNING: {ci}because WorldBorder is not installed, players out of the border will not be teleported!"));
                    }

                    p.getBorderManager().sendCheckMessage(sender, newDiameter);
                }
                else
                {
                    p.getBorderManager().setCurrentBorderDiameter(newDiameter);

                    if (p.getBorderManager().getMapShape() == MapShape.CIRCULAR)
                    {
                        p.getServer().broadcastMessage(I.tn("{lightpurple}The diameter of the map is now {0} block.", "{lightpurple}The diameter of the map is now {0} blocks.", newDiameter));
                    }
                    else
                    {
                        p.getServer().broadcastMessage(I.t("{lightpurple}The size of the map is now {0}×{0}.", newDiameter));
                    }
                }
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(I.t("{ce}“{0}” is not a number...", args[0]));
            }
        }

        // /uh border set <?> force
        else if (args.length == 2 && args[1].equalsIgnoreCase("force"))
        {
            try
            {
                Integer newDiameter = Integer.valueOf(args[0]);

                p.getBorderManager().setCurrentBorderDiameter(newDiameter);

                if (p.getBorderManager().getMapShape() == MapShape.CIRCULAR)
                {
                    p.getServer().broadcastMessage(I.tn("{lightpurple}The diameter of the map is now {0} block.", "{lightpurple}The diameter of the map is now {0} blocks.", newDiameter));
                }
                else
                {
                    p.getServer().broadcastMessage(I.t("{lightpurple}The size of the map is now {0}×{0}.", newDiameter));
                }
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(I.t("{ce}“{0}” is not a number...", args[0]));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        if (args.length == 2)
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
        return Collections.singletonList(I.t("{cc}/uh border set <diameter> [force]{ci}: changes the size of the map. If force is not given, the operation will be canceled if there is a player outside the border."));
    }
}
