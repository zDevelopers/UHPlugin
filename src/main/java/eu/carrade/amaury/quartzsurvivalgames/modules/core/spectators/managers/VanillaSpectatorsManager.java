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


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Vanilla spectator mode
 */
public class VanillaSpectatorsManager extends SpectatorsManager
{
    /**
     * Stores the previous gamemodes of the players.
     */
    private Map<UUID, GameMode> oldGameModes = new HashMap<>();


    @Override
    public void setSpectating(final Player player, final boolean spectating)
    {
        if (player == null)
            return;

        if (spectating)
        {
            if (player.getGameMode() != GameMode.SPECTATOR)
            {
                oldGameModes.put(player.getUniqueId(), player.getGameMode());
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
        else
        {
            player.setGameMode(oldGameModes.getOrDefault(player.getUniqueId(), Bukkit.getDefaultGameMode()));
            oldGameModes.remove(player.getUniqueId());
        }
    }

    @Override
    public boolean isSpectating(Player player)
    {
        return player != null && player.getGameMode() == GameMode.SPECTATOR;
    }
}
