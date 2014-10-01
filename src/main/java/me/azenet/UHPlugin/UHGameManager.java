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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.task.FireworksOnWinnersTask;
import me.azenet.UHPlugin.task.TeamStartTask;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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

	private HashSet<String> players = new HashSet<String>(); // Will be converted to UUID when a built-in API for name->UUID conversion will be available 
	private HashSet<UUID> alivePlayers = new HashSet<UUID>();
	private HashSet<UUID> spectators = new HashSet<UUID>();
	private Map<UUID,Location> deathLocations = new HashMap<UUID,Location>();

	private HashSet<String> deadPlayersToBeResurrected = new HashSet<String>(); // Same
	
	private Integer alivePlayersCount = 0;
	private Integer aliveTeamsCount = 0;
	
	private Boolean gameWithTeams = true;
	
	// Used for the slow start.
	private Boolean slowStartInProgress = false;
	private Boolean slowStartTPFinished = false;
	
	private Boolean gameRunning = false;
	private Integer episode = 0;
	private UHSound deathSound = null;
	
	// Used to send a contextual error message in UHCommandManager, using only one exception,
	// by checking the message. (Used in this.finishGame().)
	public final static String FINISH_ERROR_NOT_STARTED = "Unable to finish the game: the game is not started";
	public final static String FINISH_ERROR_NOT_FINISHED = "Unable to finish the game: the game is not finished";
	
	
	public UHGameManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		this.tm = p.getTeamManager();
		
		this.random = new Random();
		
		
		// Registers the death sound
		this.deathSound = new UHSound(p.getConfig().getConfigurationSection("death.announcements.sound"));
	}

	/**
	 * Initializes the environment before the start of the game.
	 */
	public void initEnvironment() {
		p.getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
		p.getServer().getWorlds().get(0).setTime(6000L);
		p.getServer().getWorlds().get(0).setStorm(false);
		p.getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);
	}

	/**
	 * Initializes the given player.
	 * 
	 *  - Teleportation to the default world's spawn point.
	 *  - Max food level & health.
	 *  - Scoreboard.
	 *  - Fixed health score.
	 *  - Spectate mode disabled.
	 *  - Gamemode: creative (if permission "uh.build" granted) or adventure (else).
	 * 
	 * @param player
	 */
	public void initPlayer(final Player player) {
		Location l = player.getWorld().getSpawnLocation();
		player.teleport(l.add(0,1,0));
		
		player.setFoodLevel(20);
		player.setSaturation(14f);
		player.setHealth(20d);
		
		p.getScoreboardManager().setScoreboardForPlayer(player);
		
		// Used to update the "health" objective, to avoid a null one.
		// Launched later because else, the health is constantly set to 20,
		// and this prevents the health score to be updated.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				p.getScoreboardManager().updateHealthScore(player);
			}
		}, 20L);
		
		// Disable the spectator mode if the game is not started.
		if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
			p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
		}
		
		// If the user has the permission to build before the game, he will probably needs
		// the creative mode.
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
	 * @throws IllegalStateException if the game is already started.
	 */
	public void start(CommandSender sender, Boolean slow) throws IllegalStateException {
		
		if(isGameRunning()) {
			throw new IllegalStateException("The game is already started!");
		}
		
		/** Initialization of the players and the teams **/
		
		// We adds all the connected players (excepted spectators) to a list of alive players.
		// Also, the spectator mode is enabled/disabled if needed.
		alivePlayers.clear();
		for(final Player player : p.getServer().getOnlinePlayers()) {
			if(!spectators.contains(player.getUniqueId())) {
				alivePlayers.add(player.getUniqueId());
				
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
		for(UUID id : alivePlayers) {
			this.players.add(p.getServer().getPlayer(id).getName());
		}
		
		// This is used to be able to delete the teams created on-the-fly
		ArrayList<String> onTheFlyTeams = new ArrayList<String>();
		
		
		// No team? We creates a team per player.
		if(tm.getTeams().isEmpty()) {
			this.gameWithTeams = false;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				if(!spectators.contains(player.getUniqueId())) {
					
					String teamName = player.getName();
					teamName = teamName.substring(0, Math.min(teamName.length(), 16));
					
					UHTeam team = new UHTeam(teamName, null, this.p);
					team.addPlayer(player);
					
					tm.addTeam(team);
				}
			}
		}
		// With teams? We adds players without teams to a solo team.
		else {
			this.gameWithTeams = true;
			
			for(final Player player : p.getServer().getOnlinePlayers()) {
				if(tm.getTeamForPlayer(player) == null && !spectators.contains(player.getUniqueId())) {
					
					// A team with that name may already exists.
					// Tries:
					// 1. The name of the player;
					// 2. TheName (bigRandomNumberHere).
					
					String teamName = player.getName();
					
					if(tm.getTeam(teamName) != null) { // Team registered
						// The probability of a conflict here is so small...
						// I will not take this possibility into account.
						teamName = player.getName() + " " + this.random.nextInt(1000000);
					}
					
					teamName = teamName.substring(0, Math.min(teamName.length(), 16));
					
					UHTeam team = new UHTeam(teamName, null, this.p);
					team.addPlayer(player);
					
					tm.addTeam(team);
					
					onTheFlyTeams.add(teamName);
				}
			}
		}
		
		
		this.aliveTeamsCount = tm.getTeams().size();
		
		if(p.getSpawnsManager().getSpawnPoints().size() < tm.getTeams().size()) {
			sender.sendMessage(i.t("start.notEnoughTP"));
			
			// We clears the teams if the game was in solo-mode, to avoid a team-counter to be displayed on the next start
			if(!this.gameWithTeams) {
				tm.reset(true);
			}
			// We removes the teams automatically added, to avoid a bad team count.
			else {
				for(String teamName : onTheFlyTeams) {
					tm.removeTeam(teamName, true);
				}
			}
			
			return;
		}
		
		/** Teleportation **/
		
		// Standard mode
		if(slow == false) {
			List<Location> unusedTP = p.getSpawnsManager().getSpawnPoints();
			for (final UHTeam t : tm.getTeams()) {
				final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
				
				BukkitRunnable teamStartTask = new TeamStartTask(p, t, lo);
				teamStartTask.runTaskLater(p, 10L);
				
				unusedTP.remove(lo);
			}
			
			
			this.startEnvironment();
			this.startTimer();
			this.scheduleDamages();
			this.sendStartupProTips();
			this.finalizeStart();
		}
		
		// Slow mode
		else {
			slowStartInProgress = true;
			
			// The players are frozen during the start.
			p.getFreezer().setGlobalFreezeState(true, false);
			
			// Used to display the number of teams, players... in the scoreboard instead of 0
			// while the players are teleported.
			p.getScoreboardManager().updateCounters();
			
			// A simple information, because this start is slower (yeah, Captain Obvious here)
			p.getServer().broadcastMessage(i.t("start.teleportationInProgress"));
			
			
			// TP
			
			List<Location> unusedTP = p.getSpawnsManager().getSpawnPoints();
			Integer teamsTeleported = 1;
			Integer delayBetweenTP = p.getConfig().getInt("start.slow.delayBetweenTP");
			
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
		p.getFreezer().setGlobalFreezeState(false, false);
		
		// The fly is removed to everyone
		for(Player player : p.getServer().getOnlinePlayers()) {
			if(alivePlayers.contains(player.getUniqueId())) {
				player.setFlying(false);
				player.setAllowFlight(false);
			}
		}
		
		// The environment is initialized, the game is started.
		this.startEnvironment();
		this.startTimer();
		this.scheduleDamages();
		this.sendStartupProTips();
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
	public void startTimer() {
		if(p.getConfig().getBoolean("episodes.enabled")) {
			this.episode = 1;
			
			// Removes the fake timer displayed before the start of the game.
			p.getScoreboardManager().startTimer();
			
			// An empty string is used for the name of the main timer, because
			// such a name can't be used by players.
			UHTimer mainTimer = new UHTimer("");
			mainTimer.setDuration(this.getEpisodeLength());
			
			p.getTimerManager().registerMainTimer(mainTimer);
			
			mainTimer.start();
		}
	}
	
	/**
	 * Enables the damages 30 seconds (600 ticks) later.
	 */
	public void scheduleDamages() {
		// 30 seconds later, damages are enabled.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				damageIsOn = true;
			}
		}, 600L);
	}
	
	/**
	 * Sends two ProTips:
	 *  - about the team chat, to all players, 20 seconds after the beginning of the game;
	 *  - about the invincibility, 5 seconds after the beginning of the game.
	 */
	public void sendStartupProTips() {
		// Team chat - 20 seconds after
		if(this.isGameWithTeams()) {
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					for(Player player : getOnlineAlivePlayers()) {
						p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_USE_T_COMMAND);
					}
				}
			}, 400L);
		}
		
		// Invincibility - 5 seconds after
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
			@Override
			public void run() {
				for(Player player : getOnlineAlivePlayers()) {
					p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_STARTUP_INVINCIBILITY);
				}
			}
		}, 100L);
	}
	
	/**
	 * Broadcasts the start message and change the state of the game.
	 * Also, forces the global freeze start to false, to avoid toggle bugs (like inverted state).
	 */
	public void finalizeStart() {
		Bukkit.getServer().broadcastMessage(i.t("start.go"));
		
		p.getFreezer().setGlobalFreezeState(false);
		
		// Start sound
		new UHSound(p.getConfig().getConfigurationSection("start.sound")).broadcast();
		
		this.gameRunning = true;
		
		// The updateCounters method needs to be executed when the game is marked
		// as running, in order to display the team count.
		p.getScoreboardManager().updateCounters();
		p.getScoreboardManager().updateTimers();
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
	 * Updates the cached values of the numbers of alive players
	 * and teams.
	 */
	public void updateAliveCounters() {
		this.alivePlayersCount = alivePlayers.size();
		this.aliveTeamsCount = getAliveTeams().size();
		
		p.getScoreboardManager().updateCounters();
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
			
			// Restarts the timer.
			// Useless for a normal start (restarted in the event), but needed if the episode was shifted.
			if(!shifter.equals("")) {
				p.getScoreboardManager().restartTimers();
				p.getTimerManager().getMainTimer().start();
			}
			
			p.getScoreboardManager().updateCounters();
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
		
		Player playerOnline = Bukkit.getPlayer(playerName);
		
		if(playerOnline != null) {
			return resurrectPlayerOnlineTask(playerOnline);
		}
		else {
			// We checks if the player was a player
			if(!this.players.contains(playerName)) {
				return false;
			}
		}
		
		// So, now, we are sure that the player is really offline.		
		// The task needed to be executed will be executed when the player join.
		this.deadPlayersToBeResurrected.add(playerName);
		
		return true;
	}
	
	/**
	 * The things that have to be done in order to resurrect the players
	 * and that needs the player to be online.
	 * 
	 * @param player The player to resurrect
	 * @return true if the player was dead, false otherwise.
	 */
	public boolean resurrectPlayerOnlineTask(Player player) {
		
		if(this.alivePlayers.contains(player.getUniqueId())) {
			return false;
		}
		
		// Player registered as alive
		this.alivePlayers.add(player.getUniqueId());
		this.updateAliveCounters();
		
		// This method can be used to add a player after the game has started.
		if(!players.contains(player.getName())) {
			players.add(player.getName());
		}
		
		// Spectator disabled
		if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
			p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
		}
		
		// Death point removed on the dynmap
		p.getDynmapIntegration().hideDeathLocation(player);
		
		// All players are notified
		this.p.getServer().broadcastMessage(i.t("resurrect.broadcastMessage", player.getName()));
		
		return true;
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
	 * Registers a player as resurrected.
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
		deathLocations.put(player.getUniqueId(), location);
	}
	
	/**
	 * This method removes the stored death location.
	 * @param player
	 */
	public void removeDeathLocation(Player player) {
		deathLocations.remove(player.getUniqueId());
	}
	
	/**
	 * This method returns the stored death location.
	 * 
	 * @param player
	 * @return Location
	 */
	public Location getDeathLocation(Player player) {
		if(deathLocations.containsKey(player.getUniqueId())) {
			return deathLocations.get(player.getUniqueId());
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
		return deathLocations.containsKey(player.getUniqueId());
	}
	
	/**
	 * Adds a spectator. When the game is started, spectators are ignored 
	 * and the spectator mode is enabled if SpectatorPlus is present.
	 * 
	 * @param player The player to register as a spectator.
	 */
	public void addSpectator(Player player) {
		spectators.add(player.getUniqueId());
		tm.removePlayerFromTeam(player);
	}
	
	/**
	 * Removes a spectator.
	 * 
	 * @param player
	 */
	public void removeSpectator(Player player) {
		spectators.remove(player.getUniqueId());
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
		
		HashSet<String> spectatorNames = new HashSet<String>();
		
		for(UUID id : spectators) {
			spectatorNames.add(p.getServer().getPlayer(id).getName());
		}
		
		return spectatorNames;
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
	 * @param player The player.
	 * @return True if the player is dead.
	 */
	public boolean isPlayerDead(Player player) {
		return !alivePlayers.contains(player.getUniqueId());
	}
	
	/**
	 * Returns true if the given player is dead.
	 * 
	 * @param player The UUID of the player.
	 * @return True if the player is dead.
	 */
	public boolean isPlayerDead(UUID player) {
		return !alivePlayers.contains(player);
	}
	
	/**
	 * Registers a player as dead.
	 * 
	 * @param player The player to mark as dead.
	 */
	public void addDead(Player player) {
		alivePlayers.remove(player.getUniqueId());
	}
	
	/**
	 * Registers a player as dead.
	 * 
	 * @param player The UUID of the player to mark as dead.
	 */
	public void addDead(UUID player) {
		alivePlayers.remove(player);
	}

	
	/**
	 * Broadcasts the winner(s) of the game and launches some fireworks
	 * 
	 * @throws IllegalStateException if the game is not started or not finished
	 * (use the message to distinguish these cases, {@link #FINISH_ERROR_NOT_STARTED}
	 * or {@link #FINISH_ERROR_NOT_FINISHED}).
	 */
	public void finishGame() {
		if(!p.getGameManager().isGameRunning()) {
			throw new IllegalStateException(FINISH_ERROR_NOT_STARTED);
		}
		
		if(p.getGameManager().getAliveTeamsCount() != 1) {
			throw new IllegalStateException(FINISH_ERROR_NOT_FINISHED);
		}
		
		// There's only one team.
		UHTeam winnerTeam = p.getGameManager().getAliveTeams().get(0);
		ArrayList<OfflinePlayer> listWinners = winnerTeam.getPlayers();
		
		if(p.getConfig().getBoolean("finish.message")) {
			if(p.getGameManager().isGameWithTeams()) {
				String winners = "";
				
				for(OfflinePlayer winner : listWinners) {
					if(winner == listWinners.get(0)) {
						// Nothing
					}
					else if(winner == listWinners.get(listWinners.size() - 1)) {
						winners += " " + i.t("finish.and") + " ";
					}
					else {
						winners += ", ";
					}
					
					winners += winner.getName();
				}
				
				p.getServer().broadcastMessage(i.t("finish.broadcast.withTeams", winners, winnerTeam.getDisplayName()));
			}
			else {
				p.getServer().broadcastMessage(i.t("finish.broadcast.withoutTeams", winnerTeam.getName()));
			}
		}
		
		if(p.getConfig().getBoolean("finish.fireworks.enabled")) {
			new FireworksOnWinnersTask(p, listWinners).runTaskTimer(p, 0l, 10l);
		}
	}
	
	/**
	 * Returns a list of the currently alive teams.
	 * 
	 * @return The list.
	 */
	public ArrayList<UHTeam> getAliveTeams() {
		ArrayList<UHTeam> aliveTeams = new ArrayList<UHTeam>();
		for (UHTeam t : tm.getTeams()) {
			for (OfflinePlayer p : t.getPlayers()) {
				if (!this.isPlayerDead(p.getUniqueId()) && !aliveTeams.contains(t)) aliveTeams.add(t);
			}
		}
		
		return aliveTeams;
	}
	
	/**
	 * Returns a list of the currently alive players.
	 * 
	 * @return The list.
	 */
	public HashSet<OfflinePlayer> getAlivePlayers() {
		
		HashSet<OfflinePlayer> alivePlayersList = new HashSet<OfflinePlayer>();
		
		for(UUID id : alivePlayers) {
			alivePlayersList.add(p.getServer().getOfflinePlayer(id));
		}
		
		return alivePlayersList;
	}
	
	/**
	 * Returns a list of the currently alive and online players.
	 * 
	 * @return The list.
	 */
	public HashSet<Player> getOnlineAlivePlayers() {
		
		HashSet<Player> alivePlayersList = new HashSet<Player>();
		
		for(UUID id : alivePlayers) {
			Player player = p.getServer().getPlayer(id);
			if(player != null) {
				alivePlayersList.add(player);
			}
		}
		
		return alivePlayersList;
	}
	
	/**
	 * Returns the death sound, or null if no death sound is registered.
	 * 
	 * @return
	 */
	public UHSound getDeathSound() {
		return deathSound;
	}
	
	/**
	 * Returns the length of one episode, in seconds.
	 * 
	 * @return
	 */
	public Integer getEpisodeLength() {
		try {
			return UHUtils.string2Time(p.getConfig().getString("episodes.length"));
		} catch(IllegalArgumentException e) {
			return 20 * 60; // default value, 20 minutes
		}
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
}
