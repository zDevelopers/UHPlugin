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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.managers;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.SpectatorPlusDependency;
import fr.zcraft.quartzlib.core.QuartzLib;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


/**
 * Represents a spectator manager, able to put players in or remove players from spectator mode.
 */
public abstract class SpectatorsManager {
    private final static SpectatorPlusDependency spectatorPlusDependency;

    static {
        spectatorPlusDependency = QuartzLib.loadComponent(SpectatorPlusDependency.class);
    }

    /**
     * @return an instance of a {@link SpectatorsManager}: {@link SPlusSpectatorsManager} if the
     * SpectatorPlus plugin is available; {@link VanillaSpectatorsManager} else.
     */
    public static SpectatorsManager getInstance() {
        if (spectatorPlusDependency.isEnabled()) {
            return new SPlusSpectatorsManager(spectatorPlusDependency.getSPAPI());
        } else {
            return new VanillaSpectatorsManager();
        }
    }

    /**
     * Changes the spectating mode of a player.
     *
     * @param player     The player.
     * @param spectating {@code true} to enable the spectator mode; {@code false} to disable it.
     */
    public abstract void setSpectating(final Player player, final boolean spectating);

    /**
     * Checks if the given player is currently spectating.
     *
     * @param player The player.
     * @return {@code true} if spectating.
     */
    public abstract boolean isSpectating(final Player player);

    /**
     * Changes the spectating mode of a player.
     *
     * @param playerID   The player's UUID.
     * @param spectating {@code true} to enable the spectator mode; {@code false} to disable it.
     */
    public void setSpectating(final UUID playerID, final boolean spectating) {
        setSpectating(Bukkit.getPlayer(playerID), spectating);
    }

    /**
     * Checks if the given player is currently spectating.
     *
     * @param playerID The player's UUID.
     * @return {@code true} if spectating.
     */
    public boolean isSpectating(final UUID playerID) {
        return isSpectating(Bukkit.getPlayer(playerID));
    }
}
