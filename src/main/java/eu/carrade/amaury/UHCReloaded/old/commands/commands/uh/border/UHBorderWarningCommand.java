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
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.old.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command (name = "warning")
public class UHBorderWarningCommand extends AbstractCommand
{
    private final Integer WARNING_INTERVAL;

    private final UHCReloaded p;


    public UHBorderWarningCommand(UHCReloaded p)
    {
        this.p = p;

        WARNING_INTERVAL = UHConfig.MAP.BORDER.WARNING_INTERVAL.get();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
        { // /uh border warning
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
        }
        else if (args[0].equalsIgnoreCase("cancel"))
        { // /uh border warning cancel
            p.getBorderManager().cancelWarning();
            sender.sendMessage(I.t("{cs}Warning canceled."));
        }
        else
        { // /uh border warning <?>
            try
            {
                int warnDiameter = Integer.parseInt(args[0]);
                int warnTime = 0;

                // /uh border warning <?> <?>
                if (args.length >= 4)
                {
                    warnTime = Integer.parseInt(args[1]);
                }

                p.getBorderManager().setWarningSize(warnDiameter, warnTime, sender);
                sender.sendMessage(I.tn("{cs}Future size saved. All players outside this future border will be warned every {0} second.", "{cs}Future size saved. All players outside this future border will be warned every {0} seconds.", WARNING_INTERVAL));

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
        if (args.length == 1)
        {
            return CommandUtils.getAutocompleteSuggestions(args[0], Collections.singletonList("cancel"));
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
        return Arrays.asList(I.t("{cc}/uh border warning <futureDiameter> [minutesBeforeReduction]{ci}: warns all players outside the given future diameter. It's just a notice, nothing else."), I.t("{cc}/uh border warning cancel{ci}: cancels a previously-set warning."));
    }
}
