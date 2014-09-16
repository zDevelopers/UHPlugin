/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

package me.azenet.UHPlugin;

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
public class UHSound {

	private Sound sound = null;
	private Float volume = 1f;
	private Float pitch = 1f;

	/**
	 * Constructs a sound with volume = 1f and pitch = 1f.
	 * 
	 * @param sound The sound.
	 */
	public UHSound(Sound sound) {
		this.sound = sound;
	}

	public UHSound(Sound sound, Float volume, Float pitch) {
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
	public UHSound(ConfigurationSection config) {
		if(config == null) {
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
	public void play(Player player) {
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
	public void play(Player player, Location location) {
		player.playSound(location, sound, volume, pitch);
	}

	/**
	 * Plays this sound for all players, at the current location of the players.
	 */
	public void broadcast() {
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			play(player);
		}
	}

	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public Float getVolume() {
		return volume;
	}

	public void setVolume(Float volume) {
		this.volume = volume;
	}

	public Float getPitch() {
		return pitch;
	}

	public void setPitch(Float pitch) {
		this.pitch = pitch;
	}

	@Override
	public String toString() {
		return "UHSound [sound=" + sound + ", volume=" + volume + ", pitch=" + pitch + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pitch == null) ? 0 : pitch.hashCode());
		result = prime * result + ((sound == null) ? 0 : sound.hashCode());
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UHSound)) {
			return false;
		}
		UHSound other = (UHSound) obj;
		if (pitch == null) {
			if (other.pitch != null) {
				return false;
			}
		} else if (!pitch.equals(other.pitch)) {
			return false;
		}
		if (sound != other.sound) {
			return false;
		}
		if (volume == null) {
			if (other.volume != null) {
				return false;
			}
		} else if (!volume.equals(other.volume)) {
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
	public static Sound string2Sound(String soundName) {
		if(soundName != null) {
			soundName = soundName.trim().toUpperCase().replace(" ", "_");
			try {
				return Sound.valueOf(soundName);
			} catch(IllegalArgumentException e) {
				// Non-existent sound
				return null;
			}
		}
		
		return null;
	}
}
