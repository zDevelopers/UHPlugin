package me.azenet.UHPlugin;

import java.util.ArrayList;

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
	
	private ArrayList<Player> players = new ArrayList<Player>();
	
	
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
		updatePlayerObjects();
		return players;
	}

	public void addPlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		players.add(player);
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).addPlayer(player);
		
		plugin.getTeamManager().colorizePlayer(player);
	}
	
	public void removePlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		updatePlayerObjects();
		players.remove(player);
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).removePlayer(player);
		
		plugin.getTeamManager().colorizePlayer(player);
	}
	
	public void deleteTeam() {
		updatePlayerObjects();
		
		// We removes the players from the team (scoreboard team too)
		for(Player player : players) {
			removePlayer(player);
		}
		
		// Then the scoreboard team is deleted.
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).unregister();
		
	}
	
	public boolean containsPlayer(Player player) {
		Validate.notNull(player, "The player cannot be null.");
		
		for(Player playerInTeam : players) {
			if(playerInTeam.getName().equalsIgnoreCase(player.getName())) {
				return true;
			}
		}
		return false;
	}

	public void teleportTo(Location lo) {
		Validate.notNull(lo, "The location cannot be null.");
		
		updatePlayerObjects();
		for (Player p : players) {
			p.teleport(lo);
		}
	}

	public ChatColor getChatColor() {
		return color;
	}
	
	/**
	 * Used to handle the deconnection of a player.
	 * This will reload the Player objects, this is absolutely vital to teleport players
	 * deco/reconnected.
	 */
	private void updatePlayerObjects() {
		ArrayList<Player> playersCopy = (ArrayList<Player>) players.clone();
		players = new ArrayList<Player>();
		
		for(Player player : playersCopy) {
			Player playerFromServer = plugin.getServer().getPlayer(player.getName());
			if(playerFromServer != null) {
				players.add(playerFromServer);
			}
			else {
				players.add(player);
			}
		}
	}
}
