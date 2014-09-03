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

package me.azenet.UHPlugin.task;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTimer;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateTimerTask extends BukkitRunnable {

	private UHPlugin p = null;
	
	public UpdateTimerTask(UHPlugin p) {
		this.p = p;
	}
	
	@Override
	public void run() {
		for(UHTimer timer : p.getTimerManager().getRunningTimers()) {
			timer.update();
		}
	}
}
