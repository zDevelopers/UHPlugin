package me.azenet.UHPlugin.events;

import me.azenet.UHPlugin.UHTimer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a timer ends.
 * 
 * @author Amaury Carrade
 */
public final class TimerStartsEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UHTimer timer;
 
    public TimerStartsEvent(UHTimer timer) {
        this.timer = timer;
    }
    
    /**
     * Returns the timer.
     * 
     * @return
     */
    public UHTimer getTimer() {
    	return timer;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}