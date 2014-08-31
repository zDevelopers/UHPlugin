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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UHTeamChatManager {
	
	UHPlugin p = null;
	I18n i = null;
	
	List<UUID> teamChatLocked = new ArrayList<UUID>();
	
	public UHTeamChatManager(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	/**
	 * Sends a team-message from the given player.
	 * 
	 * @param sender The sender of this message.
	 * @param message The message to be sent.
	 */
	public void sendTeamMessage(final Player sender, String message) {
		UHTeam team = p.getTeamManager().getTeamForPlayer((Player) sender);
		
		if(team == null) {
			sender.sendMessage(i.t("team.message.noTeam"));
			return;
		}
		
		for(final Player player : team.getPlayers()) {
			player.sendMessage(i.t("team.message.format", ((Player) player).getDisplayName(), message));
		}
		
		if(!p.getProtipsSender().wasProtipSent(sender, UHProTipsSender.PROTIP_LOCK_CHAT)) {
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					p.getProtipsSender().sendProtip(sender, UHProTipsSender.PROTIP_LOCK_CHAT);
				}
			}, 30L);
		}
	}
	
	/**
	 * Sends a global message from the given player.
	 * 
	 * @param sender The sender of this message.
	 * @param message The message to be sent.
	 */
	public void sendGlobalMessage(Player sender, String message) {
		sender.chat(message);
	}
	
	/**
	 * Toggles the chat between the global chat and the team chat.
	 * 
	 * @param player The chat of this player will be toggled.
	 * @return true if the chat is now the team chat; false else.
	 */
	public boolean toggleChatForPlayer(final Player player) {
		if(teamChatLocked.contains(player.getUniqueId())) {
			teamChatLocked.remove(player.getUniqueId());			
			return false;
		}
		else {
			teamChatLocked.add(player.getUniqueId());
			
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_USE_G_COMMAND);
				}
			}, 10L);
			
			return true;
		}
	}
	
	/**
	 * Returns true if the team chat is enabled for the given player.
	 * 
	 * @param player The player.
	 * @return
	 */
	public boolean isTeamChatEnabled(Player player) {
		return teamChatLocked.contains(player.getUniqueId());
	}
}
