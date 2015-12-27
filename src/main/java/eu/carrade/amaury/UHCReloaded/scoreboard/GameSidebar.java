/*
 * Copyright or © or Copr. AmauryCarrade (2015)
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
package eu.carrade.amaury.UHCReloaded.scoreboard;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHGameManager;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.misc.Freezer;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import fr.zcraft.zlib.components.scoreboard.Sidebar;
import fr.zcraft.zlib.components.scoreboard.SidebarMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GameSidebar extends Sidebar
{
    private final I18n i;
    private final Configuration config;
    private final UHGameManager gameManager;

    private final boolean EPISODES_ENABLED;
    private final boolean EPISODES_IN_SIDEBAR;
    private final boolean PLAYERS_IN_SIDEBAR;
    private final boolean TEAMS_IN_SIDEBAR;
    private final boolean KILLS_IN_SIDEBAR;
    private final boolean TIMER_IN_SIDEBAR;
    private final boolean FREEZE_STATUS_IN_SIDEBAR;

    private final boolean OWN_TEAM_IN_SIDEBAR;
    private final String  OWN_TEAM_TITLE_COLOR;
    private final boolean OWN_TEAM_TITLE_IS_NAME;
    private final boolean OWN_TEAM_DISPLAY_HEARTS;
    private final boolean OWN_TEAM_COLOR_WHOLE_NAME;
    private final boolean OWN_TEAM_STRIKE_DEAD_PLAYERS;
    private final boolean OWN_TEAM_DISPLAY_LOGIN_STATE_ITALIC;
    private final String  OWN_TEAM_DISPLAY_LOGIN_STATE_SUFFIX;
    private final boolean OWN_TEAM_DISPLAY_MET_PLAYERS_ONLY;
    private final double  OWN_TEAM_DISPLAY_MET_PLAYERS_MIN_DISTANCE_SQUARED;

    private final String FROOZEN_NULL_TIMER_TEXT;
    private final String HEART = "\u2764";

    private final String sidebarTitle;
    private final List<String> sidebarTop = new ArrayList<>();
    private final List<String> sidebarTimers = new ArrayList<>();


    public GameSidebar()
    {
        i = UHCReloaded.i();

        config = UHCReloaded.get().getConfig();
        gameManager = UHCReloaded.get().getGameManager();

        EPISODES_ENABLED = config.getBoolean("episodes.enabled");
        EPISODES_IN_SIDEBAR = config.getBoolean("scoreboard.episode");
        PLAYERS_IN_SIDEBAR = config.getBoolean("scoreboard.players");
        TEAMS_IN_SIDEBAR = config.getBoolean("scoreboard.teams");
        KILLS_IN_SIDEBAR = config.getBoolean("scoreboard.kills");
        TIMER_IN_SIDEBAR = config.getBoolean("scoreboard.timer");
        FREEZE_STATUS_IN_SIDEBAR = config.getBoolean("scoreboard.freezeStatus");

        OWN_TEAM_IN_SIDEBAR = config.getBoolean("scoreboard.ownTeam.enabled");
        OWN_TEAM_TITLE_COLOR = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.ownTeam.title.color"));
        OWN_TEAM_TITLE_IS_NAME = config.getBoolean("scoreboard.ownTeam.title.useTeamName");
        OWN_TEAM_DISPLAY_HEARTS = config.getBoolean("scoreboard.ownTeam.content.displayHearts");
        OWN_TEAM_COLOR_WHOLE_NAME = config.getBoolean("scoreboard.ownTeam.content.colorName");
        OWN_TEAM_STRIKE_DEAD_PLAYERS = config.getBoolean("scoreboard.ownTeam.content.strikeDeadPlayers");
        OWN_TEAM_DISPLAY_LOGIN_STATE_ITALIC = config.getBoolean("scoreboard.ownTeam.content.loginState.italic");
        OWN_TEAM_DISPLAY_LOGIN_STATE_SUFFIX = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.ownTeam.content.loginState.suffix"));
        OWN_TEAM_DISPLAY_MET_PLAYERS_ONLY = config.getBoolean("scoreboard.ownTeam.content.displayMetPlayersOnly.enabled");
        OWN_TEAM_DISPLAY_MET_PLAYERS_MIN_DISTANCE_SQUARED = Math.pow(config.getDouble("scoreboard.ownTeam.content.displayMetPlayersOnly.displayedWhenCloserThan"), 2);

        FROOZEN_NULL_TIMER_TEXT = new UHTimer("").toString();

        setAsync(true);
        setAutoRefreshDelay(20);
        setContentMode(SidebarMode.PER_PLAYER);

        sidebarTitle = ChatColor.translateAlternateColorCodes('&', UHCReloaded.get().getConfig().getString("scoreboard.title", "Kill the Patrick"));
    }

    @Override
    public void preRender()
    {
        sidebarTop.clear();
        sidebarTimers.clear();

        // Top sidebar

        if (EPISODES_ENABLED && EPISODES_IN_SIDEBAR)
        {
            sidebarTop.add(i.t(
                    "scoreboard.episode",
                    String.valueOf(gameManager.isGameStarted() ? gameManager.getEpisode() : 0)
            ));
        }

        if (!gameManager.isGameStarted())
        {
            if (PLAYERS_IN_SIDEBAR)
                sidebarTop.add(i.t("scoreboard.players", String.valueOf(Bukkit.getOnlinePlayers().size())));
        }
        else
        {
            if (gameManager.isGameWithTeams() && EPISODES_ENABLED && EPISODES_IN_SIDEBAR)
                sidebarTop.add("");

            if (PLAYERS_IN_SIDEBAR)
                sidebarTop.add(i.t("scoreboard.players", String.valueOf(gameManager.getAlivePlayersCount())));

            if (gameManager.isGameWithTeams() && TEAMS_IN_SIDEBAR)
                sidebarTop.add(i.t("scoreboard.teams", String.valueOf(gameManager.getAliveTeamsCount())));
        }


        // Timers part of the sidebar

        insertTimers(sidebarTimers);

        if (TIMER_IN_SIDEBAR)
        {
            if (!gameManager.isGameStarted())
                sidebarTimers.add(FROOZEN_NULL_TIMER_TEXT);
            else
                sidebarTimers.add(UHCReloaded.get().getTimerManager().getMainTimer().toString());
        }
    }

    @Override
    public List<String> getContent(Player player)
    {
        List<String> sidebar = new ArrayList<>();

        sidebar.addAll(sidebarTop);
        sidebar.add("");

        if (OWN_TEAM_IN_SIDEBAR && gameManager.isGameStarted() && gameManager.isGameWithTeams())
        {
            UHTeam team = UHCReloaded.get().getTeamManager().getTeamForPlayer(player);

            if (team != null)
            {
                sidebar.add(
                          (OWN_TEAM_TITLE_COLOR.isEmpty() ? team.getColor().toChatColor() : OWN_TEAM_TITLE_COLOR)
                        + (OWN_TEAM_TITLE_IS_NAME ? ChatColor.BOLD + team.getName() : i.t("scoreboard.yourTeam"))
                );

                Location playerLocation = player.getLocation();

                for (UUID teamMember : team.getPlayersUUID())
                {
                    SidebarPlayerCache cache = UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(teamMember);

                    // If enabled, we check if the player was already met or is close to this player.
                    // Only if the damages are on (= 30 seconds after the game start) to avoid false close while
                    // teleporting.
                    if(OWN_TEAM_DISPLAY_MET_PLAYERS_ONLY)
                    {
                        if(!(teamMember.equals(player.getUniqueId()) || cache.getTeammatesDisplayed().contains(teamMember)))
                        {
                            if (gameManager.isGameStarted() && gameManager.isTakingDamage())
                            {
                                if (gameManager.isPlayerDead(teamMember))
                                    continue; // dead (spectators don't have to be displayed in the sidebar).

                                Player teammate = Sidebar.getPlayerAsync(teamMember);
                                if (teammate == null)
                                    continue; // offline


                                Location teammateLocation = teammate.getLocation();

                                // Check if the players are close
                                if (teammateLocation.getWorld().equals(playerLocation.getWorld()))
                                {
                                    final double distanceSquared = teammateLocation.distanceSquared(playerLocation);
                                    if (distanceSquared <= OWN_TEAM_DISPLAY_MET_PLAYERS_MIN_DISTANCE_SQUARED)
                                        cache.getTeammatesDisplayed().add(teamMember);
                                    else
                                        continue; // Too far, skipped
                                }
                                else
                                {
                                    continue; // Too far, skipped
                                }
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }

                    final String strike = OWN_TEAM_STRIKE_DEAD_PLAYERS && !cache.isAlive() ? ChatColor.STRIKETHROUGH.toString() : "";
                    final ChatColor aliveColor = cache.isAlive() ? ChatColor.WHITE : ChatColor.GRAY;

                    final String heart = OWN_TEAM_DISPLAY_HEARTS ? cache.getHealthColor() + strike + HEART + " " : "";
                    final String name = (OWN_TEAM_COLOR_WHOLE_NAME ? cache.getHealthColor() : aliveColor)
                            + strike
                            + (OWN_TEAM_DISPLAY_LOGIN_STATE_ITALIC && !cache.isOnline() ? ChatColor.ITALIC : "")
                            + cache.getPlayerName()
                            + (!cache.isOnline() ? ChatColor.RESET + "" + (OWN_TEAM_COLOR_WHOLE_NAME ? cache.getHealthColor() : aliveColor) + " " + OWN_TEAM_DISPLAY_LOGIN_STATE_SUFFIX : "");

                    sidebar.add(heart + name);
                }

                sidebar.add("");
            }
        }

        if (KILLS_IN_SIDEBAR && gameManager.isGameStarted())
        {
            SidebarPlayerCache cache = UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId());

            sidebar.add(i.t("scoreboard.kills", String.valueOf(cache.getPlayersKilled().size())));
            sidebar.add("");
        }

        sidebar.addAll(sidebarTimers);

        if (FREEZE_STATUS_IN_SIDEBAR)
        {
            insertFreezeStatus(sidebar, player);
        }

        return sidebar;
    }

    @Override
    public String getTitle(Player player)
    {
        return sidebarTitle;
    }


    /**
     * Inserts the timers in the given list, to be displayed in the sidebar, at the bottom of the
     * list.
     *
     * @param sidebar The list representing the sidebar's content.
     */
    private void insertTimers(List<String> sidebar)
    {
        for (UHTimer timer : UHCReloaded.get().getTimerManager().getTimers())
        {
            if (timer.isDisplayed())
            {
                sidebar.add(timer.getDisplayName());
                sidebar.add(timer.toString());
                sidebar.add("");
            }
        }
    }

    /**
     * Inserts the « frozen » text at the bottom of the list, if active globally or for the given
     * player.
     *
     * @param sidebar The list representing the sidebar's content.
     */
    private void insertFreezeStatus(List<String> sidebar, Player player)
    {
        final Freezer freezer = UHCReloaded.get().getFreezer();

        if ((freezer.getGlobalFreezeState() && !freezer.isHiddenFreeze()) || freezer.isPlayerFrozen(player))
        {
            sidebar.add("");
            sidebar.add(i.t("freeze.scoreboard"));
        }
    }
}
