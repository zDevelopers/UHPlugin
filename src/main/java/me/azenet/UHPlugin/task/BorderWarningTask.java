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

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.borders.MapShape;
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderWarningTask extends BukkitRunnable {

	private UHPlugin p = null;
	private I18n i = null;
	
	public BorderWarningTask(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	@Override
	public void run() {
		
		if(p.getFreezer().getGlobalFreezeState()) {
			return; // No messages are sent if the game is frozen.
		}
		
		// Message sent to all players outside the border
		for(Player player : p.getBorderManager().getPlayersOutside(p.getBorderManager().getWarningSize())) {
			double distance = p.getBorderManager().getDistanceToBorder(player.getLocation(), p.getBorderManager().getWarningSize());
			
			if(p.getBorderManager().getMapShape() == MapShape.CIRCULAR) {
				player.sendMessage(i.t("borders.warning.messageCircular", String.valueOf(p.getBorderManager().getWarningSize())));
			}
			else {
				player.sendMessage(i.t("borders.warning.messageSquared", String.valueOf(p.getBorderManager().getWarningSize())));
			}
			
			player.sendMessage(i.t("borders.warning.messageDistance", String.valueOf(distance)));
		}
	}
}
