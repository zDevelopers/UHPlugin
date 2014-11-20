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
		return valueOf(name.trim().toUpperCase());
	}
}
