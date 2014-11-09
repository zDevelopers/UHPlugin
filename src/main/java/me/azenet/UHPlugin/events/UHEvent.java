package me.azenet.UHPlugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An UHC Reloaded event
 */
public class UHEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
        return handlers;
    }
}
