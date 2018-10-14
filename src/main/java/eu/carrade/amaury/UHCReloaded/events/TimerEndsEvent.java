/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package eu.carrade.amaury.UHCReloaded.events;

import eu.carrade.amaury.UHCReloaded.old.timers.UHTimer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * This event is fired when a timer ends.
 * <p>
 * It is fired before all the values of the timer are reset.
 */
public final class TimerEndsEvent extends Event
{
    private UHTimer timer;
    private Boolean timerWasUp = false;
    private Boolean restart = false;


    public TimerEndsEvent(UHTimer timer, Boolean timerUp)
    {
        this.timer = timer;

        this.timerWasUp = timerUp;
    }

    /**
     * Returns the timer.
     *
     * @return the timer.
     */
    public UHTimer getTimer()
    {
        return timer;
    }

    /**
     * Returns true if the timer was stopped because it was up.
     *
     * @return true if the timer was stopped because it was up.
     */
    public boolean wasTimerUp()
    {
        return timerWasUp;
    }

    /**
     * If true, the timer will be restarted.
     *
     * @param restart true if the timer needs to be restarted.
     */
    public void setRestart(boolean restart)
    {
        this.restart = restart;
    }

    /**
     * Return true if the timer will be restarted.
     *
     * @return {@code true} if the timer needs to be restarted.
     */
    public boolean getRestart()
    {
        return this.restart;
    }



    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
