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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.game.teleporter;

import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.tools.Callback;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * @see Teleporter
 */
class TeleportationRunnable extends BukkitRunnable {
    private final Teleporter teleporter;
    private final Queue<UUID> teleportationQueue;

    private final Callback<UUID> onTeleportation;
    private final Callback<UUID> onTeleportationSuccessful;
    private final Callback<UUID> onTeleportationFailed;
    private final Callback<Set<UUID>> onTeleportationProcessFinished;

    private final Set<UUID> failed = new HashSet<>();

    public TeleportationRunnable(Teleporter teleporter, Set<UUID> playersToTeleport, Callback<UUID> onTeleportation,
                                 Callback<UUID> onTeleportationSuccessful, Callback<UUID> onTeleportationFailed,
                                 Callback<Set<UUID>> onTeleportationProcessFinished) {
        this.teleporter = teleporter;
        this.onTeleportation = onTeleportation;
        this.onTeleportationSuccessful = onTeleportationSuccessful;
        this.onTeleportationFailed = onTeleportationFailed;
        this.onTeleportationProcessFinished = onTeleportationProcessFinished;

        this.teleportationQueue = new ArrayDeque<>(playersToTeleport);
    }

    @Override
    public void run() {
        try {
            UUID player = teleportationQueue.remove();

            QSGUtils.callIfDefined(onTeleportation, player);

            if (teleporter.teleportPlayer(player, false)) {
                QSGUtils.callIfDefined(onTeleportationSuccessful, player);
            } else {
                QSGUtils.callIfDefined(onTeleportationFailed, player);
                failed.add(player);
            }
        }
        catch (NoSuchElementException e) // Queue empty
        {
            QSGUtils.callIfDefined(onTeleportationProcessFinished, failed);
            cancel();
        }
    }
}
