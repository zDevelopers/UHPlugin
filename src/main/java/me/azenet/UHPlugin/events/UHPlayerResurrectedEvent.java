package me.azenet.UHPlugin.events;

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
