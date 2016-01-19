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

package eu.carrade.amaury.UHCReloaded.integration;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.teams.TeamColor;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;


public class UHDynmapIntegration
{
    private UHCReloaded p = null;
    private DynmapAPI api = null;
    private MarkerAPI markerAPI = null;
    private MarkerSet markerSet = null;

    public UHDynmapIntegration(UHCReloaded plugin)
    {
        this.p = plugin;

        Plugin apiTest = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if (apiTest == null || !apiTest.isEnabled())
        {
            PluginLogger.warning("Dynmap is not present, so the integration was disabled.");
            return;
        }

        this.api = (DynmapAPI) apiTest;

        markerAPI = api.getMarkerAPI();
        if (markerAPI == null)
        {
            PluginLogger.warning("Dynmap is available, but the markers API is not. The integration was disabled.");
            return;
        }


        // All is good, let's integrate.
        initDynmapIntegration();

        PluginLogger.info("Successfully hooked into Dynmap.");
    }

    private void initDynmapIntegration()
    {

        markerSet = markerAPI.getMarkerSet("uhplugin.markerset");

        if (markerSet == null)
        {
            markerSet = markerAPI.createMarkerSet("uhplugin.markerset", "UltraHardcore", null, false);
        }
        else
        {
            markerSet.setMarkerSetLabel("UltraHardcore");
        }
    }

    public void shutdownDynmapIntegration()
    {
        if (isDynmapIntegrationEnabled())
        {
            markerSet.deleteMarkerSet();
        }
    }

    public boolean isDynmapIntegrationEnabled()
    {
        return !(this.api == null);
    }

    public DynmapAPI getDynmapAPI()
    {
        return api;
    }

    public MarkerAPI getDynmapMarkerAPI()
    {
        return markerAPI;
    }

    /** Death locations **/

    /**
     * Displays the death location of the given player.
     *
     * @param player The player.
     */
    public void showDeathLocation(Player player)
    {
        if (!isDynmapIntegrationEnabled())
        {
            return;
        }

        if (!p.getConfig().getBoolean("dynmap.showDeathLocations"))
        {
            return;
        }

        if (!p.getGameManager().hasDeathLocation(player))
        {
            return;
        }

        Location deathPoint = p.getGameManager().getDeathLocation(player);

        String markerID = getDeathMarkerName(player);
        String markerLabel = I.t("dynmap.markerLabelDeath", player.getName());
        MarkerIcon icon = markerAPI.getMarkerIcon("skull");

        Marker marker = markerSet.createMarker(markerID, markerLabel, true, deathPoint.getWorld().getName(), deathPoint.getX(), deathPoint.getY(), deathPoint.getZ(), icon, false);
        if (marker == null)
        {
            p.getLogger().warning("Unable to create marker " + markerID);
        }
    }

    /**
     * Hides the death location of the given player.
     *
     * @param player The player.
     */
    public void hideDeathLocation(Player player)
    {
        if (!isDynmapIntegrationEnabled())
        {
            return;
        }

        if (!p.getConfig().getBoolean("dynmap.showDeathLocations"))
        {
            return;
        }

        Marker marker = markerSet.findMarker(getDeathMarkerName(player));
        if (marker != null)
        {
            marker.deleteMarker();
        }
    }

    /**
     * Returns the internal ID of the marker of the death point of the given player.
     *
     * @param player The player.
     * @return The ID.
     */
    private String getDeathMarkerName(Player player)
    {
        return "uhplugin.death." + player.getName();
    }


    /** Spawn locations **/

    /**
     * Displays the spawn point of the given team.
     *
     * @param team The team.
     * @param spawnPoint The location of the spawn point.
     */
    public void showSpawnLocation(UHTeam team, Location spawnPoint)
    {
        if (!isDynmapIntegrationEnabled())
        {
            return;
        }

        if (!p.getConfig().getBoolean("dynmap.showSpawnLocations"))
        {
            return;
        }


        TeamColor teamColor = team.getColor();
        if (teamColor == null)
        {
            teamColor = TeamColor.GREEN; // green flags for solo games without colors
        }

        String markerID = getSpawnMarkerName(team);

        String markerLabel;
        if (p.getGameManager().isGameWithTeams())
        {
            markerLabel = I.t("dynmap.markerLabelSpawn", team.getName());
        }
        else
        {
            markerLabel = I.t("dynmap.markerLabelSpawnNoTeam", team.getName());
        }

        showSpawnLocation(spawnPoint, teamColor, markerLabel, markerID);
    }

