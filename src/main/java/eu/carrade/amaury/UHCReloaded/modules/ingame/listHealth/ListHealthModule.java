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
package eu.carrade.amaury.UHCReloaded.modules.ingame.listHealth;

import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;


@ModuleInfo (
        name = "Health in Players List",
        description = "Displays the health of players in the overlay list displayed with <TAB>.",
        when = ModuleInfo.ModuleLoadTime.ON_GAME_STARTING
)
public class ListHealthModule extends UHModule
{
    @Override
    protected void onEnable()
    {
        // Initialization of the scoreboard (health in players' list)
        final Objective healthObjective = UR.get().getScoreboard().registerNewObjective("Health", Criterias.HEALTH);
        healthObjective.setDisplayName("Health");
        healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        // Sometimes, the health is initialized to 0. This is used to fix this.
        updateHealthScore();
    }

    @Override
    protected void onDisable()
    {
        UR.get().getScoreboard().clearSlot(DisplaySlot.PLAYER_LIST);
    }

    /**
     * Updates the health score for all players.
     */
    public void updateHealthScore()
    {
        Bukkit.getOnlinePlayers().forEach(this::updateHealthScore);
    }

    /**
     * Updates the health score for the given player.
     *
     * @param player The player to update.
     */
    public void updateHealthScore(final Player player)
    {
        if (player.getHealth() != 1d) // Prevents killing the player
        {
            player.setHealth(player.getHealth() - 1d);

            RunTask.later(() ->
            {
                if (player.getHealth() <= player.getMaxHealth() - 1d) // Avoids an IllegalArgumentException
                {
                    player.setHealth(player.getHealth() + 1d);
                }
            }, 3L);
        }
    }
}
