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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class UHTeamManager {
	
	private UHPlugin p = null;
	private I18n i = null;
	private HashSet<UHTeam> teams = new HashSet<UHTeam>();
	
	private int maxPlayersPerTeam;
	
	
	public UHTeamManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		
		this.maxPlayersPerTeam = p.getConfig().getInt("teams-options.maxPlayersPerTeam");
	}
	
	/**
	 * Is the given team registered?
	 * 
	 * @param team The team.
	 * @return {@code true} if the team is registered.
	 */
	public boolean isTeamRegistered(UHTeam team) {
		return teams.contains(team);
	}
	
	/**
	 * Is the given team registered?
	 * 
	 * @param name The name of the team.
	 * @return {@code true} if the team is registered.
	 */
	public boolean isTeamRegistered(String name) {
		return getTeam(name) != null;
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
		if(isTeamRegistered(name)) {
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
		
		if(isTeamRegistered(teamName)) { // Taken!
			Random rand = new Random();
			do {
				teamName = color.toString().toLowerCase() + rand.nextInt(1000);
			} while(isTeamRegistered(teamName));
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
		if(isTeamRegistered(team)) {
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
		
		team.addPlayer(player);
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
			team.removePlayer(player, dontNotify);
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
		for(UHTeam team : new HashSet<UHTeam>(teams)) {
			this.removeTeam(team, dontNotify);
		}
		
		// 2: internal list reset
		teams.clear();
	}
	
	/**
	 * Removes all teams.
	 */
	public void reset() {
		reset(false);
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
	public Set<UHTeam> getTeams() {
		return teams;
	}
	
	/**
	 * Returns the maximal number of players in each team.
	 * 
	 * @return The max.
	 */
	public int getMaxPlayersPerTeam() {
		return maxPlayersPerTeam;
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
	
	/**
	 * Displays a chat-based GUI (using tellraw formatting) to player to select a team.
	 * <p>
	 * Nothing is displayed if the player cannot use the /join command.
	 * 
	 * @param player The receiver of the chat-GUI.
	 */
	public void displayTeamChooserChatGUI(Player player) {
		if(!player.hasPermission("uh.player.join.self")) return;
		if(!p.getProtocolLibIntegrationWrapper().isProtocolLibIntegrationEnabled()) {
			p.getLogger().log(Level.SEVERE, "Cannot display team-chooser GUI without ProtocolLib");
			return;
		}
		if(p.getGameManager().isGameRunning()) {
			if(!p.getGameManager().isGameWithTeams()) {
				return;
			}
		}
		
		player.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");
		
		if(p.getTeamManager().getTeams().size() != 0) {
			player.sendMessage(i.t("team.gui.choose"));
			
			boolean displayPlayers = p.getConfig().getBoolean("teams-options.gui.displayPlayersInTeams");
			
			for(UHTeam team : p.getTeamManager().getTeams()) {
				
				String text = "{\"text\":\"\",\"extra\":[";
				
				// Team count (something like "[2/5]”)
				text += "{";
				if(maxPlayersPerTeam != 0) {
					text += "\"text\": \"" + i.t("team.gui.playersCount", String.valueOf(team.getSize()), String.valueOf(maxPlayersPerTeam)) + "\", ";
				}
				else {
					text += "\"text\": \"" + i.t("team.gui.playersCountUnlimited", String.valueOf(team.getSize())) + "\", ";
				}
				
				String players = "";
				if(displayPlayers) {
					String bullet = "\n - ";
					for(OfflinePlayer opl : team.getPlayers()) {
						if(!p.getGameManager().isGameRunning()) {
							players += bullet + i.t("team.list.itemPlayer", opl.getName());
						}
						else {
							if(p.getGameManager().isPlayerDead(opl.getUniqueId())) {
								players += bullet + i.t("team.list.itemPlayerDead", opl.getName());
							}
							else {
								players += bullet + i.t("team.list.itemPlayerAlive", opl.getName());
							}
						}
					}
				}
				text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("team.gui.tooltipCount", String.valueOf(team.getPlayers().size())) + players + "\"}";
				text += "},";
				
				text += "{\"text\":\" \"},{";
				
				// Team name (click event is here)
				text += "\"text\":\"" + team.getName() + "\",";
				text += "\"color\":\"" + team.getColor().toString().toLowerCase() + "\",";
				text += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/join " + team.getName() + "\"},";
				if(team.containsPlayer(player)) {
					text += "\"bold\":\"true\",";
					text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("team.gui.tooltipJoinInside", team.getDisplayName()) + "\"}";
				}
				else {
					text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("team.gui.tooltipJoin", team.getDisplayName()) + "\"}";
				}
				text += "}";
				
				text += "]}";
				
				UHUtils.sendJSONMessage(player, text);
			}
			
			if(p.getTeamManager().getTeamForPlayer(player) != null && player.hasPermission("uh.player.leave.self")) {
				String text = "{";
					text += "\"text\":\"" + i.t("team.gui.leaveTeam") + "\",";
					text += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leave\"}";
				text += "}";
				
				UHUtils.sendJSONMessage(player, text);
			}
			else {
				player.sendMessage(i.t("team.gui.howToDisplayAgain"));
			}
		}
		else {
			// No teams.
			player.sendMessage(i.t("team.gui.noTeams"));
		}
		
		player.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");
	}
}
