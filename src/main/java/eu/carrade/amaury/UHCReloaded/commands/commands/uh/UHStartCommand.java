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

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This command starts the game.
 *
 * Usage: /uh start [slow [go]]
 */
@Command (name = "start")
public class UHStartCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHStartCommand(UHCReloaded plugin)
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
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {

        if (args.length == 1 && args[0].equalsIgnoreCase("help"))
        {
            throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("slow") && args[1].equalsIgnoreCase("go"))
        { // /uh start slow go
            p.getGameManager().finalizeStartSlow(sender);
        }

        else
        {
            Map<String, String> defaultTags = new HashMap<>();
            defaultTags.put("slow", "false");
            defaultTags.put("ignoreTeams", "false");

            Map<String, String> tags = CommandUtils.getTagsInArgs(args, defaultTags);

            try
            {
                p.getGameManager().start(sender, UHUtils.stringToBoolean(tags.get("slow")), UHUtils.stringToBoolean(tags.get("ignoreTeams")));
            }
            catch (IllegalStateException e)
            {
                sender.sendMessage(I.t("start.already"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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

        if (args.length == 2 && args[0].equalsIgnoreCase("slow"))
        { // /uh start slow <?>
            return CommandUtils.getAutocompleteSuggestions(args[1], Collections.singletonList("go"));
        }

        else
        {
            // Can be improved

            List<String> suggestions = new ArrayList<>();
            suggestions.add("slow:true");
            suggestions.add("ignoreTeams:true");

            if (args.length == 1)
            {
                suggestions.add("slow");
                suggestions.add("help");
            }

            return CommandUtils.getAutocompleteSuggestions(args[args.length - 1], suggestions);
        }
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return Arrays.asList(
                I.t("cmd.startHelpTitle"),
                I.t("cmd.startHelpBasic"),
                I.t("cmd.startHelpTagsTitle"),
                I.t("cmd.startHelpTags"),
                I.t("cmd.startHelpSlow"),
                I.t("cmd.startHelpIgnoreTeams")
        );
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("cmd.helpStart"));
    }

    @Override
    public String getCategory()
    {
        return Category.GAME.getTitle();
    }
}
