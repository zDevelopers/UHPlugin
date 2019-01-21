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
package eu.carrade.amaury.UHCReloaded.modules.cosmetics.playerListHeaderFooter;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.ListHeaderFooter;
import fr.zcraft.zteams.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;


@ModuleInfo (
        name = "Players List Header & Footer",
        description = "Fills the players list header & footer with any text, that may contains infos related " +
                "to the current game through placeholders. Other modules can add placeholders.",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.COSMETICS,
        icon = Material.ACTIVATOR_RAIL,
        settings = Config.class,
        can_be_loaded_late = false
)
public class PlayerListHeaderFooterModule extends UHModule
{
    private final Map<String, Supplier<String>> placeholderSuppliers = new HashMap<>();

    @Override
    protected void onEnable()
    {
        registerPlaceholder("title", UHConfig.TITLE::get);
        registerPlaceholder("playersText", () -> I.tn("{0} player", "{0} players", UR.game().countAlivePlayers()));
        registerPlaceholder("playersCount", () -> String.valueOf(UR.game().countAlivePlayers()));
        registerPlaceholder("teamsText", () -> I.tn("{0} team", "{0} teams", UR.game().countAliveTeams()));
        registerPlaceholder("teamsCount", () -> String.valueOf(UR.game().countAliveTeams()));

        RunTask.nextTick(this::update);
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(ListPlaceholdersCommand.class);
    }

    /**
     * Registers a new placeholder for player list headers & footers.
     *
     * @param placeholderName The name of the placeholder. The module will lookup for {givenName}
     *                        in the patterns.
     * @param supplier The supplier returning the value to use for this placeholder.
     */
    public void registerPlaceholder(final String placeholderName, final Supplier<String> supplier)
    {
        placeholderSuppliers.put(placeholderName, supplier);
    }

    public Map<String, Supplier<String>> getPlaceholderSuppliers()
    {
        return Collections.unmodifiableMap(placeholderSuppliers);
    }

    private String computeText(String pattern)
    {
        return pattern.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', replaceTags(pattern));
    }

    /**
     * @param raw The raw text.
     * @return The text, with tags replaced using the registered placeholders.
     */
    private String replaceTags(String raw)
    {
        for (Map.Entry<String, Supplier<String>> entry : placeholderSuppliers.entrySet())
        {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue().get());
        }

        return raw;
    }

    public void update()
    {
        if (!isEnabled()) return;  // Other modules may keep a reference if the module is disabled.

        final String headerPattern = Config.HEADERS.get(UR.game().getPhase());
        final String footerPattern = Config.FOOTERS.get(UR.game().getPhase());

        if ((headerPattern != null && !headerPattern.isEmpty()) || (footerPattern != null && !footerPattern.isEmpty()))
        {
            final String header = headerPattern != null ? computeText(headerPattern) : "";
            final String footer = footerPattern != null ? computeText(footerPattern) : "";

            final Stream<? extends Player> receivers;

            if (UR.game().currentPhaseAfter(GamePhase.STARTING))
            {
                receivers = Stream.concat(
                        UR.game().getAliveConnectedPlayers().stream(),
                        UR.module(SpectatorsModule.class).getSpectators().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                );
            }
            else
            {
                receivers = Bukkit.getOnlinePlayers().stream();
            }

            receivers.forEach(player -> ListHeaderFooter.sendListHeaderFooter(player, header, footer));
        }
    }

    @EventHandler (priority = EventPriority.MONITOR) protected void onGamePhaseChange(final GamePhaseChangedEvent ev)  { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onPlayerDeath(final AlivePlayerDeathEvent ev)      { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onPlayerResurrect(final PlayerResurrectedEvent ev) { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onPlayerJoin(final PlayerJoinEvent ev)             { update(); }

    @EventHandler (priority = EventPriority.MONITOR) protected void onTeamsChange(final TeamUpdatedEvent ev)      { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onTeamsChange(final TeamRegisteredEvent ev)   { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onTeamsChange(final TeamUnregisteredEvent ev) { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onTeamsChange(final PlayerJoinedTeamEvent ev) { update(); }
    @EventHandler (priority = EventPriority.MONITOR) protected void onTeamsChange(final PlayerLeftTeamEvent ev)   { update(); }
}
