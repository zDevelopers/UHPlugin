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
package eu.carrade.amaury.UHCReloaded.scoreboard.managers;

import org.bukkit.entity.Player;


public abstract class ScoreboardManager {

	/**
	 * Returns a new instance of the Scoreboard Manager.
	 *
	 * @return An instance.
	 */
	public static ScoreboardManager getNewInstance() {
		return null;
	}


	/**
	 * Ensures the given player will use the good scoreboard.
	 *
	 * @param player The player.
	 */
	public abstract void setScoreboardForPlayer(Player player);

	/**
	 * Schedules an update of the scoreboard.
	 */
	public abstract void launchUpdateCycle();

	/**
	 * Builds the sidebar from scratch.
	 *
	 * @param player
	 */
	public abstract void rebuildSidebar(Player player);

	/**
	 * Updates the sidebar for the given player.
	 *
	 * <p>
	 *     This method assumes the scoreboard was already built.
	 * </p>
	 *
	 * @param player
	 */
	public abstract void updateSidebar(Player player);


	/**
	 * The top of the sidebar is always the same for both of the sidebars per-player
	 * and one-for-all.
	 *
	 * <p>
	 *     This method injects this section of the sidebar.
	 * </p>
	 *
	 * <p>
	 *     This method WILL be called asynchronously if the sidebars per-player are used.
	 * </p>
	 *
	 * @param sidebar The sidebar to init.
	 */
	public void initSidebarTop(SidebarObjective sidebar) {

	}

	/**
	 * The bottom of the sidebar is always the same for both of the sidebars per-player
	 * and one-for-all: it's the timers and the main timer.
	 *
	 * <p>
	 *     This method injects this section of the sidebar, at the bottom of it.
	 * </p>
	 *
	 * <p>
	 *     This method WILL be called asynchronously if the sidebars per-player are used.
	 * </p>
	 *
	 * @param sidebar The sidebar to init.
	 */
	public void initSidebarBottom(SidebarObjective sidebar) {

	}

	/**
	 * Updates the common top of the sidebars.
	 *
	 * <p>
	 *     This method WILL be called asynchronously if the sidebars per-player are used.
	 * </p>
	 *
	 * @param sidebar The sidebar to update.
	 */
	public void updateSidebarTop(SidebarObjective sidebar) {

	}

	/**
	 * Updates the common bottom of the sidebars.
	 *
	 * <p>
	 *     This method WILL be called asynchronously if the sidebars per-player are used.
	 * </p>
	 *
	 * @param sidebar The sidebar to update.
	 */
	public void updateSidebarBottom(SidebarObjective sidebar) {

	}
}
