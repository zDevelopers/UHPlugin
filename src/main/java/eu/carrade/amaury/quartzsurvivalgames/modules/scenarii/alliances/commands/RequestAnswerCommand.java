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

package eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.commands;

import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.AllianceRequest;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.AlliancesModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.UUID;
import org.bukkit.entity.Player;

@CommandInfo(name = "alliance-request-answer", usageParameters = "<requestUUID> <yes|no> §8(internal)")
public class RequestAnswerCommand extends Command {
    @Override
    protected void run() throws CommandException {
        if (args.length < 2) {
            QSG.log(AlliancesModule.class)
                    .info("{0} (badly) used the alliance-request-answer directly.", sender.getName());
            throwInvalidArgument(
                    I.t("Invalid command usage. But, you shouldn't use this command directly. What are you doing?"));
        }

        try {
            final Player player = playerSender();
            final UUID requestID = UUID.fromString(args[0]);
            final boolean answer = getBooleanParameter(1);

            final AllianceRequest request = QSG.module(AlliancesModule.class).getRequestByID(requestID);

            if (request != null) {
                try {
                    QSG.log(AlliancesModule.class).info("{0}: reply from {1}: {2}", request, player.getName(), answer);
                    request.registerApproval(player.getUniqueId(), answer);
                }
                catch (IllegalArgumentException e) {
                    QSG.log(AlliancesModule.class)
                            .warning("{0}: {1} tried to reply {2} but was not in the approvers list.", request,
                                    player.getName(), answer);
                    error(I.t("You weren't asked for your opinion regarding this request."));
                }
            } else {
                error(I.t("This request has expired."));
            }
        }
        catch (IllegalArgumentException e) {
            QSG.log(AlliancesModule.class)
                    .info("{0} (badly) used the alliance-request-answer directly.", sender.getName());
            throwInvalidArgument("Malformed UUID.");
        }
    }
}
