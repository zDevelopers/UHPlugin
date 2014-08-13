package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.task.TeamStartTask;
import me.azenet.UHPlugin.task.UpdateTimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UHGameManager {
	
	private UHPlugin p = null;
	private UHTeamManager tm = null;
	private I18n i = null;
	private Random random = null;
	
	private Boolean damageIsOn = false;
	private UHScoreboardManager scoreboardManager = null;

	private LinkedList<Location> loc = new LinkedList<Location>();
	private HashSet<String> players = new HashSet<String>();
	private HashSet<String> alivePlayers = new HashSet<String>();
	private HashSet<String> spectators = new HashSet<String>();
	private Map<String,Location> deathLocations = new HashMap<String,Location>();

	private HashSet<String> deadPlayersToBeResurrected = new HashSet<String>();
	
	private Integer alivePlayersCount = 0;
	private Integer aliveTeamsCount = 0;
	
	private Boolean gameWithTeams = true;
	
	// Used for the slow start.
	private Boolean slowStartInProgress = false;
	private Boolean slowStartTPFinished = false;
	
	private Boolean gameRunning = false;
	private Integer episode = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	
	private Long episodeStartTime = 0L;
	
	private Boolean timerPaused = false;
	private Long timerPauseTime = 0L;
	
	
	public UHGameManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		this.tm = p.getTeamManager();
		
		this.random = new Random();
	}


	public void initEnvironment() {
		p.getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
		p.getServer().getWorlds().get(0).setTime(6000L);
		p.getServer().getWorlds().get(0).setStorm(false);
		p.getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);
	}
	
	public void initScoreboard() {
		// Strange: if the scoreboard manager is instanced in the constructor, when the
		// scoreboard manager try to get the game manager through UHPlugin.getGameManager(),
		// the value returned is "null"...
		// This is why we initializes the scoreboard manager later, in this method.
		this.scoreboardManager = new UHScoreboardManager(p);
	}
	
	public void initPlayer(final Player player) {
		Location l = player.getWorld().getSpawnLocation();
		player.teleport(l.add(0,1,0));
		
		player.setFoodLevel(20);
		player.setSaturation(14f);
		player.setHealth(20d);
		
		p.getGameManager().getScoreboardManager().setScoreboardForPlayer(player);
		
		// Used to update the "health" objective, to avoid a null one.
		// Launched later because else, the health is constantly set to 20,
		// and this prevents the health score to be updated.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				p.getGameManager().getScoreboardManager().updateHealthScore(player);
			}
		}, 20L);
		
		// Disable the spectator mode if the game is not started.
		if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
			p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
		}
		
		// If the user has the permission to build before the game, he will probably needs
		// the creative gamemode.
		if(!player.hasPermission("uh.build")) {
			player.setGameMode(GameMode.ADVENTURE);
		}
		else {
			player.setGameMode(GameMode.CREATIVE);
		}
	}


	/**
	 * Starts the game.
	 *  - Teleports the teams
	 *  - Changes the gamemode, reset the life, clear inventories, etc.
	 *  - Launches the timer
	 *  
	 * @param sender The player who launched the game.
	 * @param slow If true, the slow mode is enabled.
	 * With the slow mode, the players are, at first, teleported team by team with a 3-seconds delay,
	 * and with the fly.
	 * Then, the fly is removed and the game starts.
	 * 
	 * @throws RuntimeException if the game is already started.
	 */
	public void start(CommandSender sender, Boolean slow) {
		
		if(isGameRunning()) {
			throw new RuntimeException("The game is already started!");
		}
		
		/** Initialization of the players and the teams **/
		
		// We adds all the connected players (excepted spectators) to a list of alive players.
		// Also, the spectator mode is enabled/disabled if needed.
		alivePlayers.clear();
		for(final Player player : p.getServer().getOnlinePlayers()) {
			if(!spectators.contains(player.getName())) {
				alivePlayers.add(player.getName());
				
				if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
					p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
				}
			}
			else {
				if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
					p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, true);
				}
			}
		}
		
		this.alivePlayersCount = alivePlayers.size();
		
		// The names of the players is stored for later use
		// Used in the resurrectOffline method, to check if a player was really a player or not.
		this.players = (HashSet<String>) alivePlayers.clone();
		
		
		// No team? We creates a team per player.
		if(tm.getTeams().size() == 0) {
			this.gameWithTeams = false;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				if(!spectators.contains(player.getName())) {
					UHTeam team = new UHTeam(player.getName(), ChatColor.WHITE, this.p);
					team.addPlayer(player);
					tm.addTeam(team);
				}
			}
		}
		// With teams? We adds players without teams to a solo team.
		else {
			this.gameWithTeams = true;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				if(tm.getTeamForPlayer(player) == null && !spectators.contains(player.getName())) {
					UHTeam team = new UHTeam(player.getName(), ChatColor.WHITE, this.p);
					team.addPlayer(player);
					tm.addTeam(team);
				}
			}
		}
		
		
		this.aliveTeamsCount = tm.getTeams().size();
		
		if(loc.size() < tm.getTeams().size()) {
			sender.sendMessage(i.t("start.notEnoughTP"));
			
			// We clears the teams if the game was in solo-mode, to avoid a team-counter to be displayed on the next start
			if(!this.gameWithTeams) {
				tm.reset();
			}
			
			return;
		}
		
		/** Teleportation **/
		
		// Standard mode
		if(slow == false) {
			LinkedList<Location> unusedTP = loc;
			for (final UHTeam t : tm.getTeams()) {
				final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
				
				BukkitRunnable teamStartTask = new TeamStartTask(p, t, lo);
				teamStartTask.runTaskLater(p, 10L);
				
				unusedTP.remove(lo);
			}
			
			
			this.startEnvironment();
			this.startTimer();
			this.scheduleDamages();
			this.finalizeStart();
		}
		
		// Slow mode
		else {
			slowStartInProgress = true;
			
			// The players are frozen during the start.
			p.getFreezer().setGlobalFreezeState(true);
			
			// Used to display the number of teams, players... in the scoreboard instead of 0
			// while the players are teleported.
			scoreboardManager.updateScoreboard();
			
			// A simple information, because this start is slower (yeah, Captain Obvious here)
			
			p.getServer().broadcastMessage(i.t("start.teleportationInProgress"));
			
			
			// TP
			
			LinkedList<Location> unusedTP = loc;
			Integer teamsTeleported = 1;
			Integer delayBetweenTP = p.getConfig().getInt("slow-start.delayBetweenTP");
			
			for (final UHTeam t : tm.getTeams()) {
				final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
				
				BukkitRunnable teamStartTask = new TeamStartTask(p, t, lo, true, sender, teamsTeleported);
				teamStartTask.runTaskLater(p, 20L * teamsTeleported * delayBetweenTP);
				
				teamsTeleported++;

				
				unusedTP.remove(lo);
			}
			
			// The end is handled by this.finalizeStartSlow().
		}
	}
	
	/**
	 * Finalizes the start of the game, with the slow mode.
	 * Removes the fly and ends the start (environment, timer...)
	 * 
	 * @param sender
	 */
	public void finalizeStartSlow(CommandSender sender) {
		
		if(!this.slowStartInProgress) {
			sender.sendMessage(i.t("start.startSlowBeforeStartSlowGo"));
			return;
		}
		
		if(!this.slowStartTPFinished) {
			sender.sendMessage(i.t("start.startSlowWaitBeforeGo"));
			return;
		}
		
		// The freeze is removed.
		p.getFreezer().setGlobalFreezeState(false);
		
		// The fly is removed to everyone
		for(Player player : p.getServer().getOnlinePlayers()) {
			if(alivePlayers.contains(player.getName())) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
		}
		
		// The environment is initialized, the game is started.
		this.startEnvironment();
		this.startTimer();
		this.scheduleDamages();
		this.finalizeStart();
		
		this.slowStartInProgress = false;
	}
	
	/**
	 * Initializes the environment at the beginning of the game.
	 */
	public void startEnvironment() {
		World w = p.getServer().getWorlds().get(0);
		
		w.setGameRuleValue("doDaylightCycle", ((Boolean) p.getConfig().getBoolean("daylightCycle.do")).toString());
		w.setGameRuleValue("keepInventory", ((Boolean) false).toString()); // Just in case...
		
		w.setTime(p.getConfig().getLong("daylightCycle.time"));
		w.setStorm(false);
		w.setDifficulty(Difficulty.HARD);
	}
	
	/**
	 * Launches the timer by launching the task that updates the scoreboard every second.
	 */
	private void startTimer() {
		if(p.getConfig().getBoolean("episodes.enabled")) {
			this.episode = 1;
			this.minutesLeft = getEpisodeLength();
			this.secondsLeft = 0;
			
			this.episodeStartTime = System.currentTimeMillis();
			
			BukkitRunnable updateTimer = new UpdateTimerTask(p);
			updateTimer.runTaskTimer(p, 20L, 20L);
		}
	}
	
	/**
	 * Enables the damages 30 seconds (600 ticks) later.
	 */
	private void scheduleDamages() {
		// 30 seconds later, damages are enabled.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				damageIsOn = true;
			}
		}, 600L);
	}
	
	/**
	 * Broadcast the start message and change the state of the game.
	 */
	private void finalizeStart() {
		Bukkit.getServer().broadcastMessage(i.t("start.go"));
		this.scoreboardManager.updateScoreboard();
		this.gameRunning = true;
	}
	
	
	/**
	 * Notify the game manager the teleportation is finished.
	 * 
	 * @param finished True if the TP is finished.
	 */
	public void setSlowStartTPFinished(Boolean finished) {
		this.slowStartTPFinished = finished;
	}
	
	
	
	
	/**
	 * Updates the timer.
	 */
	public void updateTimer() {
		if(p.getConfig().getBoolean("episodes.enabled") && !this.timerPaused) {
			if(p.getConfig().getBoolean("episodes.syncTimer")) {
				long timeSinceStart = System.currentTimeMillis() - this.episodeStartTime;
				long diffSeconds = timeSinceStart / 1000 % 60;
				long diffMinutes = timeSinceStart / (60 * 1000) % 60;
				
				if(diffMinutes >= this.getEpisodeLength()) {
					shiftEpisode();
				}
				else {
					minutesLeft = (int) (this.getEpisodeLength() - diffMinutes) - 1;
					secondsLeft = (int) (60 - diffSeconds) - 1;
				}
			}
			else {
				secondsLeft--;
				if (secondsLeft == -1) {
					minutesLeft--;
					secondsLeft = 59;
				}
				if (minutesLeft == -1) {
					shiftEpisode();
				}
			}
		}
	}
	
	/**
	 * Pauses (or restarts) the timer.
	 */
	public void toggleTimerPause() {
		// If the game is not started, the timer is not running.
		if(p.getGameManager().isGameRunning()) {
			if(!this.timerPaused) {
				this.timerPaused = true;
				this.timerPauseTime = System.currentTimeMillis();
			}
			else {
				// We have to add to the time of the start of the episode the elapsed time
				// during the pause.
				this.episodeStartTime += (System.currentTimeMillis() - this.timerPauseTime);
				this.timerPauseTime = 0L;

				this.timerPaused = false;
			}
		}
	}
	
	/**
	 * Returns true if the timer is paused.
	 * 
	 * @return true if the timer is paused.
	 */
	public boolean isTimerPaused() {
		return timerPaused;
	}
	
	
	/**
	 * Updates the cached values of the numbers of alive players
	 * and teams.
	 */
	public void updateAliveCounters() {
		this.alivePlayersCount = alivePlayers.size();
		this.aliveTeamsCount = getAliveTeams().size();
		
		this.scoreboardManager.updateScoreboard();
	}
	
	
	/**
	 * Shifts an episode.
	 * 
	 * @param shifter The player who shifts the episode, an empty string if the episode is shifted because the timer is up.
	 */
	public void shiftEpisode(String shifter) {
		if(p.getConfig().getBoolean("episodes.enabled")) {
			String message = null;
			if(!shifter.equals("")) {
				message = i.t("episodes.endForced", String.valueOf(episode), shifter);
			}
			else {
				message = i.t("episodes.end", String.valueOf(episode));
			}
			p.getServer().broadcastMessage(message);
			
			this.episode++;
			this.minutesLeft = getEpisodeLength();
			this.secondsLeft = 0;
			
			this.episodeStartTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * Shift an episode because the timer is up.
	 */
	public void shiftEpisode() {
		shiftEpisode("");
	}
	
	
	/**
	 * Resurrects an offline player.
	 * The tasks that needed to be executed when the player is online are delayed
	 * and executed when the player joins.
	 * 
	 * @param playerName The name of the player to resurrect
	 * @return true if the player was dead, false otherwise.
	 */
	public boolean resurrect(String playerName) {
		if(!this.isPlayerDead(playerName)) {
			return false;
		}
		
		Player playerOnline = Bukkit.getPlayer(playerName);
		if(playerOnline != null) {
			return resurrectOnline(playerOnline);
		}
		else {
			// We checks if the player was a player
			if(!this.players.contains(playerName)) {
				return false;
			}
		}
		
		// So, now, we are sure that the player is really offline.
		this.alivePlayers.add(playerName);
		this.updateAliveCounters();
		
		// The task needed to be executed will be executed when the player join.
		this.deadPlayersToBeResurrected.add(playerName);
		
		return true;
	}
	
	/**
	 * Resurrect a player
	 * 
	 * @param player
	 * @return true if the player was dead, false otherwise.
	 */
	private boolean resurrectOnline(Player player) {
		
		// We registers the user as an alive player.
		this.alivePlayers.add(player.getName());
		this.updateAliveCounters();
		
		// This method can be used to add a player after the game has started.
		if(!players.contains(player.getName())) {
			players.add(player.getName());
		}
		
		this.resurrectPlayerOnlineTask(player);
		
		return true;
	}
	
	/**
	 * The things that have to be done in order to resurrect the players
	 * and that need the player to be online.
	 * 
	 * @param player The player to resurrect
	 */
	public void resurrectPlayerOnlineTask(Player player) {
		// Spectator disabled
		if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
			p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
		}
		
		// Death point hided in the dynmap
		p.getDynmapIntegration().hideDeathLocation(player);
		
		// All players are notified
		this.p.getServer().broadcastMessage(i.t("resurrect.broadcastMessage", player.getName()));
	}
	
	/**
	 * Returns true if a player need to be resurrected.
	 * 
	 * @param player
	 * @return
	 */
	public boolean isDeadPlayersToBeResurrected(Player player) {
		return deadPlayersToBeResurrected.contains(player.getName());
	}
	
	/**
	 * Register a player as resurrected.
	 * 
	 * @param player
	 */
	public void markPlayerAsResurrected(Player player) {
		deadPlayersToBeResurrected.remove(player.getName());
	}
	
	
	/**
	 * This method saves the location of the death of a player.
	 * 
	 * @param player
	 * @param location
	 */
	public void addDeathLocation(Player player, Location location) {
		deathLocations.put(player.getName(), location);
	}
	
	/**
	 * This method removes the stored death location.
	 * @param player
	 */
	public void removeDeathLocation(Player player) {
		deathLocations.remove(player.getName());
	}
	
	/**
	 * This method returns the stored death location.
	 * 
	 * @param player
	 * @return Location
	 */
	public Location getDeathLocation(Player player) {
		if(deathLocations.containsKey(player.getName())) {
			return deathLocations.get(player.getName());
		}
		
		return null;
	}
	
	/**
	 * This method returns true if a death location is stored for the given player.
	 * 
	 * @param player
	 * @return boolean
	 */
	public boolean hasDeathLocation(Player player) {
		return deathLocations.containsKey(player.getName());
	}
	
	/**
	 * Adds a spectator. When the game is started, spectators are ignored 
	 * and the spectator mode is enabled if SpectatorPlus is present.
	 * 
	 * @param player The player to register as a spectator.
	 */
	public void addSpectator(Player player) {
		spectators.add(player.getName());
		tm.removePlayerFromTeam(player);
	}
	
	/**
	 * Removes a spectator.
	 * 
	 * @param player
	 */
	public void removeSpectator(Player player) {
		spectators.remove(player.getName());
	}
	
	/**
	 * Returns a list of the current registered spectators.
	 * 
	 * This returns only a list of the <em>initial</em> spectators.
	 * Use {@link UHGameManager.getAlivePlayers()} to get the alive players, and remove
	 * the elements of this list from the online players to get the spectators.
	 * 
	 * @return The initial spectators.
	 */
	public HashSet<String> getSpectators() {
		return spectators;
	}
	
	
	/**
	 * Adds a spawn point.
	 * 
	 * @param x
	 * @param z
	 */
	public void addLocation(int x, int z) {
		loc.add(new Location(p.getServer().getWorlds().get(0), x, p.getServer().getWorlds().get(0).getHighestBlockYAt(x,z)+120, z));
	}
	
	/**
	 * Returns true if the game was launched.
	 * 
	 * @return The running state.
	 */
	public boolean isGameRunning() {
		return gameRunning;
	}
	
	/**
	 * Returns true if the game is a game with teams, and false if the game is a solo game.
	 * 
	 * @return
	 */
	public boolean isGameWithTeams() {
		return gameWithTeams;
	}
	
	/**
	 * Returns true if damages are enabled.
	 * Damages are enabled 30 seconds after the beginning of the game.
	 * 
	 * @return
	 */
	public boolean isTakingDamage() {
		return damageIsOn;
	}
	
	/**
	 * Returns true if the given player is dead.
	 * 
	 * @param name The name of the player.
	 * @return True if the player is dead.
	 */
	public boolean isPlayerDead(String name) {
		return !alivePlayers.contains(name);
	}
	
	/**
	 * Registers a player as dead.
	 * 
	 * @param name The name of the player to mark as dead.
	 */
	public void addDead(String name) {
		alivePlayers.remove(name);
	}

	/**
	 * Returns a list of the currently alive teams.
	 * 
	 * @return The list.
	 */
	public ArrayList<UHTeam> getAliveTeams() {
		ArrayList<UHTeam> aliveTeams = new ArrayList<UHTeam>();
		for (UHTeam t : tm.getTeams()) {
			for (Player p : t.getPlayers()) {
				if (p.isOnline() && !aliveTeams.contains(t)) aliveTeams.add(t);
			}
		}
		return aliveTeams;
	}
	
	/**
	 * Returns a list of the currently alive players.
	 * 
	 * @return The list.
	 */
	public HashSet<String> getAlivePlayers() {
		return this.alivePlayers;
	}
	
	/**
	 * Returns the scoreboard manager.
	 * 
	 * @return
	 */
	public UHScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}
	
	/**
	 * Returns the length of one episode, in minutes.
	 * 
	 * @return
	 */
	public Integer getEpisodeLength() {
		return p.getConfig().getInt("episodes.length");
	}
	
	/**
	 * Returns the (cached) number of alive players.
	 * 
	 * @return
	 */
	public Integer getAlivePlayersCount() {
		return alivePlayersCount;
	}

	/**
	 * Returns the (cached) number of alive teams.
	 * 
	 * @return
	 */
	public Integer getAliveTeamsCount() {
		return aliveTeamsCount;
	}
	
	/**
	 * Returns the number of the current episode.
	 * 
	 * @return
	 */
	public Integer getEpisode() {
		return episode;
	}

	/**
	 * Returns the number of minutes left in the current episode.
	 * 
	 * @return
	 */
	public Integer getMinutesLeft() {
		return minutesLeft;
	}
	
	/**
	 * Returns the number of seconds left in the current <em>minute</em>.
	 * @return
	 */
	public Integer getSecondsLeft() {
		return secondsLeft;
	}
}
