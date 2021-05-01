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

package eu.carrade.amaury.quartzsurvivalgames.modules.cosmetics.playerListHeaderFooter;

import eu.carrade.amaury.quartzsurvivalgames.QSGConfig;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzteams.events.PlayerJoinedTeamEvent;
import fr.zcraft.quartzteams.events.PlayerLeftTeamEvent;
import fr.zcraft.quartzteams.events.TeamRegisteredEvent;
import fr.zcraft.quartzteams.events.TeamUnregisteredEvent;
import fr.zcraft.quartzteams.events.TeamUpdatedEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;


@ModuleInfo(
        name = "Players List Header & Footer",
        description = "Fills the players list header & footer with any text, that may contains infos related " +
                "to the current game through placeholders. Other modules can add placeholders.",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.COSMETICS,
        icon = Material.ACTIVATOR_RAIL,
        settings = Config.class,
        can_be_loaded_late = false
)
public class PlayerListHeaderFooterModule extends QSGModule {
    private final Map<String, Supplier<String>> placeholderSuppliers = new HashMap<>();

    @Override
    protected void onEnable() {
        registerPlaceholder("title", QSGConfig.TITLE::get);
        registerPlaceholder("playersText", () -> I.tn("{0} player", "{0} players", QSG.game().countAlivePlayers()));
        registerPlaceholder("playersCount", () -> String.valueOf(QSG.game().countAlivePlayers()));
        registerPlaceholder("teamsText", () -> I.tn("{0} team", "{0} teams", QSG.game().countAliveTeams()));
        registerPlaceholder("teamsCount", () -> String.valueOf(QSG.game().countAliveTeams()));

        RunTask.nextTick(this::update);
    }

    @Override
    public List<Class<? extends Command>> getCommands() {
        return Collections.singletonList(ListPlaceholdersCommand.class);
    }

    /**
     * Registers a new placeholder for player list headers & footers.
     *
     * @param placeholderName The name of the placeholder. The module will lookup for {givenName}
     *                        in the patterns.
     * @param supplier        The supplier returning the value to use for this placeholder.
     */
    public void registerPlaceholder(final String placeholderName, final Supplier<String> supplier) {
        placeholderSuppliers.put(placeholderName, supplier);
    }

    public Map<String, Supplier<String>> getPlaceholderSuppliers() {
        return Collections.unmodifiableMap(placeholderSuppliers);
    }

    private String computeText(String pattern) {
        return pattern.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', replaceTags(pattern));
    }

    /**
     * @param raw The raw text.
     * @return The text, with tags replaced using the registered placeholders.
     */
    private String replaceTags(String raw) {
        for (Map.Entry<String, Supplier<String>> entry : placeholderSuppliers.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue().get());
        }

        return raw;
    }

    public void update() {
        if (!isEnabled()) {
            return;  // Other modules may keep a reference if the module is disabled.
        }

        final String headerPattern = Config.HEADERS.get(QSG.game().getPhase());
        final String footerPattern = Config.FOOTERS.get(QSG.game().getPhase());

        if ((headerPattern != null && !headerPattern.isEmpty()) ||
                (footerPattern != null && !footerPattern.isEmpty())) {
            final String header = headerPattern != null ? computeText(headerPattern) : "";
            final String footer = footerPattern != null ? computeText(footerPattern) : "";

            final Stream<? extends Player> receivers;

            if (QSG.game().currentPhaseAfter(GamePhase.STARTING)) {
                receivers = Stream.concat(
                        QSG.game().getAliveConnectedPlayers().stream(),
                        QSG.module(SpectatorsModule.class).getSpectators().stream().map(Bukkit::getPlayer)
                                .filter(Objects::nonNull)
                );
            } else {
                receivers = Bukkit.getOnlinePlayers().stream();
            }

            receivers.forEach(player -> player.setPlayerListHeaderFooter(header, footer));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onGamePhaseChange(final GamePhaseChangedEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerDeath(final AlivePlayerDeathEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerResurrect(final PlayerResurrectedEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerJoin(final PlayerJoinEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onTeamsChange(final TeamUpdatedEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onTeamsChange(final TeamRegisteredEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onTeamsChange(final TeamUnregisteredEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onTeamsChange(final PlayerJoinedTeamEvent ev) {
        update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onTeamsChange(final PlayerLeftTeamEvent ev) {
        update();
    }
}
