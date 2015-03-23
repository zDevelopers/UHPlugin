/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
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

package eu.carrade.amaury.UHCReloaded.scoreboard;

import eu.carrade.amaury.UHCReloaded.UHGameManager;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {

	private UHCReloaded p = null;
	private I18n i = null;
	private UHGameManager gm = null;
	private Scoreboard sb = null;
	private SidebarObjective sidebar = null;

	// Old values, to be able to update the minimum.
	// Initialized to -1 to force an update at the first launch.
	private Integer oldEpisode = -1;
	private Integer oldAlivePlayersCount = -1;
	private Integer oldAliveTeamsCount = -1;

	// Timers
	private List<UHTimer> displayedTimers = new ArrayList<UHTimer>();


	// Static values
	private String objectiveName = "UHPlugin";
	private NumberFormat formatter = new DecimalFormat("00");


	/**
	 * Constructor.
	 * Initializes the scoreboard.
	 *
	 * @param plugin
	 */
	public ScoreboardManager(UHCReloaded plugin) {
		this.p  = plugin;
		this.i  = p.getI18n();
		this.gm = p.getGameManager();
		this.sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();


		// Initialization of the scoreboard (match info in the sidebar)
		if(p.getConfig().getBoolean("scoreboard.enabled")) {
			try {
				sb.clearSlot(DisplaySlot.SIDEBAR);
				sb.getObjective(objectiveName).unregister();
			} catch(NullPointerException | IllegalArgumentException ignored) { }

			sidebar = new SidebarObjective(sb, objectiveName);
			sidebar.setDisplayName(getScoreboardName());

			buildSidebar();
		}

		// Initialization of the scoreboard (health in players' list)
		if(p.getConfig().getBoolean("scoreboard.health")) {
			Objective healthObjective = sb.registerNewObjective("Health", Criterias.HEALTH);
			healthObjective.setDisplayName("Health");
			healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

			// Sometimes, the health is initialized to 0. This is used to fix this.
			updateHealthScore();
		}
		else {
			sb.clearSlot(DisplaySlot.PLAYER_LIST); // Just in case
		}
	}

	/**
	 * Used to displays the player count before the beginning of the game.
	 *
	 * To be called when a player joins.
	 */
	public void addPlayerBeforeStart() {
		sidebar.updateEntry(this.getText("players", p.getServer().getOnlinePlayers().size() - 1),
				this.getText("players", p.getServer().getOnlinePlayers().size()));
	}

	/**
	 * Used to displays the player count before the beginning of the game.
	 *
	 * To be called when a player leaves.
	 */
	public void removePlayerBeforeStart() {
		sidebar.updateEntry(this.getText("players", p.getServer().getOnlinePlayers().size() + 1),
				this.getText("players", p.getServer().getOnlinePlayers().size()));
	}

	/**
	 * Re-builds the sidebar from scratch.
	 */
	public void buildSidebar() {
		if(!p.getConfig().getBoolean("scoreboard.enabled")) {
			return;
		}

		sidebar.reset(true);

		// Initial state (before start)
		if(!gm.isGameStarted()) {
			if(p.getConfig().getBoolean("episodes.enabled")) {
				sidebar.addEntry(this.getText("episode", 0), true);
			}

			sidebar.addEntry(this.getText("players", p.getServer().getOnlinePlayers().size()), true);

			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer")) {
				sidebar.addEntry(SidebarObjective.SEPARATOR, true);

				// Displays a fake, frozen timer if the game is not started.
				sidebar.addEntry(this.getTimerText(new UHTimer(""), true, false), true);
			}

			buildTimersSidebar();
			displayFreezeState();

			sidebar.reconstruct();
		}

		// In-game state
		else {
			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.episode")) {
				sidebar.addEntry(this.getText("episode", gm.getEpisode()), true);
				oldEpisode = gm.getEpisode();
			}

			if(p.getConfig().getBoolean("scoreboard.players")) {
				sidebar.addEntry(this.getText("players", p.getGameManager().getAlivePlayersCount()), true);
				oldAlivePlayersCount = p.getGameManager().getAlivePlayersCount();
			}

			if(gm.isGameWithTeams() && p.getConfig().getBoolean("scoreboard.teams")) {
				sidebar.addEntry(this.getText("teams", p.getGameManager().getAliveTeamsCount()), true);
				oldAliveTeamsCount = p.getGameManager().getAliveTeamsCount();
			}

			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer") && p.getTimerManager().getMainTimer() != null) {
				sidebar.addEntry(SidebarObjective.SEPARATOR, true);
				sidebar.addEntry(getTimerText(p.getTimerManager().getMainTimer(), false, false), true);
			}

			buildTimersSidebar();
			displayFreezeState();

			sidebar.reconstruct();
		}
	}

	/**
	 * Adds the timers to the sidebar.
	 *
	 * <p>Appends to the current sidebar. This method will *not* reconstruct the sidebar.</p>
	 */
	private void buildTimersSidebar() {
		for(UHTimer timer : displayedTimers) {
			sidebar.addEntry(SidebarObjective.SEPARATOR, true);
			sidebar.addEntry(timer.getDisplayName(), true);
			sidebar.addEntry(getTimerText(timer, false, false), true);
		}
	}

	/**
	 * Displays a timer in the scoreboard.
	 *
	 * @param timer
	 */
	public void displayTimer(UHTimer timer) {
		if(!displayedTimers.contains(timer)) {
			displayedTimers.add(timer);
			buildSidebar();
		}
	}

	/**
	 * Hides a timer, in the scoreboard.
	 *
	 * @param timer
	 */
	public void hideTimer(UHTimer timer) {
		if(displayedTimers.remove(timer)) {
			buildSidebar();
		}
	}

	/**
	 * The sidebar needs to be reconstructed when the timers are restarted, to avoid
	 * duplicated values.
	 */
	public void restartTimers() {
		if(p.getConfig().getBoolean("scoreboard.enabled")) {
			buildSidebar();
		}
	}

	/**
	 * Updates the timers of the scoreboard (if needed).
	 */
	public void updateTimers() {
		if(p.getConfig().getBoolean("scoreboard.enabled") && !p.getFreezer().getGlobalFreezeState()) {

			// Main timer
			if(p.getConfig().getBoolean("episodes.enabled") && p.getConfig().getBoolean("scoreboard.timer") && p.getTimerManager().getMainTimer() != null) {
				sidebar.updateEntry(getTimerText(p.getTimerManager().getMainTimer(), false, true), getTimerText(p.getTimerManager().getMainTimer(), false, false));
			}


			// Other timers
			for(UHTimer timer : displayedTimers) {
				sidebar.updateEntry(getTimerText(timer, false, true), getTimerText(timer, false, false));
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
				if(oldEpisode == -1) oldEpisode = 0; // May happens the first time

				sidebar.updateEntry(getText("episode", oldEpisode), getText("episode", episode));
				oldEpisode = episode;
			}

			if(!alivePlayersCount.equals(oldAlivePlayersCount) && p.getConfig().getBoolean("scoreboard.players")) {
				// Needed if the game is launched without any player, and players are marked as alive later.
				if(oldAlivePlayersCount == -1) oldAlivePlayersCount = 0;

				sidebar.updateEntry(getText("players", oldAlivePlayersCount), getText("players", alivePlayersCount));
				oldAlivePlayersCount = alivePlayersCount;
			}

			if(gm.isGameWithTeams() && !aliveTeamsCount.equals(oldAliveTeamsCount) && p.getConfig().getBoolean("scoreboard.teams")) {
				// Needed if the game is launched without any player, and players are marked as alive later.
				if(oldAliveTeamsCount == -1) oldAliveTeamsCount = 0;

				sidebar.updateEntry(getText("teams", oldAliveTeamsCount), getText("teams", aliveTeamsCount));
				oldAliveTeamsCount = aliveTeamsCount;
			}
		}
	}

	/**
	 * Displays the given freeze state in the scoreboard.
	 *
	 * @param frozen If {@code true}, the "frozen" text will be displayed. Else, hidden.
	 */
	private void displayFreezeState(boolean frozen) {
		if(p.getConfig().getBoolean("scoreboard.enabled", true) && p.getConfig().getBoolean("scoreboard.freezeStatus", true)) {

			final String freezerStatusText = i.t("freeze.scoreboard");

			if(frozen) {
				sidebar.addEntry(SidebarObjective.SEPARATOR, true);
				sidebar.addEntry(freezerStatusText, true);
				sidebar.reconstruct();
			}
			else {
				int sepIndex = sidebar.getEntryIndex(freezerStatusText) - 1;
				if(sepIndex != -2) {
					sidebar.removeEntryAtIndex(sepIndex, true);
					sidebar.removeEntry(freezerStatusText, true);
					sidebar.reconstruct();
				}
			}
		}
	}

	/**
	 * Displays the freeze state in the scoreboard.
	 */
	public void displayFreezeState() {
		displayFreezeState(p.getFreezer().getGlobalFreezeState());
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
	public String getTimerText(UHTimer timer, Boolean forceNonHoursTimer, Boolean useOldValues) {
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
