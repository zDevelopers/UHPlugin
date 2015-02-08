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

package me.azenet.UHPlugin.timers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class UHTimerManager {
	
	private Map<String,UHTimer> timers = new HashMap<String,UHTimer>();
	private UHTimer mainTimer = null;
	
	/**
	 * Cached list of the running timers
	 */
	private Map<String,UHTimer> runningTimers = new HashMap<String,UHTimer>();
	
	/**
	 * List of the timers to resume if running timers are paused.
	 * 
	 * @see {@link #pauseAllRunning(boolean)}.
	 */
	private HashSet<UHTimer> timersToResume = new HashSet<UHTimer>();
	
	
	public UHTimerManager() {
		
	}
	
	/**
	 * Registers the main timer, used to display the episodes countdown.
	 *
	 * @param timer The timer.
	 */
	public void registerMainTimer(UHTimer timer) {
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
		timers.remove(timer.getName());
		runningTimers.remove(timer.getName());
		
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
	 * @return The timer, or null if there isn't any timer with this name.
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
	 * Pauses (or resumes) all the running timers.
	 * 
	 * @param paused If true, all the timers will be paused. Else, resumed.
	 */
	public void pauseAll(boolean paused) {
		for(UHTimer timer : getRunningTimers()) {
			timer.setPaused(paused);
		}
		
		if(!paused) {
			// If we restart all the timers regardless to their previous state,
			// this data is meaningless.
			timersToResume.clear();
		}
	}
	
	/**
	 * Pauses (or resumes) all the running timers.
	 * <p>
	 * This method will only resume the previously-running timers.
	 * 
	 * @param paused If true, all the timers will be paused. Else, resumed.
	 */
	public void pauseAllRunning(boolean paused) {
		if(paused) {
			for(UHTimer timer : getRunningTimers()) {
				if(!timer.isPaused()) {
					timer.setPaused(true);
					timersToResume.add(timer);
				}
			}
		}
		else {
			for(UHTimer timer : timersToResume) {
				timer.setPaused(false);
			}
			
			timersToResume.clear();
		}
	}
}
