package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class UHFreezerListener implements Listener {
	
	private UHPlugin p = null;
	
	public UHFreezerListener(UHPlugin p) {
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
	 * Used to prevent items to despawn if the game is freezed.
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
