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
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class UHTeam {
	private UHPlugin plugin = null;
	
	private String name = null;
	private String displayName = null;
	private ChatColor color = null;
	
	private ArrayList<UUID> players = new ArrayList<UUID>();
	
	
	public UHTeam(String name, ChatColor color, UHPlugin plugin) {
		Validate.notNull(name, "The name cannot be null.");
		Validate.notNull(plugin, "The plugin cannot be null.");
		
		this.plugin = plugin;
		
		this.name = name;
		this.color = color;
		
		if(this.color != null) {
			this.displayName = color + name + ChatColor.RESET;
		}
		else {
			this.displayName = name;
		}
		
		Scoreboard sb = this.plugin.getGameManager().getScoreboardManager().getScoreboard();
		
		sb.registerNewTeam(this.name);
		Team t = sb.getTeam(this.name);
		
		t.setDisplayName(this.displayName);
		
		if(this.color != null) {
			t.setPrefix(this.color.toString());
		}
		
		t.setCanSeeFriendlyInvisibles(plugin.getConfig().getBoolean("teams-options.canSeeFriendlyInvisibles", true));
		t.setAllowFriendlyFire(plugin.getConfig().getBoolean("teams-options.allowFriendlyFire", true));
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public ArrayList<Player> getPlayers() {
		ArrayList<Player> playersList = new ArrayList<Player>();
		
		for(UUID id : players) {
			playersList.add(plugin.getServer().getPlayer(id));
		}
		
		return playersList;
	}

	public void addPlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		players.add(player.getUniqueId());
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).addPlayer(player);
		
		plugin.getTeamManager().colorizePlayer(player);
	}
	
	public void removePlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		players.remove(player.getUniqueId());
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).removePlayer(player);
		
		plugin.getTeamManager().colorizePlayer(player);
	}
	
	public void deleteTeam() {
		// We removes the players from the team (scoreboard team too)
		for(UUID id : players) {
			removePlayer(plugin.getServer().getPlayer(id));
		}
		
		// Then the scoreboard team is deleted.
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).unregister();
		
	}
	
	public boolean containsPlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		for(UUID playerInTeamID : players) {
			if(playerInTeamID.equals(player.getUniqueId())) {
				return true;
			}
		}
		return false;
	}

	public void teleportTo(Location lo) {
		Validate.notNull(lo, "The location cannot be null.");
		
		for (UUID id : players) {
			plugin.getServer().getPlayer(id).teleport(lo);
		}
	}

	public ChatColor getChatColor() {
		return color;
	}
}
