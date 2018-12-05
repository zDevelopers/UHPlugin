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
package eu.carrade.amaury.UHCReloaded.modules.beginning.cages;

import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.start.PlayerAboutToBeTeleportedToSpawnPointEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.start.PlayerSpawnPointSelectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.start.PlayerTeleportedToSpawnPointEvent;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashMap;
import java.util.Map;


@ModuleInfo (
        name = "Cages",
        description = "Puts players in cages instead of floating in the air, during the startup process",
        when = ModuleInfo.ModuleLoadTime.ON_GAME_STARTING,
        settings = Config.class
)
public class CagesModule extends UHModule
{
    private Map<Location, Cage> cages = new HashMap<>();


    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerSpawnSelected(PlayerSpawnPointSelectedEvent ev)
    {
        final Location spawn = cloneAndNormalize(ev.getSpawnPoint());
        cages.putIfAbsent(spawn, Cage.createInstanceForTeam(ZTeams.get().getTeamForPlayer(ev.getPlayer()), spawn));
    }

    @EventHandler
    public void onPlayerAboutToBeTeleportedToSpawn(PlayerAboutToBeTeleportedToSpawnPointEvent ev)
    {
        final Cage cage = cages.get(cloneAndNormalize(ev.getSpawnPoint()));

        if (cage != null) cage.build();
    }

    @EventHandler
    public void onPlayerTeleportedToSpawn(PlayerTeleportedToSpawnPointEvent ev)
    {
        final Location normalizedLocation = cloneAndNormalize(ev.getSpawnPoint());

        // We only remove the fly if there is a cage for that player.
        if (cages.containsKey(normalizedLocation))
        {
            ev.getPlayer().setFlying(false);
            ev.getPlayer().setAllowFlight(false);

            // We slightly fix the player location so he is at the center of the platform.
            ev.getPlayer().teleport(normalizedLocation.add(.5, .5, .5));
        }
    }

    @EventHandler
    public void onGameStarts(GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.IN_GAME) return;
        cages.forEach((location, nicolas) -> nicolas.destroy()); // Not even sorry.
    }

    private Location cloneAndNormalize(final Location location)
    {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), 0, 0);
    }
}
