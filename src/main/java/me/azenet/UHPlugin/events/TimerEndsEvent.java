package me.azenet.UHPlugin.events;

import me.azenet.UHPlugin.UHTimer;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a timer ends.
 * 
 * @author Amaury Carrade
 */
public final class TimerEndsEvent extends Event {
    
	private static final HandlerList handlers = new HandlerList();
    
	private UHTimer timer;
    private Boolean timerWasUp = false;
    
    
    public TimerEndsEvent(UHTimer timer, Boolean timerUp) {
       this.timer = timer;
       
       this.timerWasUp = timerUp;
    }
    
    /**
     * Returns the timer.
     * 
     * @return the timer.
     */
    public UHTimer getTimer() {
    	return timer;
    }
    
    /**
     * Returns true if the timer was stopped because it was up.
     * 
     * @return true if the timer was stopped because it was up.
     */
    public boolean wasTimerUp() {
    	return timerWasUp;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}