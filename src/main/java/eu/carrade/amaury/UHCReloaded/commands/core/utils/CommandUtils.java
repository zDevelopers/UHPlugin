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
package eu.carrade.amaury.UHCReloaded.commands.core.utils;

import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommandUtils
{

    /**
     * Returns {@code true} if the sender is allowed to execute the given command.
     *
     * <p>
     *     Use that only if you have an isolated UHCommand object. Not if you have a direct access to
     *     the parent command, or if you know the command is a root command.
     * </p>
     *
     * @param sender The sender.
     * @param command The command.
     *
     * @return {@code true} if the sender is allowed to execute the command.
     */
    public static boolean isAllowed(CommandSender sender, AbstractCommand command)
    {
        if (command.getParent() == null)
        { // root command
            Command commandAnnotation = command.getClass().getAnnotation(Command.class);
            if (commandAnnotation != null)
            {
                if (commandAnnotation.permission() == null)
                {
                    return true;
                }
                else if (commandAnnotation.permission().isEmpty())
                {
                    return sender.hasPermission(commandAnnotation.name());
                }
                else
                {
                    return sender.hasPermission(commandAnnotation.permission());
                }
            }
        }
        else
        {
            return sender.hasPermission(command.getParent().getSubcommandsPermissions().get(command.getClass().getAnnotation(Command.class).name()));
        }

        return false; // should never happens.
    }

    /**
     * Returns the args without the first item.
     *
     * @param args The arguments sent to the parent command.
     * @return The arguments to send to the child command.
     */
    public static String[] getSubcommandArguments(String[] args)
    {
        if (args.length <= 1)
        {
            return new String[0];
        }

        return Arrays.copyOfRange(args, 1, args.length);
    }


    /**
     * Returns the tags in the arguments, following the format "tagname:value".
     *
     * <p>
     *     If a tag is defined multiple times, the value used is the last one.
     * </p>
     * <p>
     *     Invalid tags (other format that « key:value ») are ignored.
     * </p>
     *
     * @param args The args.
     * @param defaults The defaults values. The values defined here will always be in the returned map,
     *                 with the same value if the key is not in the arguments.
     *                 {@code null} if no default values are needed.
     *
     * @return A map tagname -> value.
     */
    public static Map<String, String> getTagsInArgs(String[] args, Map<String, String> defaults)
    {
        Map<String, String> tagsCollected;

        if (defaults != null)
        {
            tagsCollected = new HashMap<>(defaults);
        }
        else
        {
            tagsCollected = new HashMap<>();
        }

        for (String arg : args)
        {
            String[] argSpilt = arg.split(":");
            if (argSpilt.length >= 2)  // valid
            {
                String key = argSpilt[0];
                String value = StringUtils.join(Arrays.copyOfRange(argSpilt, 1, argSpilt.length), ":");

                tagsCollected.put(key, value);
            }
        }

        return tagsCollected;
    }


    /**
     * Returns a list of autocompletion suggestions based on what the user typed and on a list of
     * available commands.
     *
     * @param typed What the user typed. This string needs to include <em>all</em> the words typed.
     * @param suggestionsList The list of the suggestions.
     * @param numberOfWordsToIgnore If non-zero, this number of words will be ignored at the beginning of the string. This is used to handle multiple-words autocompletion.
     *
     * @return The list of matching suggestions.
     */
    public static List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList, int numberOfWordsToIgnore)
    {
        List<String> list = new ArrayList<String>();

        // For each suggestion:
        //  - if there isn't any world to ignore, we just compare them;
        //  - else, we removes the correct number of words at the beginning of the string;
        //    then, if the raw suggestion matches the typed text, we adds to the suggestion list
        //    the filtered suggestion, because the Bukkit's autocompleter works on a “per-word” basis.

        for (String rawSuggestion : suggestionsList)
        {
            String suggestion;

            if (numberOfWordsToIgnore == 0)
            {
                suggestion = rawSuggestion;
            }
            else
            {
                // Not the primary use, but, hey! It works.
                suggestion = UHUtils.getStringFromCommandArguments(rawSuggestion.split(" "), numberOfWordsToIgnore);
            }

            if (rawSuggestion.toLowerCase().startsWith(typed.toLowerCase()))
            {
                list.add(suggestion);
            }
        }

        Collections.sort(list, Collator.getInstance());

        return list;
    }

    /**
     * Returns a list of autocompletion suggestions based on what the user typed and on a list of
     * available commands.
     *
     * @param typed What the user typed.
     * @param suggestionsList The list of the suggestions.
     *
     * @return The list of matching suggestions.
     */
    public static List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList)
    {
        return getAutocompleteSuggestions(typed, suggestionsList, 0);
    }


    /**
     * Displays a separator around the output of the commands.
     *
     * <p>
     *    To be called before and after the output (prints a line only).
     * </p>
     *
     * @param sender The line will be displayed for this sender.
     */
    public static void displaySeparator(CommandSender sender)
    {
        if (!(sender instanceof Player))
        {
            return;
        }

        sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");
    }
}
