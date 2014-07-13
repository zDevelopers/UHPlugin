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
		t.setCanSeeFriendlyInvisibles(true);
		t.setAllowFriendlyFire(true);
		t.setPrefix(this.color + "");
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void addPlayer(Player player) {
		players.add(player);
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).addPlayer(player);
	}
	
	public void removePlayer(Player player) {
		players.remove(player);
		plugin.getGameManager().getScoreboardManager().getScoreboard().getTeam(this.name).removePlayer(player);
	}

	public void teleportTo(Location lo) {
		for (Player p : players) {
			p.teleport(lo);
		}
	}

	public ChatColor getChatColor() {
		return color;
	}
}
