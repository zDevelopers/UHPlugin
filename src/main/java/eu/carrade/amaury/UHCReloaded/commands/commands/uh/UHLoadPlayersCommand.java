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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.misc.OfflinePlayersLoader;
import fr.zcraft.zlib.components.i18n.I;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Command(name = "loadplayers")
public class UHLoadPlayersCommand extends AbstractCommand
{
    @Override
    public void run(final CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        if (!Bukkit.getOnlineMode())
        {
            sender.sendMessage(I.t("{ce}You cannot load unknown players in offline mode, sorry."));
            return;
        }

        if (args.length == 0)
        {
            /// Error returned if one calls /uh loadplayers without arguments.
            sender.sendMessage(I.t("{ce}You need to provide at least one player name."));
            return;
        }

        /// Message displayed when the /uh loadplayers command is used, as the execution may take some time.
        sender.sendMessage(I.t("{cst}Loading players..."));

        OfflinePlayersLoader.loadPlayers(
                Arrays.asList(args),
                addedPlayers -> sender.sendMessage(I.tn("{cs}Loaded {0} player successfully.", "{cs}Loaded {0} players successfully.", addedPlayers.size())),
                notFound -> {
                    /// Message sent if some players cannot be loaded while /uh loadplayers is used. 0 = amount of players missing; 1 = list of nicknames (format "nick1, nick2, nick3").
                    sender.sendMessage(I.tn("{ce}{0} player is missing: {1}.", "{ce}{0} players are missing: {1}.", notFound.size(), notFound.size(), StringUtils.join(notFound, ", ")));
                }
        );
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh loadplayers <pseudo> [pseudo] ... {ci}: loads the given players in the server so they can be added to teams even if they never logged in."));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
