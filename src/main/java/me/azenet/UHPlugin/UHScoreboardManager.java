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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.azenet.UHPlugin.i18n.I18n;

import org.apache.commons.lang.Validate;
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
	
	// Timers
	
	/**
	 * Represents the position of each displayed timer.
	 * <ul>
	 *  <li><tt>This position</tt>: the space before the timer.</li>
	 *  <li><tt>This position - 1</tt>: the name of the timer.</li>
	 *  <li><tt>This position - 2</tt>: the timer.</li>
	 * </ul>
	 */
	private Map<UHTimer,Integer> timersPositions = new HashMap<UHTimer,Integer>();
	
	/**
	 * The first position of the timers (main timer excluded).
	 * <p>
	 * -1, so there isn't any problem related to the line with a score equals to 0 
	 * (not displayed except if the score is set to 1, then to 0 one tick later).
	 */
	private static final Integer TIMERS_FIRST_POSITION = 81;
	
	/**
	 * The last position used by the timers.
	 */
	private Integer timersLastUsedPosition = null;
	
	/**
	 * The number of spaces in the separator. Max 16.
	 */
	private Map<UHTimer,Integer> numberOfSpacesInSeparator = new HashMap<UHTimer,Integer>();
	
	/**
	 * The biggest number of spaces used, for the separators.
	 * <p>
	 * 1 because "" is used by the main timer, and " " by the "Game Frozen" text.
	 */
	private Integer biggestNumberOfSpacesUsed = 1;
	
	
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
				this.objective.getScore("").setScore(85);
				
				// Displays a fake, frozen timer if the game is not started.
				this.objective.getScore(this.getTimerText(new UHTimer(""), true, false)).setScore(84);
			}
			
			updateCounters();
			updateTimers();
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
		this.sb.resetScores(this.getTimerText(new UHTimer(""), true, false));
	}
	
	/**
	 * Displays a timer in the scoreboard.
	 * 
	 * @param timer
	 */
	public void displayTimer(UHTimer timer) {
		if(timersPositions.containsKey(timer)) {
			return; // already displayed
		}
		
		
		// Position of the timer
		Integer position = null;
		if(timersLastUsedPosition == null) {
			position = TIMERS_FIRST_POSITION;
		}
		else {
			position = timersLastUsedPosition - 3;
		}
		
		timersPositions.put(timer, position);
		timersLastUsedPosition = position;
		
		// Number of spaces in the separator
		numberOfSpacesInSeparator.put(timer, biggestNumberOfSpacesUsed + 1);
		biggestNumberOfSpacesUsed++;
		
		// Effective display
		objective.getScore(getValidScoreName(generateSpaces(numberOfSpacesInSeparator.get(timer)))).setScore(position);
		objective.getScore(getValidScoreName(timer.getName())).setScore(position - 1);
		objective.getScore(getValidScoreName(getTimerText(timer, false, false))).setScore(position - 2);
	}
	
	/**
	 * Hides a timer, in the scoreboard.
	 * 
	 * @param timer
	 */
	public void hideTimer(UHTimer timer) {
		// The timer is removed from the lists of the displayed timers.
		// All the positions of the displayed timers are changed, as well as the number of spaces used.
		
		if(!timersPositions.containsKey(timer)) {
			return;
		}
		
		// The timer is hidden
		sb.resetScores(getValidScoreName(generateSpaces(numberOfSpacesInSeparator.get(timer))));
		sb.resetScores(getValidScoreName(timer.getName()));
		sb.resetScores(getValidScoreName(getTimerText(timer, false, false)));
		sb.resetScores(getValidScoreName(getTimerText(timer, false, true)));
		
		Integer oldPosition = timersPositions.get(timer);
		
		timersPositions.remove(timer);
		numberOfSpacesInSeparator.remove(timer);
		
		for(Entry<UHTimer, Integer> entry : timersPositions.entrySet()) {
			// If the timer is below the deleted timer, the position and the spaces in the separator
			// are changed.
			
			if(entry.getValue() < oldPosition) {
				UHTimer thisTimer = entry.getKey();
				Integer newPosition = entry.getValue() + 3;
				
				timersPositions.put(thisTimer, newPosition);
				numberOfSpacesInSeparator.put(thisTimer, numberOfSpacesInSeparator.get(thisTimer) - 1);
				
				// Effective display
				sb.resetScores(getValidScoreName(generateSpaces(numberOfSpacesInSeparator.get(thisTimer) + 1))); // resets the old "space" score
				objective.getScore(getValidScoreName(generateSpaces(numberOfSpacesInSeparator.get(thisTimer)))).setScore(newPosition);
				objective.getScore(getValidScoreName(thisTimer.getName())).setScore(newPosition - 1);
				objective.getScore(getValidScoreName(getTimerText(thisTimer, false, false))).setScore(newPosition - 2);
			}
		}
		
		if(timersLastUsedPosition + 3 < TIMERS_FIRST_POSITION) {
			timersLastUsedPosition += 3;
		}
		else {
			timersLastUsedPosition = null; // no timers left.
		}
		
		biggestNumberOfSpacesUsed--;
	}
	
	/**
	 * Remove the old timers text when a pause is stopped, because the usual solution doesn't work here
	 * (the old values stored in the timers are the same as the current ones due to an update after a freeze).
	 * 
	 * This method is called before the update of the timers.
	 */
	public void restartTimers() {		
		// Main timer
		if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer") && p.getTimerManager().getMainTimer() != null) {		
			sb.resetScores(getTimerText(p.getTimerManager().getMainTimer(), false, true));
			sb.resetScores(getTimerText(p.getTimerManager().getMainTimer(), false, false));			
		}
		
		
		// Other timers
		for(Entry<UHTimer,Integer> entry : timersPositions.entrySet()) {
			sb.resetScores(getTimerText(entry.getKey(), false, true));
			sb.resetScores(getTimerText(entry.getKey(), false, false));
		}
	}
	
	/**
	 * Updates the timers of the scoreboard (if needed).
	 */
	public void updateTimers() {
		if(p.getConfig().getBoolean("scoreboard.enabled") && !p.getFreezer().getGlobalFreezeState()) {
			
			// Main timer
			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer") && p.getTimerManager().getMainTimer() != null) {
				sb.resetScores(getTimerText(p.getTimerManager().getMainTimer(), false, true));
				objective.getScore(getTimerText(p.getTimerManager().getMainTimer(), false, false)).setScore(84);
			}
			
			
			// Other timers
			for(Entry<UHTimer,Integer> entry : timersPositions.entrySet()) {
				sb.resetScores(getTimerText(entry.getKey(), false, true));
				objective.getScore(getTimerText(entry.getKey(), false, false)).setScore(entry.getValue() - 2);
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
				objective.getScore(getText("episode", episode)).setScore(88);
				oldEpisode = episode;
			}
			
			if(!alivePlayersCount.equals(oldAlivePlayersCount) && p.getConfig().getBoolean("scoreboard.players")) {
				sb.resetScores(getText("players", oldAlivePlayersCount));
				objective.getScore(getText("players", alivePlayersCount)).setScore(87);
				oldAlivePlayersCount = alivePlayersCount;
			}
			
			// This is displayed when the game is running to avoid a special case used to remove it
			// if the game is without teams.
			if(gm.isGameRunning() && gm.isGameWithTeams() && !aliveTeamsCount.equals(oldAliveTeamsCount) && p.getConfig().getBoolean("scoreboard.teams")) {
				sb.resetScores(getText("teams", oldAliveTeamsCount));
				objective.getScore(getText("teams", aliveTeamsCount)).setScore(86);
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
				objective.getScore(" ").setScore(83);
				objective.getScore(freezerStatusText).setScore(82);
			}
			else {
				sb.resetScores(" ");
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
	 * @param timer The timer to display.
	 * @param forceNonHoursTimer If true, the non-hours timer text will be returned.
	 * @param useOldValues if true, the old values of the timer will be used.
	 * @return The text of the timer.
	 */
	protected String getTimerText(UHTimer timer, Boolean forceNonHoursTimer, Boolean useOldValues) {
		Validate.notNull(timer, "The timer cannot be null");
		
		if(timer.getDisplayHoursInTimer() && !forceNonHoursTimer) {
			if(useOldValues) {
				return getTimerText(timer.getOldHoursLeft(), timer.getOldMinutesLeft(), timer.getOldSecondsLeft(), true);
			}
			else {
				return getTimerText(timer.getHoursLeft(), timer.getMinutesLeft(), timer.getSecondsLeft(), true);
			}
		}
		else {
			if(useOldValues) {
				return getTimerText(0, timer.getOldMinutesLeft(), timer.getOldSecondsLeft(), false);
			}
			else {
				return getTimerText(0, timer.getMinutesLeft(), timer.getSecondsLeft(), false);
			}
		}
	}
	
	/**
	 * Returns the text displayed in the scoreboard, for the give values.
	 * 
	 * @param hours The hours in the timer.
	 * @param minutes The minutes in the timer.
	 * @param seconds The seconds in the timer.
	 * @param displayHours If true, "hh:mm:ss"; else, "mm:ss".
	 * 
	 * @return The text of the timer.
	 */
	private String getTimerText(Integer hours, Integer minutes, Integer seconds, Boolean displayHours) {		
		if(displayHours) {
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
	 * Generates a string containing {@code spaces} spaces.
	 * 
	 * @param spaces The number of spaces in the string.
	 * 
	 * @return The string.
	 */
	protected String generateSpaces(int spaces) {
		String space = "";
		
		for(int i = 0; i < spaces; i++) {
			space += " ";
		}
		
		return space;
	}
	
	/**
	 * Returns a string that can fit in a scoreboard.
	 * 
	 * Aka, cut the given string at the 16th character.
	 * 
	 * @param original The original string.
	 * @return The cut string.
	 */
	public String getValidScoreName(String original) {
		return original.substring(0, Math.min(original.length(), 16));
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
