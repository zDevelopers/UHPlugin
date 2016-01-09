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

package eu.carrade.amaury.UHCReloaded.timers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


public class TimerManager
{

    private Map<String, UHTimer> timers = new ConcurrentHashMap<>();
    private UHTimer mainTimer = null;

    /**
     * Cached list of the running timers
     */
    private Map<String, UHTimer> runningTimers = new ConcurrentHashMap<>();

    /**
     * List of the timers to resume if running timers are paused.
     *
     * @see {@link #pauseAllRunning(boolean)}.
     */
    private Set<UHTimer> timersToResume = new CopyOnWriteArraySet<>();


    public TimerManager()
    {

    }

    /**
     * Registers the main timer, used to display the episodes countdown.
     *
     * @param timer The timer.
     */
    public void registerMainTimer(UHTimer timer)
    {
        this.mainTimer = timer;
        timer.setRegistered(true);
    }

    /**
     * Returns the main timer, used to display the episodes countdown.
     *
     * @return The main timer.
     */
    public UHTimer getMainTimer()
    {
        return this.mainTimer;
    }

    /**
     * Registers a timer.
     *
     * @param timer The timer to register.
     * @throws IllegalArgumentException if a timer with the same name is already registered.
     */
    public void registerTimer(UHTimer timer)
    {

        if (timers.get(timer.getName()) != null)
        {
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
    public void unregisterTimer(UHTimer timer)
    {
        timers.remove(timer.getName());
        runningTimers.remove(timer.getName());

        timer.setRegistered(false);
    }

    /**
     * Updates the internal list of started timers.
     */
    public void updateStartedTimersList()
    {
        runningTimers = new HashMap<>();

        if (getMainTimer() != null && getMainTimer().isRunning())
        {
            runningTimers.put(getMainTimer().getName(), getMainTimer());
        }

        for (UHTimer timer : timers.values())
        {
            if (timer.isRunning())
            {
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
    public UHTimer getTimer(String name)
    {
        return timers.get(name);
    }

    /**
     * Returns a collection containing the registered timers.
     *
     * @return The collection.
     */
    public Collection<UHTimer> getTimers()
    {
        return timers.values();
    }

    /**
     * Returns a collection containing the running timers.
     *
     * @return The collection.
     */
    public Collection<UHTimer> getRunningTimers()
    {
        return runningTimers.values();
    }

    /**
     * Pauses (or resumes) all the running timers.
     *
     * @param paused If true, all the timers will be paused. Else, resumed.
     */
    public void pauseAll(boolean paused)
    {
        for (UHTimer timer : getRunningTimers())
        {
            timer.setPaused(paused);
        }

        if (!paused)
        {
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
    public void pauseAllRunning(boolean paused)
    {
        if (paused)
        {
            for (UHTimer timer : getRunningTimers())
            {
                if (!timer.isPaused())
                {
                    timer.setPaused(true);
                    timersToResume.add(timer);
                }
            }
        }
        else
        {
            for (UHTimer timer : timersToResume)
            {
                timer.setPaused(false);
            }

            timersToResume.clear();
        }
    }
}
