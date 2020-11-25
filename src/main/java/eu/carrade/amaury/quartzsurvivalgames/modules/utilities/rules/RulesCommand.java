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
package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.rules;

import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@CommandInfo (name = "rules", usageParameters = "[player]")
public class RulesCommand extends Command
{
    public void run() throws CommandException
    {
        if (!QSG.module(RulesModule.class).hasRules())
        {
            error(I.t("{ce}No rules are set in the config file."));
        }

        if (args.length >= 1)
        {
            final Optional<? extends Player> player = Bukkit.getOnlinePlayers().stream()
                    .filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(args[0].trim()))
                    .findAny();

            if (player.isPresent())
            {
                QSG.module(RulesModule.class).displayRulesTo(player.get());

                if (!sender.equals(player.get()))
                    sender.sendMessage(I.t("{cs}Rules sent to {0}.", player.get().getName()));
            }
            else
            {
                sender.sendMessage(I.t("{ce}Cannot display the rules to {0} because he (or she) is offline.", args[0]));
            }
        }
        else
        {
            QSG.module(RulesModule.class).broadcastRules();
        }
    }

    @Override
    public List<String> complete()
    {
        if (args.length == 1) return getMatchingPlayerNames(args[0]);
        else return null;
    }
}
