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

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.AllianceRequest;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.AlliancesModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.Config;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzteams.QuartzTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CommandInfo (name = "alliance-request", usageParameters = "<player>", aliases = {"alliancerequest", "alliance", "ally"})
public class AllianceRequestCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        if (QSG.module(GameModule.class).currentPhaseBefore(GamePhase.IN_GAME))
        {
            error(I.t("The game is not started."));
        }
        else if (args.length == 0)
        {
            throwInvalidArgument(I.t("You must provide a player name."));
        }

        final AlliancesModule alliances = QSG.module(AlliancesModule.class);

        final Player requested = getPlayerParameter(0);

        if (requested.getUniqueId().equals(playerSender().getUniqueId()))
        {
            error(I.t("You cannot create an alliance with yourself."));
        }
        else if (alliances.getRequestByCouple(playerSender().getUniqueId(), requested.getUniqueId()) != null)
        {
            error(I.t("You already have an ongoing alliance request sent to {0}. Please be patient!", requested.getName()));
        }

        final AllianceRequest request = new AllianceRequest(playerSender().getUniqueId(), requested.getUniqueId());

        QSG.log().info("New request: {0} - {1}", request, request.getError());

        // The rule to send error messages is: no answer should not be distinguishable from
        // errors linked to existing alliances, because the alliances are secret.

        switch (request.getError())
        {
            case TOO_FAR:
                error(I.t("{0} is too far. You must be within {1} blocks of each other.", requested.getName(), Config.MAX_DISTANCE_TO_CREATE_AN_ALLIANCE.get()));
                return;

            case REQUESTER_OUT_OF_ALLIANCES:
                error(I.t("You can no longer join an alliance."));
                return;

            case REQUESTED_OUT_OF_ALLIANCES:
                yourRequestHasBeenSent(requested);

                requested.sendMessage("");
                requested.sendMessage(I.t("{gray}{bold}{0} just sent you an alliance request, but you cannot accept it because you have no alliance left.", sender.getName()));
                requested.sendMessage(I.t("{gray}{0} is not aware of this, but I preferred to warn you so that you would not be caught off guard if he or she mentions it.", sender.getName()));
                requested.sendMessage("");

                return;

            case FUTURE_ALLIANCE_TOO_BIG:
                // Here, if the player is solo, we say that the request was sent and we warn the
                // other alliance members. If the player is in an alliance he know its size and
                // the other one is solo.
                if (alliances.allianceSize(playerSender().getUniqueId()) == 1)
                {
                    yourRequestHasBeenSent(requested);

                    QuartzTeams.get().getTeamForPlayer(requested).getOnlinePlayers().forEach(teammate -> {
                        teammate.sendMessage("");
                        teammate.sendMessage(I.t("{gray}{bold}{0} just sent you an alliance request, but you cannot accept it because your alliance is already at full capacity.", sender.getName()));
                        teammate.sendMessage(I.t("{gray}{0} is not aware of this, but I preferred to warn you so that you would not be caught off guard if he or she mentions it.", sender.getName()));
                        teammate.sendMessage("");
                    });
                }
                else
                {
                    error(I.t("You cannot send a request alliance as your alliance is already at full capacity. Don't be too greedy!"));
                }

                return;

            case BOTH_IN_A_DIFFERENT_ALLIANCE:
                // Here we cannot inform the other alliance because they would know the sender is in an alliance.
                yourRequestHasBeenSent(requested);
                return;

            case BOTH_IN_THE_SAME_ALLIANCE:
                error(I.t("You're already allied with {0}!", requested.getName()));
                return;

            case WOULD_END_THE_GAME:
                error(I.t("You cannot create this alliance as it would end the game."));
                return;

            case OK:
                yourRequestHasBeenSent(requested);
                request.sendApprovalRequests();

                break;
        }
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingPlayerNames(Bukkit.getOnlinePlayers().stream().filter(player -> !player.equals(sender)).collect(Collectors.toSet()), args[0]);
        else return null;
    }

    private void yourRequestHasBeenSent(final Player to)
    {
        info("");
        success(I.t("{bold}Your request has been sent to {0}.", to.getName()));
        success(I.t("Wait until it is accepted. Let's hope you don't hear only silence..."));
        info("");
    }
}
