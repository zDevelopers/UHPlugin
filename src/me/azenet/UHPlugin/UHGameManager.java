package me.azenet.UHPlugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import me.azenet.UHPlugin.task.TeamStartTask;
import me.azenet.UHPlugin.task.UpdateTimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UHGameManager {
	
	private UHPlugin p = null;
	private UHTeamManager tm = null;
	private Scoreboard sb = null;

	private Random random = null;
	
	private String sbobjname = "KTP";
	private NumberFormat formatter = new DecimalFormat("00");
	private Boolean damageIsOn = false;

	private LinkedList<Location> loc = new LinkedList<Location>();
	private HashSet<String> alivePlayers = new HashSet<String>();
	
	private Integer alivePlayersCount = 0;
	private Integer aliveTeamsCount = 0;
	
	private Boolean gameWithTeams = true;
	
	private Boolean gameRunning = false;
	private Integer episode = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	
	
	public UHGameManager(UHPlugin plugin) {
		this.p = plugin;
		this.tm = plugin.getTeamManager();
		
		random = new Random();
	}

	
	
	
	
	
	
	public void initEnvironment() {
		p.getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
		p.getServer().getWorlds().get(0).setTime(6000L);
		p.getServer().getWorlds().get(0).setStorm(false);
		p.getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);
	}
	
	public void initScoreboard() {
		sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = sb.registerNewObjective("Vie", Criterias.HEALTH);
		obj.setDisplayName("Vie");
		obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}
	
	public void setMatchInfo() {
		Objective obj = null;
		try {
			obj = sb.getObjective(sbobjname);
			obj.setDisplaySlot(null);
			obj.unregister();
		}
		catch (Exception e) { }
		
		Random r = new Random();
		sbobjname = "KTP"+r.nextInt(10000000);
		obj = sb.registerNewObjective(sbobjname, "dummy");
		obj = sb.getObjective(sbobjname);

		obj.setDisplayName(this.getScoreboardName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		obj.getScore(ChatColor.GRAY + "Épisode " + ChatColor.WHITE + episode).setScore(5);
		obj.getScore(ChatColor.WHITE+""+alivePlayersCount+ChatColor.GRAY+" joueurs").setScore(4);
		
		if(this.gameWithTeams) {
			obj.getScore(ChatColor.WHITE+""+aliveTeamsCount+ChatColor.GRAY+" équipes").setScore(3);
		}
		
		obj.getScore("").setScore(2);
		obj.getScore(ChatColor.WHITE+formatter.format(this.minutesLeft)+ChatColor.GRAY+":"+ChatColor.WHITE+formatter.format(this.secondsLeft)).setScore(1);
	}
	
	public Scoreboard getScoreboard() {
		return sb;
	}
	
	public String getScoreboardName() {
		String s = p.getConfig().getString("scoreboard", "Kill the Patrick");
		return s.substring(0, Math.min(s.length(), 16));
	}
	
	public void setScoreboardForPlayer(Player p) {
		p.setScoreboard(sb);
	}
	
	
	
	
	
	
	
	/**
	 * Starts the game.
	 *  - Teleports the teams
	 *  - Changes the gamemode, reset the life, clear inventories, etc.
	 *  - Launches the timer
	 */
	public void start(CommandSender sender) {
		// We adds all the connected players to a list of alive players.
		alivePlayers.clear();
		for(final Player player : p.getServer().getOnlinePlayers()) {
			alivePlayers.add(player.getName());
		}
		this.alivePlayersCount = alivePlayers.size();
		
		// No team? We creates a team per player.
		if(tm.getTeams().size() == 0) {
			this.gameWithTeams = false;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				UHTeam team = new UHTeam(player.getName(), player.getName(), ChatColor.WHITE, this.p);
				team.addPlayer(player);
				tm.addTeam(team);
			}
		}
		// With teams? We adds players without teams to a solo team.
		else {
			this.gameWithTeams = true;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				if(tm.getTeamForPlayer(player) == null) {
					UHTeam team = new UHTeam(player.getName(), player.getName(), ChatColor.WHITE, this.p);
					team.addPlayer(player);
					tm.addTeam(team);
				}
			}
		}
		
		this.aliveTeamsCount = tm.getTeams().size();
		
		p.getLogger().info("[start] " + aliveTeamsCount + " teams");
		p.getLogger().info("[start] " + alivePlayersCount + " players");
		
		if(loc.size() < tm.getTeams().size()) {
			sender.sendMessage(ChatColor.RED + "Unable to start the game: not enough teleportation spots.");
			return;
		}
		
		
		LinkedList<Location> unusedTP = loc;
		for (final UHTeam t : tm.getTeams()) {
			final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
			
			p.getLogger().info("[start] Launching TP for team " + t.getName());
			
			BukkitRunnable teamStartTask = new TeamStartTask(p, t, lo);
			teamStartTask.runTaskLater(p, 10L);
			
			unusedTP.remove(lo);
		}
		
		World w = p.getServer().getWorlds().get(0);
		
		w.setGameRuleValue("doDaylightCycle", ((Boolean) p.getConfig().getBoolean("daylightCycle.do")).toString());
		w.setGameRuleValue("keepInventory", ((Boolean) false).toString()); // Just in case...
		
		w.setTime(p.getConfig().getLong("daylightCycle.time"));
		w.setStorm(false);
		w.setDifficulty(Difficulty.HARD);
		
		this.episode = 1;
		this.minutesLeft = getEpisodeLength();
		this.secondsLeft = 0;
		
		BukkitRunnable updateTimer = new UpdateTimerTask(p);
		updateTimer.runTaskTimer(p, 20L, 20L);
		
		p.getLogger().info("[start] UpdateTimer task launched");
		
		
		// 30 seconds later, damages are enabled.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				damageIsOn = true;
				p.getLogger().info("[start] Immunity ended.");
			}
		}, 600L);
		
		Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "--- GO ---");
		this.gameRunning = true;
	}
	

	
	
	
	
	
	public void updateTimer() {
		secondsLeft--;
		if (secondsLeft == -1) {
			minutesLeft--;
			secondsLeft = 59;
		}
		if (minutesLeft == -1) {
			shiftEpisode();
		}
	}
	
	public void updateAliveCounters() {
		this.alivePlayersCount = alivePlayers.size();
		this.aliveTeamsCount = getAliveTeams().size();
		
		this.setMatchInfo();
	}
	
	/**
	 * Shifts an episode.
	 * 
	 * @param shifter The player who shifts the episode, an empty string if the episode is shifted because the timer is up.
	 */
	public void shiftEpisode(String shifter) {
		String message = ChatColor.AQUA + "-------- Fin de l'épisode " + episode;
		if(!shifter.equals("")) {
			message += " [forcé par " + shifter + "]";
		}
		message += " --------";
		p.getServer().broadcastMessage(message);
		
		this.episode++;
		this.minutesLeft = getEpisodeLength();
		this.secondsLeft = 0;
	}
	
	/**
	 * Shift an episode because the timer is up.
	 */
	public void shiftEpisode() {
		shiftEpisode("");
	}

	
	/**
	 * Resurrect a player
	 * 
	 * @param player
	 * @return true if the player was dead, false otherwise.
	 */
	public boolean resurrect(Player player) {
		if(!this.isPlayerDead(player.getName())) {
			return false;
		}
		
		this.alivePlayers.add(player.getName());
		this.updateAliveCounters();
		
		this.p.getServer().broadcastMessage(ChatColor.GOLD + player.getName() + " returned from the dead!");
		
		return true;
	}
	
	
	
	
	/**
	 * Is the game launched?
	 * 
	 * @return True if the game is running.
	 */
	public boolean isGameRunning() {
		return this.gameRunning;
	}
	
	
	public Integer getEpisodeLength() {
		return p.getConfig().getInt("episodeLength");
	}
	
	public void addLocation(int x, int z) {
		loc.add(new Location(p.getServer().getWorlds().get(0), x, p.getServer().getWorlds().get(0).getHighestBlockYAt(x,z)+120, z));
	}
	
	public boolean isTakingDamage() {
		return damageIsOn;
	}

	public boolean isPlayerDead(String name) {
		return !alivePlayers.contains(name);
	}
	
	public void addDead(String name) {
		alivePlayers.remove(name);
	}

	private ArrayList<UHTeam> getAliveTeams() {
		ArrayList<UHTeam> aliveTeams = new ArrayList<UHTeam>();
		for (UHTeam t : tm.getTeams()) {
			for (Player p : t.getPlayers()) {
				if (p.isOnline() && !aliveTeams.contains(t)) aliveTeams.add(t);
			}
		}
		return aliveTeams;
	}
}
