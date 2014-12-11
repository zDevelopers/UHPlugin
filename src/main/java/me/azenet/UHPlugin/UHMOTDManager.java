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

package me.azenet.UHPlugin;

import org.bukkit.ChatColor;

import me.azenet.UHPlugin.i18n.I18n;

public class UHMOTDManager {
	
	private UHPlugin p;
	private I18n i;
	
	private boolean enabled;
	private String matchName = "";
	
	private String currentMOTD;
	
	public UHMOTDManager(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();
		
		enabled = p.getConfig().getBoolean("motd.enabled");
		
		if(enabled && p.getConfig().getBoolean("motd.displayMatchName")) {
			matchName = ChatColor.translateAlternateColorCodes('&', p.getConfig().getString("motd.matchNamePrefix")) + p.getScoreboardManager().getScoreboardName() + ChatColor.RESET + "\n";
		}
	}
	
	/**
	 * Returns the current MOTD.
	 * 
	 * @return The MOTD.
	 */
	public String getCurrentMOTD() {
		return currentMOTD;
	}
	
	/**
	 * Returns true if the state-based MOTDs are enabled.
	 * 
	 * @return true if enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	
	/**
	 * Updates the MOTD to the one displayed before the game start.
	 */
	public void updateMOTDBeforeStart() {
		if(enabled) currentMOTD = matchName + i.t("motd.beforeStart");
	}
	
	/**
	 * Updates the MOTD to the one displayed during the start.
	 */
	public void updateMOTDDuringStart() {
		if(enabled) currentMOTD = matchName + i.t("motd.starting");
	}
	
	/**
	 * Updates the MOTD to the one displayed during the game (includes alive counts).
	 * <p>
	 * This need to be called on each death, to update alive counts.
	 */
	public void updateMOTDDuringGame() {
		if(enabled) {
			if(!p.getGameManager().isGameWithTeams()) {
				currentMOTD = matchName + i.t("motd.runningSolo", String.valueOf(p.getGameManager().getAlivePlayersCount()));
			}
			else {
				currentMOTD = matchName + i.t("motd.runningTeams", String.valueOf(p.getGameManager().getAlivePlayersCount()), String.valueOf(p.getGameManager().getAliveTeamsCount()));
			}
		}
	}
	
	/**
	 * Updates the MOTD after the game.
	 * 
	 * @param winner The winner.
	 */
	public void updateMOTDAfterGame(UHTeam winner) {
		if(enabled) {
			if(!p.getGameManager().isGameWithTeams()) {
				currentMOTD = matchName + i.t("motd.finishedSolo", winner.getName());
			}
			else {
				currentMOTD = matchName + i.t("motd.finishedTeams", winner.getDisplayName());
			}
		}
	}
}
