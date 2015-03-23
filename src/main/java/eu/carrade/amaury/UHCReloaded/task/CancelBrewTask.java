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

package eu.carrade.amaury.UHCReloaded.task;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CancelBrewTask extends BukkitRunnable {

	private BrewerInventory inventory = null;
	private HumanEntity whoClicked = null;
	
	public CancelBrewTask(BrewerInventory inventory, HumanEntity whoClicked) {
		this.inventory = inventory;
		this.whoClicked = whoClicked;
	}
	
	@Override
	public void run() {
		if(inventory.getIngredient() == null) {
			return; // Nothing to do!
		}
		
		if(whoClicked instanceof Player) {
			ItemStack ingredient = inventory.getIngredient();
			
			if(ingredient.getType() != null && ingredient.getType().equals(Material.GLOWSTONE_DUST)) {
				inventory.setIngredient(new ItemStack(Material.AIR)); // The glowstone is removed.
				
				// First try: try to add the glowstone to an existing stack
				Boolean added = false;
				for(ItemStack item : whoClicked.getInventory().getContents()) {
					if(item != null && item.getType() != null && item.getType().equals(Material.GLOWSTONE_DUST)) {
						if(item.getAmount() + ingredient.getAmount() <= item.getMaxStackSize()) {
							// We can add the glowstone here.
							item.setAmount(item.getAmount() + ingredient.getAmount());
							added = true;
							break;
						}
					}
				}
				
				if(!added) {
					// Failed... We adds the glowstone to the first empty slot found.
					
					Integer slotEmpty = whoClicked.getInventory().firstEmpty();
					
					if(slotEmpty != -1) { // -1 is returned if there isn't any empty slot
						whoClicked.getInventory().setItem(slotEmpty, ingredient);
					}
					else {
						// Failed again (!). Maybe an item captured between the click and this execution.
						// The stack is dropped at the player's location.
						whoClicked.getWorld().dropItem(whoClicked.getLocation(), ingredient);
					}
				}
				
				((Player) whoClicked).updateInventory();
			}
		}
	}

}
