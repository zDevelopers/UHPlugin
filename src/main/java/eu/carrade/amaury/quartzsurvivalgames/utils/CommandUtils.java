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
package eu.carrade.amaury.quartzsurvivalgames.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;


public class CommandUtils
{
    public final static String CHAT_SEPARATOR = ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅";

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
                suggestion = QSGUtils.getStringFromCommandArguments(rawSuggestion.split(" "), numberOfWordsToIgnore);
            }

            if (rawSuggestion.toLowerCase().startsWith(typed.toLowerCase()))
            {
                list.add(suggestion);
            }
        }

        list.sort(Collator.getInstance());

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

        sender.sendMessage(CHAT_SEPARATOR);
    }
}
