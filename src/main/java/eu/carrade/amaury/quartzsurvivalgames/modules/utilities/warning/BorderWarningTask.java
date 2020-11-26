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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.warning;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.MapShape;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class BorderWarningTask extends BukkitRunnable {
    private final BorderModule borderModule = QSG.module(BorderModule.class);
    private final WarningModule warningModule = QSG.module(WarningModule.class);

    @Override
    public void run() {
//        final FreezerModule freezer = UR.module(FreezerModule.class);
//        if (freezer != null && freezer.getGlobalFreezeState())
//        {
//            return; // No messages are sent if the game is frozen.
//        }

        // Message sent to all players outside the border
        for (Player player : borderModule.getPlayersOutside(warningModule.getWarningSize())) {
            double distance = borderModule.getDistanceToBorder(player.getLocation(), warningModule.getWarningSize());

            if (borderModule.getMapShape() == MapShape.CIRCULAR) {
                player.sendMessage(I.tn("{ce}You are currently out of the future border (diameter of {0} block).",
                        "{ce}You are currently out of the future border (diameter of {0} blocks).",
                        warningModule.getWarningSize()));
            } else {
                player.sendMessage(I.t("{ce}You are currently out of the future border of {0}×{0} blocks.",
                        warningModule.getWarningSize()));
            }

            player.sendMessage(I.tn("{ci}You have {0} block to go before being inside.",
                    "{ci}You have {0} blocks to go before being inside.", (int) distance));
        }
    }
}
