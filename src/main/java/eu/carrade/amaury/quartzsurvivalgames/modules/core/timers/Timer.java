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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.timers;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.events.TimerEndsEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.events.TimerStartsEvent;
import fr.zcraft.quartzlib.components.i18n.I;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;


/**
 * Represents a timer.
 *
 * @author Amaury Carrade
 */
public class Timer
{
    private static final NumberFormat formatter = new DecimalFormat("00");

    private UUID id;
    private String name;
    private Boolean registered = false;
    private Boolean running = false;
    private Boolean displayed = false;
    private Boolean nameDisplayed = true;

    private Long startTime = 0L;
    private Integer duration = 0; // seconds

    // Pause
    private Boolean paused = false;
    private Long pauseTime = 0L;
    private Integer elapsedWhenPaused = 0;

    private Boolean system = false;


    public Timer(String name)
    {
        Validate.notNull(name, "The name cannot be null");

        this.id = UUID.randomUUID(); // only used as a hashCode.
        this.name = name;
    }

    public Timer(final String name, final int seconds)
    {
        this(name);
        setDuration(seconds);
    }

    public Timer(final String name, final TimeDelta duration)
    {
        this(name, Math.toIntExact(duration.getSeconds()));
    }

    /**
     * Sets the duration of the timer, in seconds.
     *
     * @param seconds The duration.
     */
    public void setDuration(int seconds)
    {
        this.duration = seconds;
    }

    /**
     * Sets the duration of the timer.
     *
     * @param duration The duration.
     */
    public void setDuration(TimeDelta duration)
    {
        this.duration = Math.toIntExact(duration.getSeconds());
    }

    /**
     * Starts this timer.
     *
     * If this is called while the timer is running, the timer is restarted.
     */
    public void start()
    {
        this.running = true;
        this.startTime = System.currentTimeMillis();

        Bukkit.getServer().getPluginManager().callEvent(new TimerStartsEvent(this));
    }

    /**
     * Stops this timer.
     */
    public void stop()
    {
        stop(false);
    }

    /**
     * Stops this timer.
     *
     * @param wasUp If true, the timer was stopped because the timer was up.
     */
    private void stop(boolean wasUp)
    {
        final TimerEndsEvent event = new TimerEndsEvent(this, wasUp);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (isRegistered())
        {
            if (event.getRestart())
            {
                start();
            }
            else
            {
                running = false;
                startTime = 0L;
            }
        }
    }

    /**
     * Updates the timer to check if it is up and terminate it.
     */
    public void update()
    {
        if (running && !paused && getElapsed() >= getDuration())
        {
            stop(true);
        }
    }

    /**
     * Pauses (or restarts after a pause) the timer.
     * <p>
     * If the timer is not running, nothing is done.
     *
     * @param pause If true the timer will be paused.
     */
    public void setPaused(boolean pause)
    {
        if (running)
        {
            // The pause is only set once (as example if the user executes /uh freeze all twice).
            if (pause != paused)
            {
                if (pause)
                {
                    elapsedWhenPaused = getElapsed();

                    paused = true;
                    pauseTime = System.currentTimeMillis();
                }

                else
                {
                    // We have to add to the time of the start of the episode the elapsed time
                    // during the pause.
                    startTime += (System.currentTimeMillis() - pauseTime);
                    pauseTime = 0L;

                    paused = false;
                    elapsedWhenPaused = 0;
                }
            }
        }
    }

    /**
     * Checks if the timer is registered in the TimersModule.
     *
     * @return true if the timer is registered.
     */
    public Boolean isRegistered()
    {
        return registered;
    }

    /**
     * Marks a timer as registered, or not.
     *
     * @param registered true if the timer is now registered.
     */
    protected void setRegistered(Boolean registered)
    {
        this.registered = registered;
    }

    /**
     * Returns the name of the timer.
     *
     * @return The name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the display name of the timer.
     * <p>
     * The display name is the name with all &-based color codes replaced by §-based ones.
     *
     * @return The name.
     */
    public String getDisplayName()
    {
        return ChatColor.translateAlternateColorCodes('&', name);
    }


    /**
     * Checks if the timer is currently running.
     *
     * @return true if the timer is running.
     */
    public Boolean isRunning()
    {
        return running;
    }

    /**
     * Checks if the timer is currently displayed in the sidebar.
     *
     * @return {@code true} if displayed.
     */
    public Boolean isDisplayed()
    {
        return displayed;
    }

    /**
     * Display or hide this timer in/from the sidebar.
     *
     * @param displayed {@code true} to display, and {@code false} to hide.
     */
    public void setDisplayed(Boolean displayed)
    {
        this.displayed = displayed;
    }

    /**
     * Checks if the name of this timer should be displayed in the sidebar.
     *
     * @return {@code true} if name displayed.
     */
    public Boolean isNameDisplayed()
    {
        return nameDisplayed;
    }

    /**
     * Displays or hides the timer's name in/from the sidebar.
     *
     * @param nameDisplayed {@code true} to display, and {@code false} to hide.
     */
    public void setNameDisplayed(Boolean nameDisplayed)
    {
        this.nameDisplayed = nameDisplayed;
    }

    /**
     * @return {@code true} if this is a system timer that cannot be altered by the user.
     */
    public Boolean isSystem()
    {
        return system;
    }

    /**
     * Sets if this timer is a system timer that cannot be altered by the user.
     *
     * @param system {@code true} if this timer is an internal system timer.
     */
    public void setSystem(Boolean system)
    {
        this.system = system;
    }

    /**
     * Returns the duration of the timer, in seconds.
     *
     * @return The duration.
     */
    public Integer getDuration()
    {
        return duration;
    }

    /**
     * @return The elapsed time since the beginning of the timer (not including pauses), in seconds.
     */
    public int getElapsed()
    {
        if (isRunning())
        {
            if (isPaused())
                return elapsedWhenPaused;
            else
                return (int) Math.floor((System.currentTimeMillis() - startTime) / 1000);
        }

        else return 0;
    }

    /**
     * @return The number of seconds left before this timer is up.
     */
    public int getTimeLeft()
    {
        return getDuration() - getElapsed();
    }

    /**
     * Checks if this timer is paused.
     *
     * @return true if the timer is paused.
     */
    public Boolean isPaused()
    {
        return paused;
    }

    @Override
    public String toString()
    {
        return formatTime(getTimeLeft());
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof Timer && ((Timer) other).getName().equals(this.getName());
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Formats a given number of seconds into [hh:]mm:ss.
     *
     * @param seconds The number of seconds.
     * @return The formatted time (includes color codes).
     */
    public static String formatTime(final int seconds)
    {
        final int secondsLeft = seconds % 60;
        final int minutesLeft = (seconds % 3600) / 60;
        final int hoursLeft = (int) Math.floor(seconds / 3600);

        if (hoursLeft != 0)
        {
            /// Timer. {0} = hours; {1} = minutes; {2} = seconds.
            return ChatColor.WHITE + I.t("{0}{gray}:{white}{1}{gray}:{white}{2}", formatter.format(hoursLeft), formatter.format(minutesLeft), formatter.format(secondsLeft));
        }
        else
        {
            /// Timer. {0} = minutes; {1} = seconds.
            return ChatColor.WHITE +I.t("{white}{0}{gray}:{white}{1}", formatter.format(minutesLeft), formatter.format(secondsLeft));
        }
    }
}
