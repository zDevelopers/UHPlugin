package me.azenet.UHPlugin;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class UHTeam {
	private String name;
	private String displayName;
	private ChatColor color;
	private UHPlugin plugin;
	private ArrayList<Player> players = new ArrayList<Player>();
	
	public UHTeam(String name, String displayName, ChatColor color, UHPlugin plugin) {
		this.name = name;
		this.displayName = displayName;
		this.color = color;
		this.plugin = plugin;
		
		Scoreboard sb = this.plugin.getGameManager().getScoreboardManager().getScoreboard();
		sb.registerNewTeam(this.name);
	
		Team t = sb.getTeam(this.name);
		t.setDisplayName(this.displayName);
		t.setCanSeeFriendlyInvisibles(plugin.getConfig().getBoolean("teams-options.canSeeFriendlyInvisibles", true));
		t.setAllowFriendlyFire(plugin.getConfig().getBoolean("teams-options.allowFriendlyFire", true));
		t.setPrefix(this.color + "");
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
		players.add(player);
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).addPlayer(player);
		
		plugin.getTeamManager().colorizePlayer(player);
	}
	
	public void removePlayer(Player player) {
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
		for(Player playerInTeam : players) {
			if(playerInTeam.getName().equalsIgnoreCase(player.getName())) {
				return true;
			}
		}
		return false;
	}

	public void teleportTo(Location lo) {
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
