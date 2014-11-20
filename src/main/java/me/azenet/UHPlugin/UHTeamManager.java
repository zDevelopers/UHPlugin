/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class UHTeamManager {
	
	private UHPlugin p = null;
	private I18n i = null;
	private ArrayList<UHTeam> teams = new ArrayList<UHTeam>();
	
	private int maxPlayersPerTeam;
	
	
	public UHTeamManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		
		this.maxPlayersPerTeam = p.getConfig().getInt("teams-options.maxPlayersPerTeam");
	}

	
	/**
	 * Adds a team.
	 * 
	 * @param color The color.
	 * @param name The name of the team.
	 * 
	 * @return The new team.
	 * 
	 * @throws IllegalArgumentException if a team with the same name already exists.
	 */
	public UHTeam addTeam(TeamColor color, String name) {
		if(this.getTeam(name) != null) {
			throw new IllegalArgumentException("There is already a team named " + name + " registered!");
		}
		
		UHTeam team = new UHTeam(name, generateColor(color), p);
		teams.add(team);
		
		return team;
	}
	
	/**
	 * Adds a team. A name is generated based on the color.
	 * 
	 * @param color The color.
	 * 
	 * @return The new team.
	 * 
	 * @throws IllegalArgumentException if a team with the same name already exists.
	 */
	public UHTeam addTeam(TeamColor color) {
		
		color = generateColor(color);
		String teamName = color.toString().toLowerCase();
		
		if(getTeam(teamName) != null) { // Taken!
			Random rand = new Random();
			do {
				teamName = color.toString().toLowerCase() + rand.nextInt(1000);
			} while(getTeam(teamName) != null);
		}
		
		UHTeam team = new UHTeam(teamName, color, p);
		teams.add(team);
		
		return team;
	}
	
	/**
	 * Adds a team from an UHTeam object.
	 * 
	 * @param team The team.
	 * @return The new team.
	 * 
	 * @throws IllegalArgumentException if a team with the same name already exists.
	 */
	public UHTeam addTeam(UHTeam team) {
		if(this.getTeam(team.getName()) != null) {
			throw new IllegalArgumentException("There is already a team named " + team.getName() + " registered!");
		}
		
		teams.add(team);
		return team;
	}

	/**
	 * Deletes a team.
	 * 
	 * @param team The team to delete.
	 * @param dontNotify If true, the player will not be notified about the leave.
	 * @return boolean True if a team was removed.
	 */
	public boolean removeTeam(UHTeam team, boolean dontNotify) {
		if(team != null) {
			if(dontNotify) {
				for(OfflinePlayer player : team.getPlayers()) {
					this.removePlayerFromTeam(player, true);
				}
			}
			
			team.deleteTeam();
		}
		
		return teams.remove(team);
	}
	
	/**
	 * Deletes a team.
	 * 
	 * @param team The team to delete.
	 * @return boolean True if a team was removed.
	 */
	public boolean removeTeam(UHTeam team) {
		return removeTeam(team, false);
	}
	
	/**
	 * Deletes a team.
	 * 
	 * @param name The name of the team to delete.
	 * @return boolean True if a team was removed.
	 */
	public boolean removeTeam(String name) {
		return removeTeam(getTeam(name), false);
	}
	
	/**
	 * Deletes a team.
	 * 
	 * @param name The name of the team to delete.
	 * @param dontNotify If true, the player will not be notified about the leave.
	 * @return boolean True if a team was removed.
	 */
	public boolean removeTeam(String name, boolean dontNotify) {
		return removeTeam(getTeam(name), dontNotify);
	}

	/**
	 * Adds a player to a team.
	 * 
	 * @param teamName The team in which we adds the player.
	 * @param player The player to add.
	 * @throws IllegalArgumentException if the team does not exists.
	 */
	public void addPlayerToTeam(String teamName, Player player) {
		UHTeam team = getTeam(teamName);
		
		if(team == null) {
			throw new IllegalArgumentException("There isn't any team named" + teamName + " registered!");
		}
		
		if(this.maxPlayersPerTeam != 0 && team.getPlayers().size() >= this.maxPlayersPerTeam) {
			throw new RuntimeException("The team " + teamName + " is full");
		}
		
		removePlayerFromTeam(player, true);
		team.addPlayer(player);
		player.sendMessage(i.t("team.addplayer.added", team.getDisplayName()));
	}

	/**
	 * Removes a player from his team.
	 * 
	 * @param player The player to remove.
	 * @param dontNotify If true, the player will not be notified about the leave.
	 */
	public void removePlayerFromTeam(OfflinePlayer player, boolean dontNotify) {
		UHTeam team = getTeamForPlayer(player);
		if(team != null) {
			team.removePlayer(player);
			if(!dontNotify && player.isOnline()) {
				((Player) player).sendMessage(i.t("team.removeplayer.removed", team.getDisplayName()));
			}
		}
	}
	
	/**
	 * Removes a player from his team.
	 * 
	 * @param player The player to remove.
	 */
	public void removePlayerFromTeam(OfflinePlayer player) {
		removePlayerFromTeam(player, false);
	}


	/**
	 * Removes all teams.
	 * 
	 * @param dontNotify If true, the player will not be notified when they leave the destroyed team.
	 */
	public void reset(boolean dontNotify) {
		// 1: scoreboard reset
		for(UHTeam team : teams) {
			this.removeTeam(team, dontNotify);
		}
		// 2: internal list reset
		teams = new ArrayList<UHTeam>();
	}
	
	/**
	 * Removes all teams.
	 */
	public void reset() {
		// 1: scoreboard reset
		for(UHTeam team : teams) {
			this.removeTeam(team, false);
		}
		// 2: internal list reset
		teams = new ArrayList<UHTeam>();
	}
	
	/**
	 * Sets the correct display name of a player, according to his team.
	 * 
	 * @param player
	 */
	public void colorizePlayer(OfflinePlayer offlinePlayer) {
		if(!p.getConfig().getBoolean("colorizeChat")) {
			return;
		}
		
		if(!offlinePlayer.isOnline()) {
			return;
		}
		
		Player player = (Player) offlinePlayer;
		
		UHTeam team = getTeamForPlayer(player);
		
		if(team == null) {
			player.setDisplayName(player.getName());
		}
		else {
			if(team.getColor() != null) {
				player.setDisplayName(team.getColor().toChatColor() + player.getName() + ChatColor.RESET);
			}
			else {
				player.setDisplayName(player.getName());
			}
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
	 * Returns the UHTeam object of the team with the given name.
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
	 * Gets a player's team.
	 * 
	 * @param player The player.
	 * @return The team of this player.
	 */
	public UHTeam getTeamForPlayer(OfflinePlayer player) {
		for(UHTeam t : teams) {
			if (t.containsPlayer(player.getUniqueId())) return t;
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
	 * Generates a color from the given color.
	 * <p>
	 * If the color is neither {@link TeamColor#RANDOM} nor {@code null}, returns the given color.<br />
	 * Else, generates a random unused (if possible) color.
	 * @param color
	 * @return
	 */
	public TeamColor generateColor(TeamColor color) {
		if(color != null && color != TeamColor.RANDOM) {
			return color;
		}
		
		// A list of the currently used colors.
		HashSet<TeamColor> availableColors = new HashSet<TeamColor>(Arrays.asList(TeamColor.values()));
		availableColors.remove(TeamColor.RANDOM);
		for(UHTeam team : getTeams()) {
			availableColors.remove(team.getColor());
		}
		
		if(availableColors.size() != 0) {
			return (TeamColor) availableColors.toArray()[(new Random()).nextInt(availableColors.size())];
		}
		else {
			// length-1 so the RANDOM option is never selected.
			return TeamColor.values()[(new Random()).nextInt(TeamColor.values().length - 1)];
		}
	}
	
	/**
	 * Imports the teams from the configuration.
	 * 
	 * @return The number of teams imported.
	 */
	public int importTeamsFromConfig() {
		if(p.getConfig().getList("teams") != null) {
			int teamsCount = 0;
			for(Object teamRaw : p.getConfig().getList("teams")) {
				if(teamRaw instanceof String && teamRaw != null) {
					String[] teamRawSeparated = ((String) teamRaw).split(",");
					TeamColor color = TeamColor.fromString(teamRawSeparated[0]);
					if(color == null) {
						p.getLogger().warning(i.t("load.invalidTeam", (String) teamRaw));
					}
					else {
						if(teamRawSeparated.length == 2) { // "color,name"
							UHTeam newTeam = addTeam(color, teamRawSeparated[1]);
							p.getLogger().info(i.t("load.namedTeamAdded", newTeam.getName(), newTeam.getColor().toString()));
							teamsCount++;
						}
						else if(teamRawSeparated.length == 1) { // "color"
							UHTeam newTeam = addTeam(color, teamRawSeparated[0]);
							p.getLogger().info(i.t("load.teamAdded", newTeam.getColor().toString()));
							teamsCount++;
						}
						else {
							p.getLogger().warning(i.t("load.invalidTeam", (String) teamRaw));
						}
					}
				}
			}
			
			return teamsCount;
		}
		
		return 0;
	}
}
