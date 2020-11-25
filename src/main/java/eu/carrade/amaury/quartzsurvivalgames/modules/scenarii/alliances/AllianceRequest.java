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
package eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.external.hawk.HawkModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.commands.RequestAnswerCommand;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGSound;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.colors.TeamColor;
import me.cassayre.florian.hawk.report.ReportEvent;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AllianceRequest
{
    private final GameModule game;
    private final AlliancesModule alliances;

    private final UUID allianceRequestUUID = UUID.randomUUID();

    private final UUID requesterID;
    private final UUID requestedID;
    private final Map<UUID, Boolean> approvalsIDs = new HashMap<>();

    private final QuartzTeam requesterTeam;
    private final QuartzTeam requestedTeam;

    private final RequestError requestError;

    public AllianceRequest(final UUID requesterID, final UUID requestedTo)
    {
        Validate.notNull(requesterID, "The requester must not be null");
        Validate.notNull(requestedTo, "The requested must not be null");

        game = QSG.module(GameModule.class);
        alliances = QSG.module(AlliancesModule.class);

        this.requesterID = requesterID;
        this.requestedID = requestedTo;

        this.requesterTeam = QuartzTeams.get().getTeamForPlayer(requesterID);
        this.requestedTeam = QuartzTeams.get().getTeamForPlayer(requestedTo);

        final Player requesterPlayer = Bukkit.getPlayer(requesterID);
        final Player requestedPlayer = Bukkit.getPlayer(requestedID);


        // Cases of request incompatibility:
        // - not both the requester and the requested are online;
        // - the requester is too far from the requested;
        // - the requester is out of alliances for the amount requested (the amount being
        //   the future amount of players in the new alliance);
        // - the requested is out of alliances for the amount requested;
        // - both the requester and the requested are in an alliance (at least one of them
        //   must be solo, or have their teammate dead);
        // - both the requester and the requested are in the same alliance (obviously);
        // - such an alliance would end the game (e.g. if there is two players left, they
        //   cannot ally).

        // We first check for errors

        if (requestedTeam.equals(requesterTeam))
        {
            requestError = RequestError.BOTH_IN_THE_SAME_ALLIANCE;
        }
        else if (requesterPlayer == null || !requesterPlayer.isOnline() || requestedPlayer == null || !requestedPlayer.isOnline())
        {
            requestError = RequestError.TOO_FAR;
        }
        else if (!requesterPlayer.getWorld().equals(requestedPlayer.getWorld()))
        {
            requestError = RequestError.TOO_FAR;
        }
        else if (requesterPlayer.getLocation().distanceSquared(requestedPlayer.getLocation()) > Math.pow(Config.MAX_DISTANCE_TO_CREATE_AN_ALLIANCE.get(), 2))
        {
            requestError = RequestError.TOO_FAR;
        }
        else if (!checkAlliancesLeft(requesterID))
        {
            requestError = RequestError.REQUESTER_OUT_OF_ALLIANCES;
        }
        else if (!checkAlliancesLeft(requestedTo))
        {
            requestError = RequestError.REQUESTED_OUT_OF_ALLIANCES;
        }
        else if (alliances.allianceSize(requestedTeam) > 1 && alliances.allianceSize(requesterTeam) > 1)
        {
            requestError = RequestError.BOTH_IN_A_DIFFERENT_ALLIANCE;
        }
        else if (!checkFutureAllianceSize())
        {
            requestError = RequestError.FUTURE_ALLIANCE_TOO_BIG;
        }
        else if (!checkGameEnd())
        {
            requestError = RequestError.WOULD_END_THE_GAME;
        }
        else
        {
            requestError = RequestError.OK;
        }

        if (requestError != RequestError.OK) return;


        // Okay so here we have a valid request. Yay!

        // Possible cases
        // - If both are in a solo team, it's a request for a two-players alliance
        // - If the requester is in a two-players alliance, an invite is sent to the
        //   third-party player to join the alliance.
        // - If the requester is solo but the requested in a two-players alliance, an
        //   invite is sent to the requested and the other member of the alliance, to
        //   be sure everyone is OK.
        // - And so on until the maximal amount of players per alliance is reached.
        //
        // To summarize, the request is sent to:
        // - the requested player, of course;
        // - if the requester is in an alliance, other players in the alliance;
        // - if the requested is in an alliance, other players in that alliance.

        approvalsIDs.put(requestedTo, false);

        if (requesterTeam.size() > 1 || requestedTeam.size() > 1)
        {
            (requestedTeam.size() > 1 ? requestedTeam : requesterTeam)
                    .getPlayers().stream()
                    .map(OfflinePlayer::getUniqueId)
                    .filter(uuid -> !uuid.equals(requesterID))
                    .forEach(player -> approvalsIDs.put(player, false));
        }

        // We don't forget to self-register as an ongoing request.

        alliances.registerRequest(this);
    }

    /**
     * Sends an approval request to the players.
     */
    public void sendApprovalRequests()
    {
        checkError();

        final QSGSound[] jingle = new QSGSound[] {
                new QSGSound(1f, 0.8f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.4f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.1f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT")
        };

        approvalsIDs.keySet().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> {
                player.sendMessage("");

                player.sendMessage(I.t("{green}{bold}{0} would like to join you in an alliance.", getName(requesterID)));

                if (!player.getUniqueId().equals(requestedID))
                {
                    player.sendMessage(I.t("{gray}This request was sent to {0}, but everyone in the alliance also need to accept.", getName(requestedID)));
                }

                player.sendMessage("");

                RawMessage.send(player, new RawText("   ")
                        .then(I.t("{darkgreen}{bold}»  {green}{bold}Accept  {darkgreen}{bold}«"))
                            .command(RequestAnswerCommand.class, allianceRequestUUID.toString(), "yes")
                            .hover(I.t("{green}{bold}Accept {green}this alliance"))
                        .then("         ")
                        .then(I.t("{darkred}{bold}»  {red}{bold}Decline  {darkred}{bold}«"))
                            .command(RequestAnswerCommand.class, allianceRequestUUID.toString(), "no")
                            .hover(I.t("{red}{bold}Decline {red}this alliance"))
                        .build()
                );

                player.sendMessage("");

                for (int i = 0; i < jingle.length; i++)
                {
                    final int index = i;
                    RunTask.later(() -> jingle[index].play(player), i * 5L);
                }
            });
    }

    public void registerApproval(final UUID approver, final boolean approval)
    {
        checkError();

        if (!approvalsIDs.containsKey(approver))
        {
            throw new IllegalArgumentException("This player was not asked for approval");
        }

        if (!approval)
        {
            denyAndClose(approver);
        }
        else
        {
            approvalsIDs.put(approver, true);

            // Missing approvers for notifications
            final int missingApproversCount = (int) approvalsIDs.values().stream().filter(answer -> !answer).count();
            final String missingApprovers = missingApproversCount > 0
                    ? " " + I.tn("{gray}Still waiting for {0}'s answer.", "{gray}Still waiting for answers from: {0}.", missingApproversCount, String.join(", ", approvalsIDs.keySet().stream().filter(id -> !approvalsIDs.get(id)).map(Bukkit::getOfflinePlayer).map(OfflinePlayer::getName).collect(Collectors.toSet())))
                    : "";

            // We notify the player who just approved the request.
            if (missingApproversCount > 0)
            {
                final Player player = Bukkit.getPlayer(approver);
                if (player != null && player.isOnline())
                {
                    final UUID allianceWith;

                    if (requesterTeam.containsPlayer(player)) allianceWith = requestedID;
                    else allianceWith = requesterID;

                    player.sendMessage(I.t("You accepted the request for an alliance with {0}.", getName(allianceWith)));
                }
            }

            // If one of the sides is an existing alliance, we notify the players in that
            // alliance.
            final QuartzTeam notifiedTeam;
            final UUID allianceWith;

            if (alliances.allianceSize(requesterTeam) > 1)
            {
                notifiedTeam = requesterTeam;
                allianceWith = requestedID;
            }
            else if (alliances.allianceSize(requestedTeam) > 1)
            {
                notifiedTeam = requestedTeam;
                allianceWith = requesterID;
            }
            else
            {
                 notifiedTeam = null;
                 allianceWith = null;
            }

            if (notifiedTeam != null)
            {
                for (Player player : requesterTeam.getOnlinePlayers())
                {
                    if (game.isAlive(player) && !player.getUniqueId().equals(approver))
                    {
                        player.sendMessage(I.t("{green}The request for an alliance with {0} was accepted by {1}.", getName(allianceWith), getName(approver)) + missingApprovers);
                    }
                }
            }

            // If everyone agree
            if (approvalsIDs.values().stream().allMatch(answer -> answer))
            {
                applyApprovedRequest();
            }
        }
    }

    private void denyAndClose(final UUID closedBy)
    {
        // If one of the players deny the request, it is closed. All players must agree.
        // If the request was closed by one of the members of an existing alliance, players
        // *of this alliance only* are notified.

        alliances.unregisterRequest(this);

        final QuartzTeam notifiedTeam;
        final OfflinePlayer allianceWith;
        final OfflinePlayer closer = Bukkit.getOfflinePlayer(closedBy);

        if (alliances.allianceSize(requesterTeam) > 1 && requesterTeam.containsPlayer(closedBy))
        {
            notifiedTeam = requesterTeam;
            allianceWith = Bukkit.getOfflinePlayer(requestedID);
        }
        else if (alliances.allianceSize(requestedTeam) > 1 && requestedTeam.containsPlayer(closedBy))
        {
            notifiedTeam = requestedTeam;
            allianceWith = Bukkit.getOfflinePlayer(requesterID);
        }
        else
        {
            notifiedTeam = null;
            allianceWith = null;
        }

        if (notifiedTeam != null)
        {
            notifiedTeam.getOnlinePlayers().stream().filter(player -> !player.getUniqueId().equals(closedBy)).forEach(
                    player -> {
                        player.sendMessage("");
                        player.sendMessage(I.t("{red}The alliance with {0} was {bold}denied{red} by {1}.", allianceWith.getName(), closer.getName()));
                        player.sendMessage("");
                    }
            );
        }

        if (closer.isOnline()) // Should always be true in normal context
        {
            closer.getPlayer().sendMessage(I.t("{red}You declined {0}'s alliance request.", getName(requesterID)));
        }
    }

    private void applyApprovedRequest()
    {
        // In all cases we unregisters this request.

        alliances.unregisterRequest(this);


        // We first check if we still can approve this request.

        if (!checkAlliancesLeft(requestedID)
                || !checkAlliancesLeft(requesterID)
                || !checkGameEnd()
                || (alliances.allianceSize(requestedTeam) > 1 && alliances.allianceSize(requesterTeam) > 1))
        {
            QSG.log(AlliancesModule.class).warning("The alliance request from {0} to {1} was about to be approved but is now invalid.", getName(requesterID), getName(requestedID));

            Stream.of(requesterID, requestedID)
                    .map(Bukkit::getPlayer).filter(Objects::nonNull)
                    .forEach(player -> player.sendMessage(I.t("{ce}This alliance request is no longer valid. Please re-send it.")));
            return;
        }


        // We create a new team for this new alliance. Old teams are dropped.

        final QuartzTeam allianceTeam = QuartzTeams.get().createTeam(I.t("Your alliance"), TeamColor.WHITE);

        final Set<UUID> alliancePlayers = Stream.of(requesterTeam, requestedTeam)
                .flatMap(team -> team.getPlayersUUID().stream())
                .filter(game::isAlive)
                .collect(Collectors.toSet());

        allianceTeam.addAll(alliancePlayers);

        requestedTeam.deleteTeam();
        requesterTeam.deleteTeam();


        // We consume the alliances

        alliances.consumeAlliance(requesterID, allianceTeam.size() - 1);
        alliances.consumeAlliance(requestedID, allianceTeam.size() - 1);


        // We notify the players

        final QSGSound[] jingle = new QSGSound[] {
                new QSGSound(1f, 1.10f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.22f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.33f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.44f, "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT"),
                new QSGSound(1f, 1.80f, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", "LEVELUP")
        };

        allianceTeam.getOnlinePlayers().forEach(player -> {
            player.sendMessage("");

            if (allianceTeam.size() == 2)
            {
                player.sendMessage(I.t("{green}{bold}You are now allied with {0}!", allianceTeam.getPlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).findAny().map(OfflinePlayer::getName).orElse("<Unknown>")));
                player.sendMessage(I.t("{green}Your objective is to win together. But chhhh! Other players are not aware of your alliance..."));
            }
            else
            {
                player.sendMessage(I.t("{green}{bold}The alliance expands!"));
                player.sendMessage(I.t("{gray}Players in the alliance: {0}", String.join(", ", allianceTeam.getPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toSet()))));
            }

            player.sendMessage("");

            for (int i = 0; i < jingle.length; i++)
            {
                final int index = i;
                RunTask.later(() -> jingle[index].play(player), i * 3L);
            }
        });


        // A small log message

        QSG.log(AlliancesModule.class).info(
                "{0} succeeded! A new alliance was created between these players: {1}.",
                this,
                String.join(", ", allianceTeam.getPlayers().stream()
                        .map(player -> player.getName() + " (a=" + alliances.getAlliancesLeft(player.getUniqueId()) + ")")
                        .collect(Collectors.toSet())
                )
        );


        // Also in the timeline

        QSG.ifLoaded(HawkModule.class, hawk -> {
            if (allianceTeam.size() == 2)
            {
                final Iterator<OfflinePlayer> players = allianceTeam.getPlayers().iterator();

                hawk.getReport().record(ReportEvent.withIcon(
                        I.t("A new alliance is founded!"),
                        I.t("Between {0} and {1}", players.next().getName(), players.next().getName()),
                        "block-structure-block-data"
                ));
            }
            else
            {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                final OfflinePlayer joiningPlayer = requestedTeam.size() == 1 ? requestedTeam.getPlayers().stream().findFirst().get() : requesterTeam.getPlayers().stream().findFirst().get();

                final List<String> playersNames = allianceTeam.getPlayers().stream().filter(player -> !player.equals(joiningPlayer)).map(OfflinePlayer::getName).collect(Collectors.toList());
                final int size = playersNames.size();

                final StringBuilder sentence = new StringBuilder(size * 16);

                for (int i = 0; i < size; i++)
                {
                    sentence.append(playersNames.get(i));

                    if (i == size - 2)
                    {
                        sentence.append(" ").append(I.t("and")).append(" ");
                    }
                    else if (i != size - 1)
                    {
                        sentence.append(", ");
                    }
                }

                hawk.getReport().record(ReportEvent.withIcon(
                        I.t("The alliance is growing!"),
                        I.t("{0} joins {1}", joiningPlayer.getName(), sentence),
                        "block-structure-block"
                ));
            }

            allianceTeam.getPlayers().forEach(player -> hawk.getReport().getPlayer(player).setTagLine(
                    I.t("Allied"),
                    null,
                    I.t("Players in the (latest) alliance: {0}", String.join(", ", allianceTeam.getPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toSet())))
            ));
        });
    }

    /**
     * @return The request's UUID, used for request identification in commands.
     */
    public UUID getUniqueId()
    {
        return allianceRequestUUID;
    }

    /**
     * @return The request sender's UUID.
     */
    public UUID getRequesterID()
    {
        return requesterID;
    }

    /**
     * @return The request's target UUID.
     */
    public UUID getRequestedID()
    {
        return requestedID;
    }

    /**
     * @return The error for this request (including « OK »).
     */
    public RequestError getError()
    {
        return requestError;
    }

    /**
     * A shortcut to get a player's name.
     *
     * @param playerID A player's UUID.
     * @return The player name as a string.
     */
    private String getName(final UUID playerID)
    {
        return Optional.of(Bukkit.getOfflinePlayer(playerID)).map(OfflinePlayer::getName).orElse("<Unknown>");
    }

    /**
     * Checks if the future alliance formed if the request is accepted is not too big according
     * to the configuration.
     *
     * @return {@code true} if the future size is OK.
     */
    private boolean checkFutureAllianceSize()
    {
        return alliances.allianceSize(requestedTeam) + alliances.allianceSize(requesterTeam) <= Config.MAX_PLAYERS_PER_ALLIANCE.get();
    }

    /**
     * Checks if the given player have sufficient alliances left for this request.
     *
     * @param checkedPlayerID The player to check.
     * @return {@code true} if there are enough alliances left.
     */
    private boolean checkAlliancesLeft(final UUID checkedPlayerID)
    {
        return alliances.getAlliancesLeft(checkedPlayerID) >= 1;
    }

    /**
     * Checks if this alliance would end the game.
     *
     * @return {@code true} if this alliance would <strong>not</strong> end the game.
     */
    private boolean checkGameEnd()
    {
        final Set<UUID> playersInFutureAlliance = Stream.of(requestedTeam.getPlayers(), requesterTeam.getPlayers())
                .flatMap(Collection::stream)
                .map(OfflinePlayer::getUniqueId)
                .collect(Collectors.toSet());
        return game.getAlivePlayersUUIDs().stream().anyMatch(player -> !playersInFutureAlliance.contains(player));
    }

    /**
     * Throws an {@link IllegalStateException} if the request has errored.
     */
    private void checkError()
    {
        if (requestError != RequestError.OK) throw new IllegalStateException("This alliance request errored.");
    }

    @Override
    public String toString()
    {
        return "AllianceRequest [from " + getName(requesterID) + " (n=" + requesterTeam.size() + ", a=" + alliances.getAlliancesLeft(requesterID) + ") to " + getName(requestedID) + " (n=" + requesterTeam.size() + ", a=" + alliances.getAlliancesLeft(requesterID) + ")]";
    }

    /**
     * The request error state. If not OK, all methods except {@link #getError()} will throw
     * an {@link IllegalStateException}.
     */
    public enum RequestError
    {
        OK,
        TOO_FAR,
        REQUESTER_OUT_OF_ALLIANCES,
        REQUESTED_OUT_OF_ALLIANCES,
        FUTURE_ALLIANCE_TOO_BIG,
        BOTH_IN_A_DIFFERENT_ALLIANCE,
        BOTH_IN_THE_SAME_ALLIANCE,
        WOULD_END_THE_GAME
    }
}
