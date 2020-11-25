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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.game.commands;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.teleporter.TeleportationMode;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.commands.WithFlags;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.List;
import org.bukkit.entity.Player;


@CommandInfo(name = "start", usageParameters = "[--slow] [--ignore-teams]")
@WithFlags({"slow", "ignore-teams"})
public class StartCommand extends Command {
    @Override
    protected void run() throws CommandException {
        final GameModule game = QSG.module(GameModule.class);

        switch (game.getPhase()) {
            case WAIT:
                game.setSlowMode(hasFlag("slow"));
                game.setTeleportationMode(
                        hasFlag("ignore-teams") ? TeleportationMode.IGNORE_TEAMS : TeleportationMode.NORMAL);

                if (hasFlag("slow")) {
                    if (sender instanceof Player) {
                        info("");
                    }
                    info(I.t("{green}{bold}The game is now starting."));
                    info(I.t(
                            "{green}Wait for the teleportation to finish; you'll then be prompted to start the game."));
                }

                game.setPhase(GamePhase.STARTING);

                break;

            case STARTING:
                try {
                    game.start();
                }
                catch (final IllegalStateException e) {
                    error(I.t("The starting process is not finished yet. Please be patient."));
                }

                break;

            default:
                error(I.t("{ce}The game is already started! Reload or restart the server to restart the game."));
        }
    }

    @Override
    protected List<String> complete() {
        return getMatchingSubset(args[args.length - 1], "--slow", "--ignore-teams");
    }
}
