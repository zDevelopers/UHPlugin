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

	public UHSound(Sound sound) {
		this.sound = sound;
	}

	public UHSound(Sound sound, Float volume, Float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	/**
	 * Play the sound for the specified player.
	 * The sound is played at the current location of the player.
	 * 
	 * @param player The player.
	 */
	public void play(Player player) {
		play(player, player.getLocation());
	}

	/**
	 * Plays the sound for the specified player, at the specified location.
	 * 
	 * @param player The player.
	 * @param location The location of the sound.
	 */
	public void play(Player player, Location location) {
		player.playSound(location, sound, volume, pitch);
	}

	/**
	 * Play this sound for all players, at the current location of the players.
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
}
