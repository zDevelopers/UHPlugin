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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.playersLoader;

import eu.carrade.amaury.quartzsurvivalgames.utils.OfflinePlayersLoader;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandInfo(
        name = "load-players",
        usageParameters = "<player1> [player2] [player3] […]",
        aliases = {"loadplayers", "load-player", "loadplayer"}
)
public class LoadPlayersCommand extends Command {
    @Override
    protected void run() {
        if (!Bukkit.getOnlineMode()) {
            sender.sendMessage(I.t("{ce}You cannot load unknown players in offline mode, sorry."));
            return;
        }

        if (args.length == 0) {
            /// Error returned if one calls /uh loadplayers without arguments.
            sender.sendMessage(I.t("{ce}You need to provide at least one player name."));
            return;
        }

        /// Message displayed when the /uh loadplayers command is used, as the execution may take some time.
        info(I.t("{cst}Loading players..."));

        final CommandSender fSender = sender;

        OfflinePlayersLoader.loadPlayers(
                Arrays.asList(args),
                addedPlayers -> fSender.sendMessage(
                        I.tn("{cs}Loaded {0} player successfully.", "{cs}Loaded {0} players successfully.",
                                addedPlayers.size())),
                notFound -> {
                    /// Message sent if some players cannot be loaded while /uh loadplayers is used. 0 = amount of players missing; 1 = list of nicknames (format "nick1, nick2, nick3").
                    fSender.sendMessage(I.tn("{ce}{0} player is missing: {1}.", "{ce}{0} players are missing: {1}.",
                            notFound.size(), notFound.size(), StringUtils.join(notFound, ", ")));
                }
        );
    }
}
