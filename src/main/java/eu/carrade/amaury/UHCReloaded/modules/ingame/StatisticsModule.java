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
package eu.carrade.amaury.UHCReloaded.modules.ingame;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;


@ModuleInfo (
        name = "Statistics",
        description = "If enabled, statistics will not be collected before the game" +
                "and will be reset at the beginning of the game. Disable if you want " +
                "to keep old statistics!",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.OTHER,
        icon = Material.WORKBENCH
)
public class StatisticsModule extends UHModule
{
    @EventHandler
    public void onPlayerStatisticIncrement(final PlayerStatisticIncrementEvent ev)
    {
        if (UR.game().getPhase() == GamePhase.WAIT)
        {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameStart(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.IN_GAME && ev.isRunningForward())
        {
            UR.game().getAliveConnectedPlayers().forEach(player -> {
                for (final Statistic statistic : Statistic.values())
                {
                    switch (statistic.getType())
                    {
                        case UNTYPED:
                            player.setStatistic(statistic, 0);
                            break;

                        case ITEM:
                        case BLOCK:
                            for (final Material material : Material.values())
                            {
                                try
                                {
                                    player.setStatistic(statistic, material, 0);
                                }
                                catch (final IllegalArgumentException ignored) {}
                            }
                            break;

                        case ENTITY:
                            for (final EntityType entityType : EntityType.values())
                            {
                                try
                                {
                                    player.setStatistic(statistic, entityType, 0);
                                }
                                catch (final Exception ignored) {}
                            }
                            break;
                    }
                }
            });
        }
    }
}