    /**
     * Displays the spawn point of the given player.
     *
     * <p>
     *     Used when the teleportation ignores the teams.
     * </p>
     *
     * @param player The player.
     * @param spawnPoint The location of the spawn point.
     */
    public void showSpawnLocation(OfflinePlayer player, Location spawnPoint)
    {
        if (player == null) return;

        UHTeam team = p.getTeamManager().getTeamForPlayer(player);

        showSpawnLocation(player, team != null ? team.getColor() : null, spawnPoint);
    }

    /**
     * Displays the spawn point of the given player.
     *
     * <p>
     *     Used when the teleportation ignores the teams.
     * </p>
     *
     * @param player The player.
     * @param color The color of the spawn point (i.e. of the team).
     * @param spawnPoint The location of the spawn point.
     */
    public void showSpawnLocation(OfflinePlayer player, TeamColor color, Location spawnPoint)
    {
        if (!isDynmapIntegrationEnabled())
        {
            return;
        }

        if (!p.getConfig().getBoolean("dynmap.showSpawnLocations"))
        {
            return;
        }


        if (color == null)
        {
            color = TeamColor.GREEN;
        }

        String markerID = getSpawnMarkerName(player);
        String markerLabel = I.t("dynmap.markerLabelSpawnNoTeam", player.getName());

        showSpawnLocation(spawnPoint, color, markerLabel, markerID);
    }

    /**
     * Displays a spawn-point marker.
     *
     * @param spawnPoint The location of the spawn.
     * @param color The color of the team (for the flag).
     * @param label The label of the marker.
     * @param markerID The ID of the marker.
     */
    private void showSpawnLocation(Location spawnPoint, TeamColor color, String label, String markerID)
    {

		/* ***  Icon  *** */

        MarkerIcon icon;

        // Let's try to find the best icon
        // Available flags:
        // redflag, orangeflag, yellowflag, greenflag, blueflag, purpleflag, pinkflag, pirateflag (black)
        // Ref. https://github.com/webbukkit/dynmap/wiki/Using-markers

        switch (color)
        {
            case BLUE:
            case DARK_BLUE:
            case AQUA:
            case DARK_AQUA:
                icon = markerAPI.getMarkerIcon("blueflag");
                break;

            case GREEN:
            case DARK_GREEN:
                icon = markerAPI.getMarkerIcon("greenflag");
                break;

            case GOLD:
                icon = markerAPI.getMarkerIcon("orangeflag");
                break;

            case YELLOW:
                icon = markerAPI.getMarkerIcon("yellowflag");
                break;

            case RED:
            case DARK_RED:
                icon = markerAPI.getMarkerIcon("redflag");
                break;

            case DARK_PURPLE:
                icon = markerAPI.getMarkerIcon("purpleflag");
                break;

            case LIGHT_PURPLE:
                icon = markerAPI.getMarkerIcon("pinkflag");
                break;

            case BLACK:
            case DARK_GRAY:
            case GRAY:
                icon = markerAPI.getMarkerIcon("pirateflag");
                break;

            case WHITE: // There is nothing better than pink I think...
            default:
                icon = markerAPI.getMarkerIcon("pinkflag");
                break;
        }


		/* ***  Registration  *** */

        Marker marker = markerSet.createMarker(markerID, label, true, spawnPoint.getWorld().getName(), spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), icon, false);

        if (marker == null)
        {
            PluginLogger.warning("Unable to create marker {0}", markerID);
        }
    }

    /**
     * Returns the internal ID of the marker of the spawn point of the given team.
     *
     * @param team The team.
     * @return The ID.
     */
    private String getSpawnMarkerName(UHTeam team)
    {
        return "uhplugin.spawn." + team.getName();
    }

    /**
     * Returns the internal ID of the marker of the spawn point of the given team.
     *
     * <p>
     *     Used if the teleportation ignores the teams.
     * </p>
     *
     * @param player The player.
     * @return The ID.
     */
    private String getSpawnMarkerName(OfflinePlayer player)
    {
        return "uhplugin.spawn." + player.getName();
    }
}
