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
package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.commands.TPDeathCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.commands.TPSpawnCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.commands.TPSpectatorsCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.commands.TPTeamCommand;
import fr.zcraft.quartzlib.components.commands.Command;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;

import java.util.*;


@ModuleInfo (
        name = "Teleportation Commands",
        description = "Provides commands to teleport to spawn, death location, or groups of players.",
        category = ModuleCategory.UTILITIES,
        icon = Material.COMMAND_MINECART
)
public class TeleportationModule extends QSGModule
{
    private final Map<UUID, Location> deathLocations = new HashMap<>();

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Arrays.asList(
                TPDeathCommand.class,
                TPSpawnCommand.class,
                TPTeamCommand.class,
                TPSpectatorsCommand.class
        );
    }

    @EventHandler
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        if (ev.getPlayer().isOnline())
        {
            deathLocations.put(ev.getPlayer().getUniqueId(), ev.getPlayer().getPlayer().getLocation());
        }
    }

    public boolean hasDeathLocation(final OfflinePlayer player)
    {
        return deathLocations.containsKey(player.getUniqueId());
    }

    public Location getDeathLocation(final OfflinePlayer player)
    {
        return deathLocations.get(player.getUniqueId()).clone();
    }
}
