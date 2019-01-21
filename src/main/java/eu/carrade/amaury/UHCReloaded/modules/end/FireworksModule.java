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
package eu.carrade.amaury.UHCReloaded.modules.end;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;


@ModuleInfo (
        name = "Fireworks",
        description = "When the game ends, this module sends some fireworks from the winners o/",
        when = ModuleLoadTime.ON_GAME_END,
        category = ModuleCategory.END,
        icon = Material.FIREWORK
)
public class FireworksModule extends UHModule
{
    private final int area = 6;  // Fireworks launched on a 6×6 area around the winners
    private final TimeDelta duration = new TimeDelta(10);

    @Override
    protected void onEnable()
    {
        RunTask.later(() ->
        {
            if (UR.game().getPhase() != GamePhase.END) return;

            final long start = System.currentTimeMillis();

            RunTask.timer(new BukkitRunnable() {
                @Override
                public void run()
                {
                    if (UR.game().getWinner() == null) return;

                    UR.game().getWinner().getOnlinePlayers().forEach(winner ->
                    {
                        final Location fireworkLocation = winner.getLocation();

                        fireworkLocation.add(
                                // a number between -area/2 and area/2
                                RandomUtils.nextDouble() * area - (area >> 1),

                                // y+2 for a clean vision of the winner.
                                2,

                                // a number between -area/2 and area/2
                                RandomUtils.nextDouble() * area - (area >> 1)
                        );

                        UHUtils.generateRandomFirework(fireworkLocation.add(4, 0, 4), 5, 15);
                        UHUtils.generateRandomFirework(fireworkLocation.add(4, 0, 4), 5, 15);
                    });

                    if ((System.currentTimeMillis() - start) / 1000 > duration.getSeconds())
                    {
                        cancel();
                    }
                }
            }, 0L, 30L);

        }, 5 * 20L);
    }
}
