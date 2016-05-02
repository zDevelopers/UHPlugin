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
package eu.carrade.amaury.UHCReloaded.scoreboard;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.borders.MapShape;
import eu.carrade.amaury.UHCReloaded.borders.worldborders.WorldBorder;
import eu.carrade.amaury.UHCReloaded.game.UHGameManager;
import eu.carrade.amaury.UHCReloaded.misc.Freezer;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.scoreboard.Sidebar;
import fr.zcraft.zlib.components.scoreboard.SidebarMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class GameSidebar extends Sidebar
{
    private final UHGameManager gameManager;
    private final WorldBorder border;

    private final boolean EPISODES_ENABLED;
    private final boolean EPISODES_IN_SIDEBAR;
    private final boolean PLAYERS_IN_SIDEBAR;
    private final boolean TEAMS_IN_SIDEBAR;
    private final boolean BORDER_IN_SIDEBAR;
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

    private final boolean BORDER_DISPLAY_DIAMETER;

    private final String FROOZEN_NULL_TIMER_TEXT;
    private final String HEART = "\u2764";

    private final String sidebarTitle;
    private final List<String> sidebarTop = new ArrayList<>();
    private final List<String> sidebarBorder = new ArrayList<>();
    private final List<String> sidebarTimers = new ArrayList<>();


    public GameSidebar()
    {
        gameManager = UHCReloaded.get().getGameManager();
        border = UHCReloaded.get().getBorderManager().getBorderProxy();

        EPISODES_ENABLED = UHConfig.EPISODES.ENABLED.get();
        EPISODES_IN_SIDEBAR = UHConfig.SCOREBOARD.EPISODE.get();
        PLAYERS_IN_SIDEBAR = UHConfig.SCOREBOARD.PLAYERS.get();
        TEAMS_IN_SIDEBAR = UHConfig.SCOREBOARD.TEAMS.get();
        BORDER_IN_SIDEBAR = UHConfig.SCOREBOARD.BORDER.DISPLAYED.get();
        KILLS_IN_SIDEBAR = UHConfig.SCOREBOARD.KILLS.get();
        TIMER_IN_SIDEBAR = UHConfig.SCOREBOARD.TIMER.get();
        FREEZE_STATUS_IN_SIDEBAR = UHConfig.SCOREBOARD.FREEZE_STATUS.get();

        OWN_TEAM_IN_SIDEBAR = UHConfig.SCOREBOARD.OWN_TEAM.ENABLED.get();
        OWN_TEAM_TITLE_COLOR = ChatColor.translateAlternateColorCodes('&', UHConfig.SCOREBOARD.OWN_TEAM.TITLE.COLOR.get());
        OWN_TEAM_TITLE_IS_NAME = UHConfig.SCOREBOARD.OWN_TEAM.TITLE.USE_TEAM_NAME.get();
        OWN_TEAM_DISPLAY_HEARTS = UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.DISPLAY_HEARTS.get();
        OWN_TEAM_COLOR_WHOLE_NAME = UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.COLOR_NAME.get();
        OWN_TEAM_STRIKE_DEAD_PLAYERS = UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.STRIKE_DEAD_PLAYERS.get();
        OWN_TEAM_DISPLAY_LOGIN_STATE_ITALIC = UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.LOGIN_STATE.ITALIC.get();
        OWN_TEAM_DISPLAY_LOGIN_STATE_SUFFIX = ChatColor.translateAlternateColorCodes('&', UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.LOGIN_STATE.SUFFIX.get());
        OWN_TEAM_DISPLAY_MET_PLAYERS_ONLY = UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.DISPLAY_MET_PLAYERS_ONLY.ENABLED.get();
        OWN_TEAM_DISPLAY_MET_PLAYERS_MIN_DISTANCE_SQUARED = Math.pow(UHConfig.SCOREBOARD.OWN_TEAM.CONTENT.DISPLAY_MET_PLAYERS_ONLY.DISPLAYED_WHEN_CLOSER_THAN.get(), 2);

        BORDER_DISPLAY_DIAMETER = UHConfig.SCOREBOARD.BORDER.DISPLAY_DIAMETER.get();

        FROOZEN_NULL_TIMER_TEXT = new UHTimer("").toString();

        setAsync(true);
        setAutoRefreshDelay(20);
        setContentMode(SidebarMode.PER_PLAYER);

        sidebarTitle = ChatColor.translateAlternateColorCodes('&', UHConfig.SCOREBOARD.TITLE.get());
    }

    @Override
    public void preRender()
    {
        sidebarTop.clear();
        sidebarBorder.clear();
        sidebarTimers.clear();

        // Top sidebar

        if (EPISODES_ENABLED && EPISODES_IN_SIDEBAR)
        {
            /// Current episode in the sidebar
            sidebarTop.add(I.t("{gray}Episode {white}{0}",
                    String.valueOf(gameManager.isGameStarted() ? gameManager.getEpisode() : 0)
            ));
        }

        if (!gameManager.isGameStarted())
        {
            if (PLAYERS_IN_SIDEBAR)
                /// Players alive in the sidebar
                sidebarTop.add(I.tn("{white}{0}{gray} player", "{white}{0}{gray} players", Bukkit.getOnlinePlayers().size(), Bukkit.getOnlinePlayers().size()));
        }
        else
        {
            if (gameManager.isGameWithTeams() && EPISODES_ENABLED && EPISODES_IN_SIDEBAR)
                sidebarTop.add("");

            if (PLAYERS_IN_SIDEBAR)
                /// Players alive in the sidebar
                sidebarTop.add(I.tn("{white}{0}{gray} player", "{white}{0}{gray} players", gameManager.getAlivePlayersCount(), gameManager.getAlivePlayersCount()));

            if (gameManager.isGameWithTeams() && TEAMS_IN_SIDEBAR)
                /// Teams alive in the sidebar
                sidebarTop.add(I.tn("{white}{0}{gray} team", "{white}{0}{gray} teams", gameManager.getAliveTeamsCount(), gameManager.getAliveTeamsCount()));
        }


        // Border part of the sidebar

        if (gameManager.isGameStarted())
        {
            insertBorder(sidebarBorder);
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
                          /// Title of the team section in the sidebar
                        + (OWN_TEAM_TITLE_IS_NAME ? ChatColor.BOLD + team.getName() : I.t("{bold}Your team"))
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

        sidebar.addAll(sidebarBorder);

        if (KILLS_IN_SIDEBAR && gameManager.isGameStarted())
        {
            SidebarPlayerCache cache = UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId());

            /// Kills count in the sidebar
            sidebar.add(I.tn("{white}{0}{gray} player killed", "{white}{0}{gray} players killed", cache.getPlayersKilled().size(), cache.getPlayersKilled().size()));
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
     * Inserts the border status in the given list, to be displayed in the sidebar.
     *
     * @param sidebar The list representing the sidebar's content.
     */
    private void insertBorder(List<String> sidebar)
    {
        if (BORDER_IN_SIDEBAR)
        {
            /// Title of the border section in the sidebar
            sidebar.add(I.t("{blue}{bold}Border"));

            int diameter = (int) Math.ceil(border.getDiameter());

            if (BORDER_DISPLAY_DIAMETER || border.getShape() == MapShape.CIRCULAR)
            {
                if (border.getShape() == MapShape.SQUARED)
                    /// Border diameter for a squared map in the sidebar
                    sidebar.add(I.tn("{white}{0} block wide", "{white}{0} blocks wide", diameter, diameter));
                else
                    /// Border diameter for a circular map in the sidebar
                    sidebar.add(I.tn("{gray}Diameter: {white}{0} block", "{gray}Diameter: {white}{0} blocks", diameter, diameter));
            }
            else
            {
                Location center = border.getCenter();
                int radius = (int) Math.ceil(diameter / 2);

                int minX = center.getBlockX() - radius;
                int maxX = center.getBlockX() + radius;
                int minZ = center.getBlockZ() - radius;
                int maxZ = center.getBlockZ() + radius;

                // Same min & max, we can display both at once
                if (minX == minZ && maxX == maxZ)
                {
                    /// Min & max coordinates in the sidebar, to locate the border. Ex: "-500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{white}{0} {1}", UHUtils.integerToStringWithSign(minX), UHUtils.integerToStringWithSign(maxZ)));
                }
                else
                {
                    /// Min & max X coordinates in the sidebar, to locate the border. Ex: "X: -500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{gray}X: {white}{0} {1}", UHUtils.integerToStringWithSign(minX), UHUtils.integerToStringWithSign(maxX)));
                    /// Min & max Z coordinates in the sidebar, to locate the border. Ex: "Z: -500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{gray}Z: {white}{0} {1}", UHUtils.integerToStringWithSign(minZ), UHUtils.integerToStringWithSign(maxZ)));
                }
            }

            sidebar.add("");
        }
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
            /// Notice displayed at the bottom of the sidebar if the game is paused (/uh freeze all).
            sidebar.add(I.t("{darkaqua}Game frozen"));
        }
    }
}
