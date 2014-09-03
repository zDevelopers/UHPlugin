package me.azenet.UHPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UHTimerManager {
	
	private Map<String,UHTimer> timers = new HashMap<String,UHTimer>();
	private UHTimer mainTimer = null;
	
	// Cached list of the running timers
	private Map<String,UHTimer> runningTimers = new HashMap<String,UHTimer>();
	
	public UHTimerManager() {
		
	}
	
	/**
	 * Registers the main timer, used to display the episodes countdown.
	 * 
	 * @param timer The timer.
	 */
	protected void registerMainTimer(UHTimer timer) {
		this.mainTimer = timer;
		timer.setRegistered(true);
	}
	
	/**
	 * Returns the main timer, used to display the episodes countdown.
	 * 
	 * @return The main timer.
	 */
	public UHTimer getMainTimer() {
		return this.mainTimer;
	}
	
	/**
	 * Registers a timer.
	 * 
	 * @param timer The timer to register.
	 * @throws IllegalArgumentException if a timer with the same name is already registered.
	 */
	public void registerTimer(UHTimer timer) {
		if(timers.get(timer.getName()) != null) {
			throw new IllegalArgumentException("A timer with the name " + timer.getName() + " is already registered.");
		}
		
		timers.put(timer.getName(), timer);
		
		timer.setRegistered(true);
	}
	
	/**
	 * Unregisters a timer.
	 * <p>
	 * If the timer was not registered, nothing is done.
	 * 
	 * @param timer The timer to unregister.
	 */
	public void unregisterTimer(UHTimer timer) {
		timers.remove(timer);
		runningTimers.remove(timer);
		
		timer.setRegistered(false);
	}
	
	/**
	 * Updates the internal list of started timers.
	 */
	public void updateStartedTimersList() {
		runningTimers = new HashMap<String,UHTimer>();
		
		if(getMainTimer() != null && getMainTimer().isRunning()) {
			runningTimers.put(getMainTimer().getName(), getMainTimer());
		}
		
		for(UHTimer timer : timers.values()) {
			if(timer.isRunning()) {
				runningTimers.put(timer.getName(), timer);
			}
		}
	}
	
	/**
	 * Returns a timer by his name.
	 * 
	 * @param name The name of the timer.
	 * 
	 * @return The timer.
	 */
	public UHTimer getTimer(String name) {
		return timers.get(name);
	}
	
	/**
	 * Returns a collection containing the registered timers.
	 * 
	 * @return The collection.
	 */
	public Collection<UHTimer> getTimers() {
		return timers.values();
	}
	
	/**
	 * Returns a collection containing the running timers.
	 * 
	 * @return The collection.
	 */
	public Collection<UHTimer> getRunningTimers() {
		return runningTimers.values();
	}
	
	/**
	 * Pauses (or unpauses) all the running timers.
	 * 
	 * @param paused If true, all the timers will be paused. Else, restarted.
	 */
	public void pauseAll(boolean paused) {
		for(UHTimer timer : getRunningTimers()) {
			timer.setPaused(paused);
		}
	}
}
