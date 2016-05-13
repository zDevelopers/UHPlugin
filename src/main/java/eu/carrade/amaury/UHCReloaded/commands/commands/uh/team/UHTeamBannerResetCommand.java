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
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.inventory.ItemStack;


@Command (name = "bannerreset")
public class UHTeamBannerResetCommand extends AbstractCommand
{
    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        UHTeam team = null;

        if (args.length >= 1)
        {
            String name = UHUtils.getStringFromCommandArguments(args, 0);
            team = UHCReloaded.get().getTeamManager().getTeam(name);
        }
        else if (sender instanceof Player)
        {
            team = UHCReloaded.get().getTeamManager().getTeamForPlayer((Player) sender);
        }
        else
        {
            /// Error message of /uh team bannerreset from the console without name
            sender.sendMessage(I.t("{ce}From the console, you must provide a team name."));
        }


        if (team == null)
        {
            sender.sendMessage(I.t("{ce}Either this team does not exists, or you are not in a team."));
        }
        else
        {
            team.setBanner((ItemStack) null);
            sender.sendMessage(I.t("{cs}The banner of the team {0}{cs} was successfully reset to the default one.", team.getDisplayName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        ArrayList<String> teamNames = new ArrayList<>();

        for (UHTeam team : UHCReloaded.get().getTeamManager().getTeams())
        {
            teamNames.add(team.getName());
        }

        return CommandUtils.getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 0), teamNames, args.length - 1);
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh team bannerreset [team name ...] {ci}: resets the banner of the team to the default. If the team name is not provided, uses the sender's team."));
    }
}
