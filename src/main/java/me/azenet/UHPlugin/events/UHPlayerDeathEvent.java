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
