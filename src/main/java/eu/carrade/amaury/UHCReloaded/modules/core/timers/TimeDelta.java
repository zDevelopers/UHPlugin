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
package eu.carrade.amaury.UHCReloaded.modules.core.timers;

import fr.zcraft.zlib.components.configuration.ConfigurationParseException;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandler;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;


public class TimeDelta
{
    private static final NumberFormat formatter = new DecimalFormat("00");

    private final long seconds;

    static
    {
        ConfigurationValueHandlers.registerHandlers(TimeDelta.class);
    }

    /**
     * Constructs a time delta.
     *
     * @param seconds The number of seconds in this delta.
     */
    public TimeDelta(long seconds)
    {
        this.seconds = seconds;
    }

    /**
     * Constructs a time delta.
     *
     * @param hours The number of hours.
     * @param minutes The number of minutes.
     * @param seconds The number of seconds.
     */
    public TimeDelta(long hours, long minutes, long seconds)
    {
        this(seconds + minutes * 60 + hours * 3600);
    }

    /**
     * Constructs a time delta from a raw string representing a time.
     *
     * <p>Allowed formats:
     *
     * <ul>
     *   <li><tt>mm</tt> – number of minutes;</li>
     *   <li><tt>mm:ss</tt> – minutes and seconds;</li>
     *   <li><tt>hh:mm:ss</tt> – hours, minutes and seconds.</li>
     * </ul>
     *
     * @param rawTime The raw time text.
     *
     * @throws IllegalArgumentException if the text is not formatted as above.
     * @throws NumberFormatException if the text between the colons cannot be
     * converted in integers.
     */
    public TimeDelta(final String rawTime) throws IllegalArgumentException, NumberFormatException
    {
        final String[] split = rawTime.split(":");

        if (rawTime.isEmpty() || split.length > 3)
        {
            throw new IllegalArgumentException("Badly formatted string in TimeDelta(String); formats allowed are mm, mm:ss or hh:mm:ss.");
        }

        if (split.length == 1)  // "mm"
        {
            this.seconds = Integer.valueOf(split[0]) * 60;
        }
        else if (split.length == 2)  // "mm:ss"
        {
            this.seconds = Integer.valueOf(split[0]) * 60 + Integer.valueOf(split[1]);
        }
        else  // "hh:mm:ss"
        {
            this.seconds = Integer.valueOf(split[0]) * 3600 + Integer.valueOf(split[1]) * 60 + Integer.valueOf(split[2]);
        }
    }

    /**
     * @return The total number of seconds in this time delta.
     */
    public long getSeconds()
    {
        return seconds;
    }

    /**
     * @param other Another {@link TimeDelta}.
     * @return {@code true} if the duration in this {@link TimeDelta} is less than
     * (or equal to) the duration in the other one.
     */
    public boolean lessThan(final TimeDelta other)
    {
        return this.getSeconds() <= other.getSeconds();
    }

    /**
     * @param other Another {@link TimeDelta}.
     * @return {@code true} if the duration in this {@link TimeDelta} is greater
     * than (or equal to) the duration in the other one.
     */
    public boolean greaterThan(final TimeDelta other)
    {
        return this.getSeconds() >= other.getSeconds();
    }

    /**
     * @param other Another {@link TimeDelta}
     * @return A new {@link TimeDelta} instance representing the sum of the two
     * {@link TimeDelta deltas}.
     */
    public TimeDelta add(final TimeDelta other)
    {
        return new TimeDelta(this.getSeconds() + other.getSeconds());
    }

    /**
     * @param other Another {@link TimeDelta}
     * @return A new {@link TimeDelta} instance representing the subtraction of
     * the two {@link TimeDelta deltas}.
     */
    public TimeDelta subtract(final TimeDelta other)
    {
        return new TimeDelta(this.getSeconds() - other.getSeconds());
    }

    /**
     * @param factor A factor.
     * @return A new {@link TimeDelta} instance representing the duration of this
     * {@link TimeDelta delta} multiplied by the given factor.
     */
    public TimeDelta multiply(final long factor)
    {
        return new TimeDelta(this.getSeconds() * factor);
    }

    /**
     * @param factor A factor.
     * @return A new {@link TimeDelta} instance representing the duration of this
     * {@link TimeDelta delta} divided by the given factor.
     */
    public TimeDelta divide(final long factor)
    {
        return new TimeDelta(this.getSeconds() / factor);
    }

    /**
     * @return A string representation using the format “mm:ss” or “hh:mm:ss” if
     * longer than one hour.
     */
    @Override
    public String toString()
    {
        final long secondsLeft = seconds % 60;
        final long minutesLeft = (seconds % 3600) / 60;
        final long hoursLeft = (long) Math.floor(seconds / 3600.0);

        if (hoursLeft != 0)
        {
            return formatter.format(hoursLeft) + ":" + formatter.format(minutesLeft) + ":" + formatter.format(secondsLeft);
        }
        else
        {
            return formatter.format(minutesLeft) + ":" + formatter.format(secondsLeft);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o != null && getClass() == o.getClass() && seconds == ((TimeDelta) o).seconds;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(seconds);
    }


    @ConfigurationValueHandler
    public static TimeDelta handleTimeDelta(String rawDelta) throws ConfigurationParseException
    {
        try
        {
            return new TimeDelta(rawDelta);
        }
        catch (IllegalArgumentException e)
        {
            throw new ConfigurationParseException("Invalid time delta format", rawDelta);
        }
    }
}
