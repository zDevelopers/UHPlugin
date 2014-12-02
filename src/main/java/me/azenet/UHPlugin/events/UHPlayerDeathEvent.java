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

package me.azenet.UHPlugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Fired when a player playing an UHC match is dead.
 * <p>
 * This event is called before all the action executed on player death (sound, scoreboard updates, etc.).
 */
public class UHPlayerDeathEvent extends UHEvent {
	
	private Player player;
	private PlayerDeathEvent ev;
	
	public UHPlayerDeathEvent(Player player, PlayerDeathEvent ev) {
		this.player = player;
		this.ev = ev;
	}

	/**
	 * Returns the dead player.
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Returns the PlayerDeathEvent under this event.
	 * @return The PlayerDeathEvent.
	 */
	public PlayerDeathEvent getPlayerDeathEvent() {
		return ev;
	}
}
