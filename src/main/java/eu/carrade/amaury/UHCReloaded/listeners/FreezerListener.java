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

package eu.carrade.amaury.UHCReloaded.listeners;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezerListener implements Listener {
	
	private UHCReloaded p = null;
	
	public FreezerListener(UHCReloaded p) {
		this.p = p;
	}
	
	
	/**
	 * Used to prevent frozen players to break blocks.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockBreakEvent(final BlockBreakEvent ev) {
		if (p.getFreezer().isPlayerFrozen(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to prevent frozen players to place blocks.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockPlaceEvent(final BlockPlaceEvent ev) {
		if (p.getFreezer().isPlayerFrozen(ev.getPlayer())) {
			ev.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to freeze the players.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev) {
		p.getFreezer().freezePlayerIfNeeded(ev.getPlayer(), ev.getFrom(), ev.getTo());
	}
	
	
	/**
	 * Used to prevent the bows to be used while in global freeze mode.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityShoot(EntityShootBowEvent ev) {
		if((ev.getEntity() instanceof Player && p.getFreezer().isPlayerFrozen((Player) ev.getEntity()))
				|| p.getFreezer().getGlobalFreezeState()) {
			
			ev.setCancelled(true);
			
			// If a shoot from a player is cancelled, the arrow seems to be
			// consumed in the player' screen.
			// The inventory needs to be updated for the arrow to "come back".
			if(ev.getEntity() instanceof Player) {
				((Player) ev.getEntity()).updateInventory();
			}
		}
	}
	
	
	/**
	 * Used to prevent items from despawning if the game is frozen.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent ev) {
		if(p.getFreezer().getGlobalFreezeState()) {
			ev.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to freeze the mobs spawning while the game is frozen.
	 * @param ev
	 */
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent ev) {
		if(p.getFreezer().getGlobalFreezeState() && ev.getEntity() instanceof Creature) {
			p.getFreezer().freezeCreature((Creature) ev.getEntity(), true);
		}
	}
	
	
	/**
	 * Used to disable any damages if the player is frozen.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (p.getFreezer().isPlayerFrozen((Player) ev.getEntity())) {
				ev.setCancelled(true);
			}
		}
	}
	
	/**
	 * Used to cancel any food loss (but the players can still eat).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onFoodUpdate(FoodLevelChangeEvent ev) {
		if(p.getFreezer().isPlayerFrozen((Player) ev.getEntity())) {
			if(ev.getFoodLevel() < ((Player) ev.getEntity()).getFoodLevel()) {
				ev.setCancelled(true);
			}
		}
	}
}
