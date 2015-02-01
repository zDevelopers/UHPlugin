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

package me.azenet.UHPlugin.task;

import java.util.Random;
import java.util.Set;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHUtils;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FireworksOnWinnersTask extends BukkitRunnable {

	private UHPlugin p = null;
	private Set<OfflinePlayer> winners = null;
	
	private Double areaSize;
	private Random rand;
	
	private long startTime = 0L;
	
	public FireworksOnWinnersTask(UHPlugin p, Set<OfflinePlayer> listWinners) {
		this.p = p;
		this.winners = listWinners;
		
		this.areaSize = p.getConfig().getDouble("finish.fireworks.areaSize");
		this.rand = new Random();
		
		this.startTime = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		// The fireworks are launched in a square centered on the player.
		Double halfAreaSize = areaSize / 2;
		
		for(OfflinePlayer winner : winners) {
			if(winner.isOnline()) {
				Location fireworkLocation = ((Player) winner).getLocation();
				
				fireworkLocation.add(rand.nextDouble() * areaSize - halfAreaSize, // a number between -halfAreaSize and halfAreaSize 
						2, // y+2 for a clean vision of the winner.
						rand.nextDouble() * areaSize - halfAreaSize);
				
				UHUtils.generateRandomFirework(fireworkLocation, 0, 15);
			}
		}
		
		if((System.currentTimeMillis() - startTime) / 1000 > p.getConfig().getInt("finish.fireworks.duration", 10)) {
			this.cancel();
		}
	}
}
