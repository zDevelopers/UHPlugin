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
package eu.carrade.amaury.UHCReloaded.old.teams;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a team color.
 *
 * Also used to convert a string to a ChatColor object.
 */
public enum TeamColor
{
    AQUA("Aqua", ChatColor.AQUA),
    BLACK("Black", ChatColor.BLACK),
    BLUE("Blue", ChatColor.BLUE),
    DARK_AQUA("Darkaqua", ChatColor.DARK_AQUA),
    DARK_BLUE("Darkblue", ChatColor.DARK_BLUE),
    DARK_GRAY("Darkgray", ChatColor.DARK_GRAY),
    DARK_GREEN("Darkgreen", ChatColor.DARK_GREEN),
    DARK_PURPLE("Darkpurple", ChatColor.DARK_PURPLE),
    DARK_RED("Darkred", ChatColor.DARK_RED),
    GOLD("Gold", ChatColor.GOLD),
    GRAY("Gray", ChatColor.GRAY),
    GREEN("Green", ChatColor.GREEN),
    LIGHT_PURPLE("Lightpurple", ChatColor.LIGHT_PURPLE),
    RED("Red", ChatColor.RED),
    WHITE("White", ChatColor.WHITE),
    YELLOW("Yellow", ChatColor.YELLOW),

    RANDOM("?", null); // Must be the last one.


    private static Map<ChatColor, TeamColor> BY_CHAT_COLOR = new HashMap<>();

    static
    {
        Arrays.stream(values())
                .filter(color -> color.toChatColor() != null)
                .forEach(color -> BY_CHAT_COLOR.put(color.toChatColor(), color));
    }


    private String name;
    private ChatColor color;

    TeamColor(String name, ChatColor color)
    {
        this.name = name;
        this.color = color;
    }

    public ChatColor toChatColor()
    {
        return this.color;
    }

    /**
     * Returns a ChatColor object from a string.
     *
     * @param name The name of the color.
     * @return The ChatColor object (null if RANDOM or not found).
     */
    public static ChatColor getChatColorByName(String name)
    {
        return Arrays.stream(values())
                .filter(color -> color.name.equalsIgnoreCase(name))
                .findFirst()
                .map(color -> color.color)
                .orElse(null);
    }

    /**
     * Case&trim-insensitive version of {@link #valueOf(String)}.
     *
     * @param name The name to get.
     * @return A TeamColor value, or null if no value found.
     */
    public static TeamColor fromString(String name)
    {
        if (name.equals("?"))
        {
            return RANDOM;
        }

        try
        {
            return valueOf(name.trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            // Maybe a color without underscore
            return Arrays.stream(values())
                    .filter(color -> color.name.equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        }
    }

    /**
     * Returns the TeamColor enum member associated to the given ChatColor.
     *
     * @param color The ChatColor.
     * @return The corresponding TeamColor.
     */
    public static TeamColor fromChatColor(ChatColor color)
    {
        return BY_CHAT_COLOR.get(color);
    }
}
