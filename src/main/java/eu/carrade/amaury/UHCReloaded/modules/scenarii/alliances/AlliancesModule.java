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
package eu.carrade.amaury.UHCReloaded.modules.scenarii.alliances;

import com.google.common.collect.ImmutableMap;
import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.start.BeforeTeleportationPhaseEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.modules.scenarii.alliances.commands.AllianceRequestCommand;
import eu.carrade.amaury.UHCReloaded.modules.scenarii.alliances.commands.RequestAnswerCommand;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.colors.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;
import java.util.stream.Collectors;


@ModuleInfo (
        name = "Alliances Scenario",
        description = "If enabled, the game will be alliances-based.\n\n" +
                "To use this scenario, start without any team configured. All players " +
                "will be alone at the beginning. If they meet someone, they can ask for " +
                "an alliance with this player. If everyone is OK, they form a new team, " +
                "but they are the only ones to know, as players names are not colored.\n\n" +
                "An existing alliance can recruit a new ally using the same mechanism: " +
                "if a player ask for an alliance to another player in an existing alliance, " +
                "the two players in the alliance will be notified and will have to accept. " +
                "If a player in an existing alliance want to invite someone, both the potential " +
                "new ally and the other player in the existing alliance will have to agree.\n\n" +
                "The number of alliances over time for a given player is limited. You start with " +
                "an amount of alliances (default 2). If you create a two-players alliance, you lose " +
                "one alliance. If you form a three-players alliance, you lose two alliances. If you " +
                "run out of alliances, you can no longer form new alliances, and requests sent to you " +
                "will be silently declined.",
        when = ModuleLoadTime.ON_GAME_STARTING,
        category = ModuleCategory.SCENARII,
        icon = Material.PAPER,
        settings = Config.class,
        can_be_loaded_late = false
)
public class AlliancesModule extends UHModule
{
    private GameModule game = null;

    private final Map<UUID, Integer> alliancesLeft = new HashMap<>();
    private final Map<UUID, AllianceRequest> ongoingRequests = new HashMap<>();

    @Override
    protected void onEnable()
    {
        game = UR.module(GameModule.class);

        // We update some teams settings

        eu.carrade.amaury.UHCReloaded.modules.core.game.Config.SIDEBAR.TEAMS.set(false);
        eu.carrade.amaury.UHCReloaded.modules.core.teams.Config.SIDEBAR.TITLE.USE_TEAM_NAME.set(true);

        ZTeams.settings()
            .setTeamsOptions(
                eu.carrade.amaury.UHCReloaded.modules.core.teams.Config.CAN_SEE_FRIENDLY_INVISIBLES.get(),
                false,
                false,
                true // Important! As we use the same team names for lots of teams, as titles.
            )
            .setMaxPlayersPerTeam(Config.MAX_PLAYERS_PER_ALLIANCE.get());


        // ...And permissions

        ZTeams.setPermissionsChecker(new TeamsPermissionsChecker(ZTeams.get().permissionsChecker()));
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Arrays.asList(AllianceRequestCommand.class, RequestAnswerCommand.class);
    }

    @Override
    public Map<String, Class<? extends Command>> getCommandsAliases()
    {
        return ImmutableMap.of(
                "alliance", AllianceRequestCommand.class,
                "a", AllianceRequestCommand.class
        );
    }


    /**
     * Inits the count of left alliances to the configured max if not already stored.
     * @param playerID The player to initialize the count for.
     */
    private void initAlliancesCountIfRequired(final UUID playerID)
    {
        alliancesLeft.putIfAbsent(playerID, Config.ALLIANCES_PER_PLAYER.get());
    }

    /**
     * Consumes an amount of alliances for the given player.
     *
     * @param playerID The player's ID.
     * @param amount The amount to consume.
     */
    public void consumeAlliance(final UUID playerID, final int amount)
    {
        initAlliancesCountIfRequired(playerID);
        alliancesLeft.put(playerID, Math.max(0, alliancesLeft.get(playerID) - amount));
    }

    /**
     * Returns the amount of alliances left for the given player.
     *
     * @param playerID The player's ID.
     * @return The amount of alliances left.
     */
    public int getAlliancesLeft(final UUID playerID)
    {
        initAlliancesCountIfRequired(playerID);
        return alliancesLeft.get(playerID);
    }

    /**
     * Checks if we can borrow the given amount of alliances to a player. If so,
     * borrows them and returns {@code true}. Else, returns {@code false}.
     *
     * @param playerID The player's ID.
     * @param amount The amount of alliances to borrow.
     *
     * @return {@code true} if they could (and were) borrowed.
     */
    public boolean consumeAlliancesIfPossible(final UUID playerID, final int amount)
    {
        if (getAlliancesLeft(playerID) >= amount)
        {
            consumeAlliance(playerID, amount);
            return true;
        }
        else return false;
    }


    public void registerRequest(final AllianceRequest request)
    {
        ongoingRequests.put(request.getUniqueId(), request);
    }

    public void unregisterRequest(final AllianceRequest request)
    {
        ongoingRequests.remove(request.getUniqueId());
    }

    public AllianceRequest getRequestByID(final UUID requestID)
    {
        return ongoingRequests.get(requestID);
    }

    public Set<AllianceRequest> getRequestsBySender(final UUID requestSenderID)
    {
        return ongoingRequests.values().stream()
                .filter(request -> request.getRequesterID().equals(requestSenderID))
                .collect(Collectors.toSet());
    }

    public AllianceRequest getRequestByCouple(final UUID requestSenderID, final UUID requestedID)
    {
        return ongoingRequests.values().stream()
                .filter(request -> request.getRequesterID().equals(requestSenderID))
                .filter(request -> request.getRequestedID().equals(requestedID))
                .findAny().orElse(null);
    }

    public int allianceSize(UUID playerID)
    {
        final ZTeam team = ZTeams.get().getTeamForPlayer(playerID);

        if (team != null) return allianceSize(team);
        else return 1;
    }

    public int allianceSize(final ZTeam team)
    {
        return (int) team.getPlayers().stream().filter(game::isAlive).count();
    }


    /**
     * Creates teams for each player before the beginning of the game: the teams will be
     * indistinguishable, and the game will be considered as a teams game, even with
     * everyone in solo.
     */
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPreSpawnSelection(final BeforeTeleportationPhaseEvent ev)
    {
        final SpectatorsModule spectators = UR.module(SpectatorsModule.class);

        new HashSet<>(ZTeams.get().getTeams()).forEach(ZTeam::deleteTeam);

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !spectators.isSpectator(player))
                .forEach(player -> ZTeams.get().createTeam(I.t("Alone"), TeamColor.WHITE, player));
    }
}
