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
package eu.carrade.amaury.UHCReloaded.modules.ingame.killsCount;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;


@ModuleInfo (
        name = "Kills Count",
        description = "Displays the player's kills count in the sidebar.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.COSMETICS,
        icon = Material.DIAMOND_SWORD
)
public class KillsCountModule extends UHModule
{
    private final Map<UUID, Set<UUID>> kills = new HashMap<>();

    @Override
    public void injectIntoSidebar(final Player player, final SidebarInjector injector)
    {
        injector.injectLines(
                SidebarInjector.SidebarPriority.BOTTOM, true,
                I.tn("{white}{0}{gray} player killed", "{white}{0}{gray} players killed", getKillsFor(player.getUniqueId()).size())
        );
    }

    private Set<UUID> getKillsFor(final UUID playerID)
    {
        return kills.computeIfAbsent(playerID, uuid -> new HashSet<>());
    }

    @EventHandler
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        if (ev.getPlayer().isOnline() && ev.getPlayer().getPlayer().getKiller() != null)
        {
            getKillsFor(ev.getPlayer().getUniqueId()).add(ev.getPlayer().getPlayer().getKiller().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerResurrect(final PlayerResurrectedEvent ev)
    {
        kills.values().forEach(playerKills -> playerKills.remove(ev.getPlayer().getUniqueId()));
    }
}
