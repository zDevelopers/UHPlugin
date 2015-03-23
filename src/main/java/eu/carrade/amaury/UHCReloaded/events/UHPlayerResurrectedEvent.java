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

package eu.carrade.amaury.UHCReloaded.events;

import org.bukkit.entity.Player;

/**
 * Called when a player is resurrected.
 * <p>
 * This event is called when:
 * <ul>
 *   <li>the command {@code /uh resurrect <player>} is executed, if the target is online;</li>
 *   <li>the resurrected player logins, else</li>
 * </ul>
 * (i.e. when the message “the player is resurrected” is broadcasted).
 */
public class UHPlayerResurrectedEvent extends UHEvent {
	
	private Player resurrectedPlayer;
	
	public UHPlayerResurrectedEvent(Player player) {
		this.resurrectedPlayer = player;
	}
	
	/**
	 * Returns the resurrected player.
	 * 
	 * @return The player.
	 */
	public Player getPlayer() {
		return resurrectedPlayer;
	}
}
