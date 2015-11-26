/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.UHCReloaded.events;

/**
 * Called when an episode changes.
 */
public class UHEpisodeChangedEvent extends UHEvent
{

    private int newEpisode;
    private EpisodeChangedCause cause;
    private String shifter;

    public UHEpisodeChangedEvent(int newEpisode, EpisodeChangedCause cause, String shifter)
    {
        this.newEpisode = newEpisode;
        this.cause = cause;
        this.shifter = shifter;
    }

    /**
     * Returns the new episode.
     *
     * @return The new episode.
     */
    public int getNewEpisode()
    {
        return newEpisode;
    }

    /**
     * Why the episode changed?
     *
     * @return The cause.
     *
     * @see EpisodeChangedCause
     */
    public EpisodeChangedCause getCause()
    {
        return cause;
    }

    /**
     * Returns the name of the shifter (the one that executed the /uh shift command, or "" if
     * the episode was shifted because the previous one was finished).
     *
     * @return The shifter.
     */
    public String getShifter()
    {
        return shifter;
    }
}
