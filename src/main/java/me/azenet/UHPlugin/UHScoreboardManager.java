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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UHScoreboardManager {
	
	private UHPlugin p = null;
	private I18n i = null;
	private UHGameManager gm = null;
	private Scoreboard sb = null;
	private Objective objective = null;
	
	// Old values, to be able to update the minimum.
	// Initialized to -1 to force an update at the first launch.
	private Integer oldEpisode = -1;
	private Integer oldAlivePlayersCount = -1;
	private Integer oldAliveTeamsCount = -1;
	private Integer oldHours = 0;
	private Integer oldMinutes = 0;
	private Integer oldSeconds = 0;
	
	// Static values
	private String objectiveName = "UHPlugin";
	private NumberFormat formatter = new DecimalFormat("00");
	
	
	/**
	 * Constructor.
	 * Initializes the scoreboard.
	 * 
	 * @param plugin
	 */
	public UHScoreboardManager(UHPlugin plugin) {
		this.p  = plugin;
		this.i  = p.getI18n();
		this.gm = p.getGameManager();
		this.sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		
		
		// Initialization of the scoreboard (match info in the sidebar)
		if(p.getConfig().getBoolean("scoreboard.enabled")) {
			try {
				this.sb.clearSlot(DisplaySlot.SIDEBAR);
				this.sb.getObjective(objectiveName).unregister();
			} catch(NullPointerException | IllegalArgumentException ignored) { }
			
			this.objective = this.sb.registerNewObjective(objectiveName, "dummy");
			this.objective.setDisplayName(getScoreboardName());
			this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			// The "space" score needs to be registered only one time, and only if the episodes/timer are enabled.
			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer")) {
				this.objective.getScore("").setScore(3);
			}
			
			updateCounters();
			updateTimer();
		}
		
		// Initialization of the scoreboard (health in players' list)
		if(p.getConfig().getBoolean("scoreboard.health")) {
			Objective healthObjective = this.sb.registerNewObjective("Health", Criterias.HEALTH);
			healthObjective.setDisplayName("Health");
			healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
			
			// Sometimes, the health is initialized to 0. This is used to fix this.
			updateHealthScore();
		}
		else {
			this.sb.clearSlot(DisplaySlot.PLAYER_LIST); // Just in case
		}
	}
	
	/**
	 * Resets the score of the timer without hours,
	 * because this score is not reset if the timer is with hours.
	 */
	public void startTimer() {
		sb.resetScores(getTimerText(oldHours, oldMinutes, oldSeconds, true));
	}
	
	/**
	 * Updates the timer of the scoreboard (if needed).
	 */
	public void updateTimer() {
		if(p.getConfig().getBoolean("scoreboard.enabled")) {
			Integer hoursLeft   = gm.getHoursLeft();
			Integer minutesLeft = gm.getMinutesLeft();
			Integer secondsLeft = gm.getSecondsLeft();
			
			// The timer score is reset every time.
			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer") && !p.getGameManager().isTimerPaused()) {
				String oldTimerText = getTimerText(oldHours, oldMinutes, oldSeconds, false);
				String newTimerText = getTimerText(hoursLeft, minutesLeft, secondsLeft, false);
				
				sb.resetScores(oldTimerText);
				objective.getScore(newTimerText).setScore(2);
				
				oldHours = hoursLeft;
				oldMinutes = minutesLeft;
				oldSeconds = secondsLeft;
			}
		}
	}
	
	/**
	 * Updates the counters of the scoreboard (if needed).
	 */
	public void updateCounters() {
		if(p.getConfig().getBoolean("scoreboard.enabled")) {
			Integer episode = gm.getEpisode();
			Integer alivePlayersCount = gm.getAlivePlayersCount();
			Integer aliveTeamsCount = gm.getAliveTeamsCount();
			
			if(!episode.equals(oldEpisode) && p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.episode")) {
				sb.resetScores(getText("episode", oldEpisode));
				objective.getScore(getText("episode", episode)).setScore(6);
				oldEpisode = episode;
			}
			
			if(!alivePlayersCount.equals(oldAlivePlayersCount) && p.getConfig().getBoolean("scoreboard.players")) {
				sb.resetScores(getText("players", oldAlivePlayersCount));
				objective.getScore(getText("players", alivePlayersCount)).setScore(5);
				oldAlivePlayersCount = alivePlayersCount;
			}
			
			// This is displayed when the game is running to avoid a special case used to remove it
			// if the game is without teams.
			if(gm.isGameRunning() && gm.isGameWithTeams() && !aliveTeamsCount.equals(oldAliveTeamsCount) && p.getConfig().getBoolean("scoreboard.teams")) {
				sb.resetScores(getText("teams", oldAliveTeamsCount));
				objective.getScore(getText("teams", aliveTeamsCount)).setScore(4);
				oldAliveTeamsCount = aliveTeamsCount;
			}
		}
	}
	
	/**
	 * Displays the freeze state in the scoreboard.
	 */
	public void displayFreezeState() {
		if(p.getConfig().getBoolean("scoreboard.enabled", true) && p.getConfig().getBoolean("scoreboard.freezeStatus", true)) {
			
			final String freezerStatusText = i.t("freeze.scoreboard").substring(0, Math.min(i.t("freeze.scoreboard").length(), 16));		
			
			if(p.getFreezer().getGlobalFreezeState()) {
				objective.getScore("  ").setScore(1);
				objective.getScore(freezerStatusText).setScore(-1); // Forces the display
				Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
					@Override
					public void run() {
						objective.getScore(freezerStatusText).setScore(0);
					}
				}, 1L);
			}
			else {
				sb.resetScores("  ");
				sb.resetScores(freezerStatusText);
			}
		}
	}
	
	/**
	 * Returns the text displayed in the scoreboard.
	 * 
	 * @param textType Either "episode", "players" or "teams".
	 * @param arg Respectively, the episode number, the players count and the teams count.
	 * @return The text.
	 * @throws IllegalArgumentException if the textType is not one of the listed types.
	 */
	private String getText(String textType, Integer arg) {
		switch(textType) {
			case "episode":
				return i.t("scoreboard.episode", arg.toString());
			case "players":
				return i.t("scoreboard.players", arg.toString());
			case "teams":
				return i.t("scoreboard.teams", arg.toString());
			default:
				throw new IllegalArgumentException("Incorrect text type, see javadoc");
		}
	}
	
	/**
	 * Returns the text displayed in the scoreboard, for the timer.
	 * 
	 * @param hours The hours in the timer
	 * @param minutes The minute in the timer
	 * @param seconds The second in the timer
	 * @param forceNonHoursTimer If true, the non-hours timer text will be returned.
	 * @return The text of the timer
	 */
	private String getTimerText(Integer hours, Integer minutes, Integer seconds, Boolean forceNonHoursTimer) {
		if(gm.displayHourInTimer() && !forceNonHoursTimer) {
			return i.t("scoreboard.timerWithHours", formatter.format(hours), formatter.format(minutes), formatter.format(seconds));
		}
		else {
			return i.t("scoreboard.timer", formatter.format(minutes), formatter.format(seconds));
		}
	}
	 
	/**
	 * Updates the health score for all players.
	 */
	public void updateHealthScore() {
		for(final Player player : p.getServer().getOnlinePlayers()) {
			updateHealthScore(player);
		}
	}
	
	/**
	 * Updates the health score for the given player.
	 * 
	 * @param player The player to update.
	 */
	public void updateHealthScore(final Player player) {
		if(player.getHealth() != 1d) { // Prevents killing the player
			player.setHealth(player.getHealth() - 1);
			
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					if(player.getHealth() <= 19d) { // Avoids an IllegalArgumentException
						player.setHealth(player.getHealth() + 1);
					}
				}
			}, 3L);
		}
	}
	
	/**
	 * Tells the player's client to use this scoreboard.
	 * 
	 * @param p The player.
	 */
	public void setScoreboardForPlayer(Player p) {
		p.setScoreboard(sb);
	}
	
	/**
	 * Returns the title of the scoreboard, truncated at 32 characters.
	 * 
	 * @return The name
	 */
	public String getScoreboardName() {
		String s = ChatColor.translateAlternateColorCodes('&', p.getConfig().getString("scoreboard.title", "Kill the Patrick"));
		return s.substring(0, Math.min(s.length(), 32));
	}
	
	/**
	 * Returns the internal scoreboard.
	 * 
	 * @return The internal scoreboard.
	 */
	public Scoreboard getScoreboard() {
		return sb;
	}
}
