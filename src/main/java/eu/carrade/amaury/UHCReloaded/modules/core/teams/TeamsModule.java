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
package eu.carrade.amaury.UHCReloaded.modules.core.teams;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.UHCReloaded.modules.core.teams.sidebar.SidebarCacheListener;
import eu.carrade.amaury.UHCReloaded.modules.core.teams.sidebar.SidebarPlayerCache;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.commands.TeamsCommand;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;


@ModuleInfo (
        name = "Teams",
        description = "Manages the teams and related commands",
        settings = Config.class,
        internal = true,
        can_be_disabled = false
)
public class TeamsModule extends UHModule
{
    private final String HEART = "\u2764";
    private double TEAMMATES_DISTANCE_SQUARED;

    private GameModule game = null;

    private Map<UUID, SidebarPlayerCache> sidebarCache = new HashMap<>();


    @Override
    protected void onEnable()
    {
        ZLib.loadComponent(ZTeams.class);

        ZTeams.settings()
                .setScoreboard(UHCReloaded.get().getScoreboard());

        TEAMMATES_DISTANCE_SQUARED = Math.pow(Config.SIDEBAR.CONTENT.DISPLAY_MET_PLAYERS_ONLY.DISPLAYED_WHEN_CLOSER_THAN.get(), 2d);
    }

    @EventHandler
    public void onGameStarting(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.STARTING) return;
        ZLib.registerEvents(new SidebarCacheListener());
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(TeamsCommand.class);
    }

    @Override
    public void injectIntoSidebar(final Player player, final SidebarInjector injector)
    {
        if (game == null) game = UR.module(GameModule.class);
        if (game == null) return; // Module not ready

        if (!game.isTeamsGame() || !Config.SIDEBAR.ENABLED.get() || game.getPhase().ordinal() < GamePhase.IN_GAME.ordinal())
            return;

        final ZTeam team = ZTeams.get().getTeamForPlayer(player);

        if (team == null) return;


        /* *** TEAM NAME *** */

        injector.injectLines(
            true, false,
            (Config.SIDEBAR.TITLE.COLOR.get().isEmpty() ? team.getColorOrWhite().toChatColor() : Config.SIDEBAR.TITLE.COLOR.get())
                + (Config.SIDEBAR.TITLE.USE_TEAM_NAME.get() ? ChatColor.BOLD + team.getName() : I.t("{bold}Your team"))
        );


        /* *** TEAM PLAYERS: WHICH ONES *** */

        final Set<OfflinePlayer> displayedPlayers = new TreeSet<>((player1, player2) -> {
            if (player1.equals(player2)) return 0;
            if (game.isAlive(player1) != game.isAlive(player2)) return game.isAlive(player1) ? -1 : 1;
            return player1.getName().toLowerCase().compareTo(player2.getName().toLowerCase());
        });

        final SidebarPlayerCache playerCache = getSidebarPlayerCache(player.getUniqueId());

        if (Config.SIDEBAR.CONTENT.DISPLAY_MET_PLAYERS_ONLY.ENABLED.get())
        {
            playerCache.updateTeammatesDisplayed(TEAMMATES_DISTANCE_SQUARED, player.getLocation(), team);
            displayedPlayers.addAll(playerCache.getMetTeammates());
        }

        else displayedPlayers.addAll(team.getPlayers());


        /* *** TEAM PLAYERS: DISPLAY *** */

        final List<String> playersSidebar = new ArrayList<>();

        displayedPlayers.forEach(displayedPlayer -> {
            final SidebarPlayerCache cache = getSidebarPlayerCache(displayedPlayer.getUniqueId());
            final boolean alive = game.isAlive(displayedPlayer);

            final String strike = Config.SIDEBAR.CONTENT.STRIKE_DEAD_PLAYERS.get() && !alive ? ChatColor.STRIKETHROUGH.toString() : "";
            final ChatColor aliveColor = alive ? ChatColor.WHITE : ChatColor.GRAY;

            final String heart = Config.SIDEBAR.CONTENT.DISPLAY_HEARTS.get() ? cache.getHealthColor() + strike + HEART + " " : "";
            final String name = (Config.SIDEBAR.CONTENT.COLOR_NAME.get() ? cache.getHealthColor() : aliveColor)
                    + strike
                    + (Config.SIDEBAR.CONTENT.LOGIN_STATE.ITALIC.get() && !cache.isOnline() ? ChatColor.ITALIC : "")
                    + cache.getPlayerName()
                    + (!cache.isOnline() ? ChatColor.RESET + "" + (Config.SIDEBAR.CONTENT.COLOR_NAME.get() ? cache.getHealthColor() : aliveColor) + " " + Config.SIDEBAR.CONTENT.LOGIN_STATE.SUFFIX.get() : "");

            playersSidebar.add(heart + name);
        });

        injector.injectLines(false, true, playersSidebar);
    }

    /**
     * Returns the cached data about the given player.
     *
     * @param id The player's UUID.
     * @return The cached data, created on the fly if needed.
     */
    public SidebarPlayerCache getSidebarPlayerCache(UUID id)
    {
        return sidebarCache.computeIfAbsent(id, SidebarPlayerCache::new);
    }
}
