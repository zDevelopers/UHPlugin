package me.azenet.UHPlugin;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UHTeamManager {
	
	private UHPlugin p = null;
	private ArrayList<UHTeam> teams = new ArrayList<UHTeam>();
	
	
	public UHTeamManager(UHPlugin plugin) {
		this.p = plugin;
	}

	
	/**
	 * Adds a team.
	 * 
	 * @param color The color. This must be a valid Minecraft color name.
	 * @param name The name of the team.
	 * @throws IllegalArgumentException if a team with the same name already exists.
	 */
	public void addTeam(ChatColor color, String name) {
		if(this.getTeam(name) != null) {
			throw new IllegalArgumentException("There is already a team named " + name + " registered!");
		}
		
		teams.add(new UHTeam(name, color, p));
	}
	
	/**
	 * Adds a team from an UHTeam object.
	 * 
	 * @param UHTeam team The team.
	 * @throws IllegalArgumentException if a team with the same name already exists.
	 */
	public void addTeam(UHTeam team) {
		if(this.getTeam(team.getName()) != null) {
			throw new IllegalArgumentException("There is already a team named " + team.getName() + " registered!");
		}
		
		teams.add(team);
	}

	/**
	 * Deletes a team.
	 * 
	 * @param name The name of the team to delete.
	 * @return boolean True if a team was removed.
	 */
	public boolean removeTeam(String name) {
		UHTeam team = getTeam(name);
		return teams.remove(team);
	}

	/**
	 * Adds a player to a team.
	 * 
	 * @param teamName The team in which we add the player.
	 * @param player The player to add.
	 * @throws IllegalArgumentException if the team does not exists.
	 */
	public void addPlayerToTeam(String teamName, Player player) {
		removePlayerFromTeam(player);
		
		UHTeam team = getTeam(teamName);
		
		if(team == null) {
			throw new IllegalArgumentException("There isn't any team named" + teamName + " registered!");
		}
		
		team.addPlayer(player);
	}

	/**
	 * Removes a player from its team.
	 * 
	 * @param player The player to remove.
	 */
	public void removePlayerFromTeam(Player player) {
		UHTeam team = getTeamForPlayer(player);
		if(team != null) {
			team.removePlayer(player);
		}
	}


	/**
	 * Removes all teams.
	 */
	public void reset() {
		// 1: scoreboard reset
		for(UHTeam team : teams) {
			team.deleteTeam();
		}
		// 2: internal list reset
		teams = new ArrayList<UHTeam>();
	}
	
	/**
	 * Sets the correct display name of a player, according to his team.
	 * 
	 * @param player
	 */
	public void colorizePlayer(Player player) {
		if(!p.getConfig().getBoolean("colorizeChat")) {
			return;
		}
		
		UHTeam team = getTeamForPlayer(player);
		
		if(team == null) {
			player.setDisplayName(ChatColor.WHITE + player.getName() + ChatColor.RESET);
		}
		else {
			player.setDisplayName(team.getChatColor() + player.getName() + ChatColor.RESET);
		}
	}
	
	/**
	 * Returns all the teams.
	 * 
	 * @return The teams.
	 */
	public ArrayList<UHTeam> getTeams() {
		return this.teams;
	}
	
	/**
	 * Get a team.
	 * 
	 * @param name The name of the team.
	 * @return The team, or null if the team does not exists.
	 */
	public UHTeam getTeam(String name) {
		for(UHTeam t : teams) {
			if (t.getName().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return null;
	}
	
	/**
	 * Get a player's team.
	 * 
	 * @param player The player.
	 * @return The team of this player.
	 */
	public UHTeam getTeamForPlayer(Player p) {
		for(UHTeam t : teams) {
			if (t.containsPlayer(p)) return t;
		}
		return null;
	}
	
	/**
	 * Checks if two players are in the same team.
	 * 
	 * @param player1 The first player.
	 * @param player2 The second player
	 * @return True if the players are in the same team, false else.
	 */
	public boolean inSameTeam(Player pl, Player pl2) {
		return (getTeamForPlayer(pl).equals(getTeamForPlayer(pl2)));
	}
	
	
	
	/**
	 * Used to convert a string to a ChatColor object.
	 */
	private enum StringToChatColor {
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
		YELLOW("Yellow", ChatColor.YELLOW);

		private String name;
		private ChatColor color;

		StringToChatColor(String name, ChatColor color) {
			this.name = name;
			this.color = color;
		}

		public static ChatColor getChatColorByName(String name) {
			for(StringToChatColor stcc : values()) {
				if (stcc.name.equalsIgnoreCase(name)) return stcc.color;
			}
			return null;
		}
	}
	
	/**
	 * Utility: return the ChatColor version of a color, or null if the provided color is invalid.
	 * @param name The name of the color.
	 * @return ChatColor
	 */
	public ChatColor getChatColorByName(String name) {
		return StringToChatColor.getChatColorByName(name);
	}
	
}
