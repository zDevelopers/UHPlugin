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

package eu.carrade.amaury.UHCReloaded.misc;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.ChatColor;


public class MOTDManager
{
    private UHCReloaded p;

    private boolean enabled;
    private String matchName = "";

    private String currentMOTD;

    public MOTDManager(UHCReloaded plugin)
    {
        p = plugin;

        enabled = p.getConfig().getBoolean("motd.enabled");

        if (enabled && p.getConfig().getBoolean("motd.displayMatchName"))
        {
            matchName = ChatColor.translateAlternateColorCodes('&', p.getConfig().getString("motd.matchNamePrefix")) + p.getScoreboardManager().getScoreboardName() + ChatColor.RESET + "\n";
        }
    }

    /**
     * Returns the current MOTD.
     *
     * @return The MOTD.
     */
    public String getCurrentMOTD()
    {
        return currentMOTD;
    }

    /**
     * Returns true if the state-based MOTDs are enabled.
     *
     * @return true if enabled.
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Updates the MOTD to the one displayed before the game start.
     */
    public void updateMOTDBeforeStart()
    {
        /// MOTD when the game is not started.
        if (enabled) currentMOTD = matchName + I.t("Waiting for players...");
    }

    /**
     * Updates the MOTD to the one displayed during the start.
     */
    public void updateMOTDDuringStart()
    {
        /// MOTD when the game is starting (slow TP in progress).
        if (enabled) currentMOTD = matchName + I.t("Starting in progress...");
    }

    /**
     * Updates the MOTD to the one displayed during the game (includes alive counts).
     * <p>
     * This need to be called on each death, to update alive counts.
     */
    public void updateMOTDDuringGame()
    {
        if (enabled)
        {
            if (!p.getGameManager().isGameWithTeams())
            {
                /// Solo game running MOTD. {0} = players alive count.
                currentMOTD = matchName + I.tn("Game running! {0} player alive.", "Game running! {0} players alive.", p.getGameManager().getAlivePlayersCount(), p.getGameManager().getAlivePlayersCount());
            }
            else
            {
                /// Teams game running MOTD. {0} = players alive count. {1} = teams alive count. Plural based on players count.
                currentMOTD = matchName + I.tn("Game running! {0} player alive in {1} team.", "Game running! {0} players alive in {1} teams.", p.getGameManager().getAlivePlayersCount(), p.getGameManager().getAlivePlayersCount(), p.getGameManager().getAliveTeamsCount());
            }
        }
    }

    /**
     * Updates the MOTD after the game.
     *
     * @param winner The winner.
     */
    public void updateMOTDAfterGame(UHTeam winner)
    {
        if (enabled)
        {
            if (!p.getGameManager().isGameWithTeams())
            {
                /// Game finished MOTD with solo winner ({0} = winner raw name).
                currentMOTD = matchName + I.t("Game finished; congratulation to {0} for his victory!", winner.getName());
            }
            else
            {
                /// Game finished MOTD with team winner ({0} = team display name).
                currentMOTD = matchName + I.t("Game finished; the team {0} wins this match!", winner.getDisplayName());
            }
        }
    }
}
