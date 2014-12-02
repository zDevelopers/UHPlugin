/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package me.azenet.UHPlugin;

import org.bukkit.ChatColor;

/**
 * Represents a team color.
 * 
 * Also used to convert a string to a ChatColor object.
 */
public enum TeamColor {
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
	RANDOM("?", null);

	private String name;
	private ChatColor color;

	TeamColor(String name, ChatColor color) {
		this.name = name;
		this.color = color;
	}
	
	public ChatColor toChatColor() {
		return this.color;
	}
	
	/**
	 * Returns a ChatColor object from a string.
	 * 
	 * @param name The name of the color.
	 * @return The ChatColor object (null if RANDOM or not found).
	 */
	public static ChatColor getChatColorByName(String name) {
		for(TeamColor color : values()) {
			if (color.name.equalsIgnoreCase(name)) return color.color;
		}
		
		return null;
	}
	
	/**
	 * Case&trim-insensitive version of {@link #valueOf(String)}.
	 * 
	 * @param name The name to get.
	 * @return A TeamColor value, or null if no value found.
	 */
	public static TeamColor fromString(String name) {
		if(name.equals("?")) {
			return RANDOM;
		}
		
		try {
			return valueOf(name.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			// Maybe a color without underscore
			for(TeamColor color : values()) {
				if(color.name.equalsIgnoreCase(name)) return color;
			}
			
			return null;
		}
	}
}
