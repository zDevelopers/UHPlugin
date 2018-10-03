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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.team;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.misc.OfflinePlayersLoader;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class is used for both /uh team join and /join commands.
 *
 * @see {@link eu.carrade.amaury.UHCReloaded.commands.commands.JoinCommand}.
 */
@Command (name = "join")
public class UHTeamJoinCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHTeamJoinCommand(UHCReloaded plugin)
    {
        p = plugin;
    }


    /**
     * Runs the command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments passed to the command.
     *
     * @throws eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
     */
    @Override
    public void run(final CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (args.length == 0)
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);

        UHTeam team;

        String targetName = "";
        Boolean self = null;

        // /... join <team>?
        team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 0));
        if (team != null)
        {
            if (sender instanceof Player)
            {
                targetName = sender.getName();
                self = true;
            }
            else
            {
                throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.ONLY_AS_A_PLAYER);
            }
        }

        // /... join <player> <team>?
        else if (args.length >= 2)
        {
            team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 1));
            if (team != null)
            {
                targetName = args[0];
                self = false;
            }
        }

        if (team == null)
        {
            sender.sendMessage(I.t("{ce}This team does not exists."));
        }
        else if (sender.hasPermission("uh.team.join")
                || (self && sender.hasPermission("uh.player.join.self"))
                || (!self && sender.hasPermission("uh.player.join.others")))
        {
            final UHTeam finalTeam = team;
            OfflinePlayersLoader.loadPlayer(targetName, player -> {
                if (player == null)
                {
                    sender.sendMessage(I.t("{ce}Unable to retrieve the player {0}."));

                    if (!Bukkit.getOnlineMode())
                        sender.sendMessage(I.t("{ce}In offline mode, you cannot add players if they never came to this server."));

                    return;
                }

                finalTeam.addPlayer(player);

                if (!sender.equals(player))
                {
                    sender.sendMessage(I.t("{cs}The player {0} was successfully added to the team {1}", player.getName(), finalTeam.getName()));
                }
            });
        }
        else
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED, this);
        }
    }

    /**
     * Tab-completes this command.
     *
     * @param sender The sender.
     * @param args   The arguments passed to the command.
     *
     * @return A list of suggestions.
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {

        if (args.length >= 2)
        {
            ArrayList<String> teamNames = new ArrayList<>();

            for (UHTeam team : this.p.getTeamManager().getTeams())
            {
                teamNames.add(team.getName());
            }

            return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 1), teamNames, args.length - 2);
        }

        else return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh team join <player> <teamName ...> {ci}: adds a player inside the given team. The name of the team is it color, or the explicit name given."));
    }
}
