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

package eu.carrade.amaury.UHCReloaded.task;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.Set;


public class FireworksOnWinnersTask extends BukkitRunnable
{
    private final Set<OfflinePlayer> winners;

    private Double areaSize;
    private Random rand;

    private long startTime;

    public FireworksOnWinnersTask(final Set<OfflinePlayer> listWinners)
    {
        this.winners = listWinners;

        this.areaSize = UHConfig.FINISH.FIREWORKS.AREA_SIZE.get();
        this.rand = new Random();

        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        // The fireworks are launched in a square centered on the player.
        final double halfAreaSize = areaSize / 2;

        for (final OfflinePlayer winner : winners)
        {
            if (winner.isOnline())
            {
                Location fireworkLocation = ((Player) winner).getLocation();

                fireworkLocation.add(rand.nextDouble() * areaSize - halfAreaSize, // a number between -halfAreaSize and halfAreaSize
                        2, // y+2 for a clean vision of the winner.
                        rand.nextDouble() * areaSize - halfAreaSize);

                UHUtils.generateRandomFirework(fireworkLocation.add(0.2, 0d, 0.2), 5, 15);
                UHUtils.generateRandomFirework(fireworkLocation.add(-0.2, 0d, 0.2), 5, 15);
            }
        }

        if ((System.currentTimeMillis() - startTime) / 1000 > UHConfig.FINISH.FIREWORKS.DURATION.get())
        {
            this.cancel();
        }
    }
}
