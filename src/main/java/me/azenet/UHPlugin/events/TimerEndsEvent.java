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

import me.azenet.UHPlugin.timers.UHTimer;

/**
 * This event is fired when a timer ends.
 * <p>
 * It is fired before all the values of the timer are reset.
 */
public final class TimerEndsEvent extends UHEvent {
    
	private UHTimer timer;
    private Boolean timerWasUp = false;
    private Boolean restart = false;
    
    
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
    
    /**
     * If true, the timer will be restarted.
     * 
     * @param restart true if the timer needs to be restarted.
     */
    public void setRestart(boolean restart) {
    	this.restart = restart;
    }
    
    /**
     * Return true if the timer will be restarted.
     * 
     * @param restart true if the timer will be restarted.
     * @return 
     */
    public boolean getRestart() {
    	return this.restart;
    }
}