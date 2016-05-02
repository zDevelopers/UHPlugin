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
package eu.carrade.amaury.UHCReloaded.misc;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class RulesManager
{
    private final boolean DISPLAY_ON_JOIN;
    private final boolean DISPLAY_ON_START;

    private final List<String> rules = new ArrayList<>();


    public RulesManager()
    {
        if (UHConfig.RULES.isDefined() && UHConfig.RULES.RULES.get() != null)
        {
            DISPLAY_ON_JOIN  = UHConfig.RULES.DISPLAY.ON_JOIN.get();
            DISPLAY_ON_START = UHConfig.RULES.DISPLAY.ON_START.get();


            // We check if the list is non-empty, i.e. if there is at least a non-empty rule.
            boolean empty = true;

            for (String rule : UHConfig.RULES.RULES.get())
            {
                if (rule == null) continue;

                rule = rule.trim();
                rules.add(ChatColor.translateAlternateColorCodes('&',rule));

                if (!rule.isEmpty())
                    empty = false;
            }

            // If the list is empty, no rules are displayed. We reset the list.
            if (empty) rules.clear();
        }
        else
        {
            DISPLAY_ON_JOIN  = false;
            DISPLAY_ON_START = true;
        }
    }

    /**
     * @return {@code true} if the rules system is enabled
     */
    public boolean isEnabled()
    {
        return rules.size() != 0;
    }

    /**
     * @return {@code true} if the rules have to be displayed to every joining player.
     */
    public boolean displayOnJoin()
    {
        return isEnabled() && DISPLAY_ON_JOIN;
    }

    /**
     * @return {@code true} if the rules have to be displayed when the game starts.
     */
    public boolean displayOnStart()
    {
        return isEnabled() && DISPLAY_ON_START;
    }


    /**
     * Displays the rules to the given receiver.
     *
     * @param receiver The receiver.
     */
    public void displayRulesTo(CommandSender receiver)
    {
        CommandUtils.displaySeparator(receiver);
        /// Title of the rules box.
        receiver.sendMessage(I.t("{red}{bold}Rules and informations"));

        for (String rule : rules)
        {
            if (rule.isEmpty())
            {
                receiver.sendMessage("");
            }
            else
            {
                /// Rule item in the rule box.
                receiver.sendMessage(I.t("{darkgray}- {reset}{0}", rule));
            }
        }

        CommandUtils.displaySeparator(receiver);
    }

    /**
     * Broadcasts the rules to the whole server.
     */
    public void broadcastRules()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            displayRulesTo(player);
        }

        displayRulesTo(Bukkit.getConsoleSender());
    }
}
