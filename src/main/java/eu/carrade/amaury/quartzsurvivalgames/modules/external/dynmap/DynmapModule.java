/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.quartzsurvivalgames.modules.external.dynmap;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.start.PlayerSpawnPointSelectedEvent;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.ZLib;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.colors.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;


@ModuleInfo (
        name = "Dynmap",
        description = "Displays the spawn & death points on the dynmap.",
        when = ModuleLoadTime.ON_GAME_STARTING,
        category = ModuleCategory.EXTERNAL,
        icon = Material.MAP,
        settings = Config.class,
        depends = "dynmap"
)
/*
 * TODO: add the world border to the map.
 */
public class DynmapModule extends QSGModule
{
    private final DynmapAPI dynmapAPI = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
    private final MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

    private MarkerSet markerSet = null;

    @Override
    protected void onEnable()
    {
        if (markerAPI == null)
        {
            log().warning("Dynmap is available, but the markers API is not. The integration was disabled.");
            ZLib.unregisterEvents(this);
            return;
        }

        markerSet = markerAPI.getMarkerSet("uhplugin.markerset");

        if (markerSet == null)
        {
            markerSet = markerAPI.createMarkerSet("uhplugin.markerset", "UHCReloaded", null, false);
        }
        else
        {
            markerSet.setMarkerSetLabel("UHCReloaded");
        }
    }

    @Override
    protected void onDisable()
    {
        if (markerSet != null)
        {
            markerSet.deleteMarkerSet();
        }
    }



    /* *** DEATH LOCATIONS *** */


    /**
     * Displays the death location of the given player.
     *
     * @param player The player.
     */
    public void showDeathLocation(final Player player)
    {
        if (!Config.SHOW_DEATH_LOCATIONS.get())
        {
            return;
        }

        final String markerID = getDeathMarkerName(player);
        /// Dynmap marker label of a death point
        final String markerLabel = I.t("Death point of {0}", player.getName());
        final MarkerIcon icon = markerAPI.getMarkerIcon("skull");

        final Marker marker = markerSet.createMarker(
                markerID, markerLabel, true,
                player.getLocation().getWorld().getName(),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                icon, false
        );

        if (marker == null)
        {
            log().warning("Unable to create marker " + markerID);
        }
    }

    /**
     * Hides the death location of the given player.
     *
     * @param player The player.
     */
    public void hideDeathLocation(final OfflinePlayer player)
    {
        if (!Config.SHOW_DEATH_LOCATIONS.get())
        {
            return;
        }

        final Marker marker = markerSet.findMarker(getDeathMarkerName(player));
        if (marker != null) marker.deleteMarker();
    }

    /**
     * Returns the internal ID of the marker of the death point of the given player.
     *
     * @param player The player.
     * @return The ID.
     */
    private String getDeathMarkerName(final OfflinePlayer player)
    {
        return "uhplugin.death." + player.getName();
    }



    /* *** SPAWNS LOCATIONS *** */


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
    public void showSpawnLocation(final OfflinePlayer player, final Location spawnPoint)
    {
        if (!Config.SHOW_SPAWN_LOCATIONS.get()) return;
        if (player == null) return;

        final ZTeam team = ZTeams.get().getTeamForPlayer(player);
        if (team == null) return;

        final String markerLabel;
        if (QSG.module(GameModule.class).isTeamsGame() && team.size() > 1)
        {
            /// Dynmap marker label of a spawn point of a team.
            markerLabel = I.t("Spawn point of the team {0}", team.getName());
        }
        else
        {
            /// Dynmap marker label of a spawn point of a player, in solo.
            markerLabel = I.t("Spawn point of {0}", team.getName());
        }

        showSpawnLocation(
                spawnPoint,
                team.getColor() == null ? TeamColor.GREEN : team.getColor(),
                markerLabel,
                getSpawnMarkerName(player)
        );
    }

    /**
     * Displays a spawn-point marker.
     *
     * @param spawnPoint The location of the spawn.
     * @param color The color of the team (for the flag).
     * @param label The label of the marker.
     * @param markerID The ID of the marker.
     */
    private void showSpawnLocation(final Location spawnPoint, final TeamColor color, final String label, final String markerID)
    {
        /* ***  Icon  *** */

        final MarkerIcon icon;

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


        /* *** Duplicate check *** */

        // We check if there is already a spawn point with the same name
        // at the same location. For teams games, with all players teleported
        // to the same spot, this could occur.

        final Marker similarMarker = markerSet.findMarkerByLabel(label);
        if (similarMarker != null
                && similarMarker.getMarkerIcon().equals(icon)
                && similarMarker.getX() == spawnPoint.getX()
                && similarMarker.getY() == spawnPoint.getY()
                && similarMarker.getZ() == spawnPoint.getZ())
        {
            return;
        }


        /* ***  Registration  *** */

        final Marker marker = markerSet.createMarker(
                markerID,
                label,
                true,
                spawnPoint.getWorld().getName(),
                spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(),
                icon,
                false
        );

        if (marker == null)
        {
            log().warning("Unable to create marker {0}", markerID);
        }
    }

    /**
     * Returns the internal ID of the marker of the spawn point of the given player.
     *
     * @param player The player.
     * @return The ID.
     */
    private String getSpawnMarkerName(final OfflinePlayer player)
    {
        return "uhplugin.spawn." + player.getName();
    }



    /* *** EVENTS INTEGRATION *** */


    @EventHandler (priority = EventPriority.MONITOR)
    public void onSpawnPointSelected(final PlayerSpawnPointSelectedEvent ev)
    {
        showSpawnLocation(ev.getPlayer(), ev.getSpawnPoint());
    }

    @EventHandler
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        if (ev.getPlayer().isOnline())
        {
            showDeathLocation(ev.getPlayer().getPlayer());
        }
    }

    @EventHandler
    public void onPlayerResurrected(final PlayerResurrectedEvent ev)
    {
        hideDeathLocation(ev.getPlayer());
    }
}
