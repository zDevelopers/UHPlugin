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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.freezer;

import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * This command freezes the players.
 * <p>
 * Usage: /uh freeze <on [player]|off [player]|all|none>
 * - on [player]: freezes the given player, or the sender if no player was provided.
 * - off [player]: unfreezes the given player (or the sender, same condition).
 * - all: freezes all the alive players, the mobs and the timer.
 * - none: unfreezes all the alive players (even if there where frozen before using
 * /uh freeze all), the mobs and the timer.
 */
@CommandInfo(name = "freeze", usageParameters = "<on|off|all|none> [target]")
public class FreezeCommand extends Command {
    @Override
    public void run() throws CommandException {
        if (args.length == 0) {
            info(I.t("{aqua}------ Freeze commands ------"));
            info(I.t("{cc}/uh freeze on [player]{ci}: freezes a player, or the sender without a specified player."));
            info(I.t(
                    "{cc}/uh freeze off [player]{ci}: unfreezes a player (or the sender), even if the entire game is frozen."));
            info(I.t("{cc}/uh freeze all{ci}: freezes the entire game (players, mobs, timer)."));
            info(I.t(
                    "{cc}/uh freeze none{ci}: unfreezes the entire game. You NEED to execute this in order to relaunch the timer."));
            return;
        }

        final String subCommand = args[0];

        if (subCommand.equalsIgnoreCase("on") || subCommand.equalsIgnoreCase("off")) {
            final boolean on = subCommand.equalsIgnoreCase("on");

            // /uh freeze on: freezes the sender
            if (args.length == 1) {
                QSG.module(FreezerModule.class).setPlayerFreezeState(playerSender(), on);

                if (on) {
                    info(I.t("{cst}You where frozen by {0}.", sender.getName()));
                } else {
                    info(I.t("{cst}You where unfrozen by {0}.", sender.getName()));
                }
            }

            // /uh freeze on <player>: freezes <player>.
            else if (args.length == 2) {
                final Player player = getPlayerParameter(1);

                if (player == null) {
                    error(I.t("{ce}{0} is offline!", args[1]));
                } else {
                    QSG.module(FreezerModule.class).setPlayerFreezeState(player, on);

                    if (on) {
                        player.sendMessage(
                                QSGUtils.prefixedMessage(I.t("Freezer"),
                                        I.t("{cst}You where frozen by {0}.", sender.getName())));
                        success(I.t("{cs}{0} is now frozen.", player.getName()));
                    } else {
                        player.sendMessage(
                                QSGUtils.prefixedMessage(I.t("Freezer"),
                                        I.t("{cst}You where unfrozen by {0}.", sender.getName())));
                        success(I.t("{cs}{0} is now unfrozen.", player.getName()));
                    }
                }
            }
        } else if (subCommand.equalsIgnoreCase("all") || subCommand.equalsIgnoreCase("none")) {
            final boolean on = subCommand.equalsIgnoreCase("all");

            QSG.module(FreezerModule.class).setGlobalFreezeState(on);

            Bukkit.broadcastMessage("");
            if (on) {
                Bukkit.broadcastMessage(
                        QSGUtils.prefixedMessage(I.t("Freezer"), I.t("{darkaqua}The entire game is now frozen.")));
            } else {
                Bukkit.broadcastMessage(
                        QSGUtils.prefixedMessage(I.t("Freezer"), I.t("{darkaqua}The game is now unfrozen.")));
            }
            Bukkit.broadcastMessage("");
        }
    }

    @Override
    public List<String> complete() {
        if (args.length == 1) {
            return getMatchingSubset(args[0], "on", "off", "all", "none");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("off")) {
                return getMatchingSubset(
                        QSG.module(FreezerModule.class).getFrozenPlayers().stream()
                                .map(OfflinePlayer::getName)
                                .collect(Collectors.toSet()),
                        args[1]);
            } else if (args[0].equalsIgnoreCase("on")) {
                return getMatchingSubset(
                        QSG.game().getAlivePlayers().stream()
                                .filter(player -> !QSG.module(FreezerModule.class).isPlayerFrozen(player))
                                .map(OfflinePlayer::getName)
                                .collect(Collectors.toSet()),
                        args[1]
                );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
