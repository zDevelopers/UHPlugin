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
package eu.carrade.amaury.UHCReloaded.modules.other.rules;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.CommandUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleInfo (
        name = "Rules",
        description = "Displays configured game rules when the game start or the " +
                "players join",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.UTILITIES,
        icon = Material.BOOKSHELF,
        settings = Config.class
)
public class RulesModule extends UHModule
{
    private final List<String> rules = new ArrayList<>();

    @Override
    public void onEnable()
    {
        if (Config.RULES.isDefined() && !Config.RULES.isEmpty() )
        {
            // We check if the list is non-empty, i.e. if there is at least a non-empty rule.
            boolean empty = true;

            for (final String rule : Config.RULES)
            {
                if (rule == null) continue;

                rules.add(ChatColor.translateAlternateColorCodes('&',rule.trim()));

                if (!rule.isEmpty())
                    empty = false;
            }

            // If the list is empty, no rules are displayed. We reset the list.
            if (empty) rules.clear();
        }
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(RulesCommand.class);
    }

    /**
     * @return {@code true} if the rules system is enabled
     */
    public boolean hasRules()
    {
        return rules.size() != 0;
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
        Bukkit.getOnlinePlayers().forEach(this::displayRulesTo);
        displayRulesTo(Bukkit.getConsoleSender());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent ev)
    {
        if (UR.game().getPhase() == GamePhase.WAIT && hasRules() && Config.DISPLAY.ON_JOIN.get())
        {
            RunTask.later(() -> { if (ev.getPlayer().isOnline()) displayRulesTo(ev.getPlayer()); }, 100L);
        }
    }

    @EventHandler
    public void onGameStart(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.IN_GAME && ev.isRunningForward() && hasRules() && Config.DISPLAY.ON_START.get())
        {
            RunTask.later(this::broadcastRules, 200L);
        }
    }
}
