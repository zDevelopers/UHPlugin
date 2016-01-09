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

package eu.carrade.amaury.UHCReloaded.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;


/**
 * Represents a sound, with volume and pitch.
 *
 * @author Amaury Carrade
 */
public class UHSound
{

    private Sound sound = null;
    private Float volume = 1f;
    private Float pitch = 1f;

    /**
     * Constructs a sound with volume = 1f and pitch = 1f.
     *
     * @param sound The sound.
     */
    public UHSound(Sound sound)
    {
        this.sound = sound;
    }

    public UHSound(Sound sound, Float volume, Float pitch)
    {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    /**
     * Constructs a sound from a configuration section.
     * <p>
     * Format:
     * <pre>
     * key:
     *     name: string parsable as a sound. If not parsable, null used (i.e. no sound played).
     *     volume: decimal number
     *     pitch: decimal number
     * </pre>
     *
     * @param config The configuration section.
     */
    public UHSound(ConfigurationSection config)
    {
        if (config == null)
        {
            return;
        }

        this.sound = string2Sound(config.getString("name"));
        this.volume = (float) config.getDouble("volume");
        this.pitch = (float) config.getDouble("pitch");
    }

    /**
     * Plays the sound for the specified player.
     * <p>
     * The sound is played at the current location of the player.
     * <p>
     * If the sound is null, fails silently.
     *
     * @param player The player.
     */
    public void play(Player player)
    {
        play(player, player.getLocation());
    }

    /**
     * Plays the sound for the specified player, at the specified location.
     * <p>
     * If the sound is null, fails silently.
     *
     * @param player The player.
     * @param location The location of the sound.
     */
    public void play(Player player, Location location)
    {
        player.playSound(location, sound, volume, pitch);
    }

    /**
     * Plays this sound for all players, at the current location of the players.
     */
    public void broadcast()
    {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
        {
            play(player);
        }
    }

    public Sound getSound()
    {
        return sound;
    }

    public void setSound(Sound sound)
    {
        this.sound = sound;
    }

    public Float getVolume()
    {
        return volume;
    }

    public void setVolume(Float volume)
    {
        this.volume = volume;
    }

    public Float getPitch()
    {
        return pitch;
    }

    public void setPitch(Float pitch)
    {
        this.pitch = pitch;
    }

    @Override
    public String toString()
    {
        return "UHSound [sound=" + sound + ", volume=" + volume + ", pitch=" + pitch + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pitch == null) ? 0 : pitch.hashCode());
        result = prime * result + ((sound == null) ? 0 : sound.hashCode());
        result = prime * result + ((volume == null) ? 0 : volume.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof UHSound))
        {
            return false;
        }
        UHSound other = (UHSound) obj;
        if (pitch == null)
        {
            if (other.pitch != null)
            {
                return false;
            }
        }
        else if (!pitch.equals(other.pitch))
        {
            return false;
        }
        if (sound != other.sound)
        {
            return false;
        }
        if (volume == null)
        {
            if (other.volume != null)
            {
                return false;
            }
        }
        else if (!volume.equals(other.volume))
        {
            return false;
        }
        return true;
    }


    /**
     * Converts a string to a Sound.
     * <p>
     * "<code>ANVIL_LAND</code>", "<code>Anvil Land</code>" and "<code>ANVIL Land</code>" are recognized as
     * <code>Sound.ANVIL_LAND</code>, as example.
     *
     * @param soundName The text to be converted.
     * @return The corresponding Sound, or null if there isn't any match.
     */
    public static Sound string2Sound(String soundName)
    {
        if (soundName != null)
        {
            soundName = soundName.trim().toUpperCase().replace(" ", "_");
            try
            {
                return Sound.valueOf(soundName);
            }
            catch (IllegalArgumentException e)
            {
                // Non-existent sound
                return null;
            }
        }

        return null;
    }
}
