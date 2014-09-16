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

package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHProTipsSender;
import me.azenet.UHPlugin.UHRecipeManager;
import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class UHCraftingListener implements Listener {
	private UHPlugin p = null;
	private I18n i = null;
	
	public UHCraftingListener(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	
	/**
	 * Used to:
	 *  - prevent items to be crafted;
	 *  - send a ProTip containing the craft to use, if an error occurred;
	 *  - add a lure to the golden apples crafted from a head;
	 *  - keep the name of the item when the anti-lore craft is used.
	 *  
	 * @param ev
	 */
	@EventHandler
	public void onPreCraftEvent(PrepareItemCraftEvent ev) {
		Recipe recipe = ev.getRecipe();
		
		if(recipe == null) {
			return;
		}
		
		/** Prevents items to be crafted **/
		
		if(!p.getRecipeManager().isRecipeAllowed(recipe)) {
			ev.getInventory().setResult(new ItemStack(Material.AIR));
			
			// ProTips
			final String failedRecipe = p.getRecipeManager().getLastFailedRecipe();
			final Player player = (Player) ev.getViewers().get(0); // crafting inventory: only one viewer in all cases.
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					switch(failedRecipe) {
						case UHRecipeManager.RECIPE_COMPASS:
							switch(p.getRecipeManager().getCompassRecipeType()) {
								case UHRecipeManager.COMPASS_EASY:
									p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_CRAFT_COMPASS_EASY);
									break;
								case UHRecipeManager.COMPASS_MEDIUM:
									p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_CRAFT_COMPASS_MEDIUM);
									break;
								case UHRecipeManager.COMPASS_HARD:
									p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_CRAFT_COMPASS_HARD);
									break;
							}
							
							break;
						
						case UHRecipeManager.RECIPE_GLISTERING_MELON:
							p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_CRAFT_GLISTERING_MELON);
							break;
						
						case UHRecipeManager.RECIPE_ENCHANTED_GOLDEN_APPLE:
							p.getProtipsSender().sendProtip(player, UHProTipsSender.PROTIP_CRAFT_NO_ENCHANTED_GOLDEN_APPLE);
							break;
					}
				}
			}, 40L);
			
			return;
		}
		
		
		/** Adds a lore to the golden apples crafted from a head **/
		
		ItemStack loreResult = p.getRecipeManager().addLore(recipe, ev.getInventory());
		if(loreResult != null) {
			ev.getInventory().setResult(loreResult);
			return;
		}
		
		
		/** The lore remover don't change the name of the item **/
		
		ItemStack keepNameResult = p.getRecipeManager().keepNameOnLoreRemover(recipe, ev.getInventory());
		if(keepNameResult != null) {
			ev.getInventory().setResult(keepNameResult);
			return;
		}
	}
	
	
	/**
	 *   - Prevents an apple to be renamed to/from the name of an head apple.
	 *    
	 *     (In vanilla clients, it is not possible to rename an apple to that name because of the
	 *     ChatColor.RESET before, but some modded clients allows the player to write §r.)
	 *    
	 *     (Thanks to Zelnehlun on BukkitDev.)
	 *     <p>
	 *   - Crafts the special compass (“semi-shapeless” recipe).
	 * 
	 * @param ev
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent ev) {
		if(ev.getWhoClicked() instanceof Player) { // Just in case
			final Inventory inventory = ev.getInventory();
			
			/** Allows any shape for the loots in the compass recipe. **/
			
			if(inventory instanceof CraftingInventory) {				
				Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
					@Override
					public void run() {
						if(p.getRecipeManager().isValidCompassRecipe(((CraftingInventory) inventory).getMatrix())) {
							((CraftingInventory) inventory).setResult(new ItemStack(Material.COMPASS));
							((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
						}					
					}
				}, 1L);
				
				return;
			}
			
			
			/** Prevent an apple to be renamed to/from the name of an head apple. **/
			
			else if(inventory instanceof AnvilInventory) {
				InventoryView view = ev.getView();
				int rawSlot = ev.getRawSlot();
				
				if(rawSlot == view.convertSlot(rawSlot)) { // ensure we are talking about the upper inventory
					if(rawSlot == 2) { // "result" slot
						ItemStack item = ev.getCurrentItem();
						if(item != null) { // result slot non empty
							ItemMeta meta = item.getItemMeta();
							
							String prohibedNameNormal = ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNormal");
							String prohibedNameNotch  = ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNotch");
							
							// It is possible that the client filters the name of the golden apple in the anvil UI,
							// removing all §.
							String filteredProhibedNameNormal = prohibedNameNormal.replace("§", "");
							String filteredProhibedNameNotch  = prohibedNameNotch.replace("§", "");
							
							
							// An item can't be renamed to the name of a golden head
							if(meta != null && meta.hasDisplayName()) {
								if(meta.getDisplayName().equals(prohibedNameNormal)
										|| meta.getDisplayName().equals(prohibedNameNotch)
										|| meta.getDisplayName().equals(filteredProhibedNameNormal)
										|| meta.getDisplayName().equals(filteredProhibedNameNotch)) {
									
									ev.setCancelled(true); // nope nope nope
									
								}
							}
							
							// A golden head can't be renamed to any other name
							if(view.getItem(0) != null) { // slot 0 = first slot
								ItemMeta metaOriginal = view.getItem(0).getItemMeta();
								
								if(metaOriginal != null && metaOriginal.hasDisplayName()) {
									if(metaOriginal.getDisplayName().equals(prohibedNameNormal)
											|| metaOriginal.getDisplayName().equals(prohibedNameNotch)
											|| metaOriginal.getDisplayName().equals(filteredProhibedNameNormal)
											|| metaOriginal.getDisplayName().equals(filteredProhibedNameNotch)) {
										
										ev.setCancelled(true);
										
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Used to craft the special compass (“semi-shapeless” recipe).
	 * 
	 * @param ev
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(final InventoryDragEvent ev) {		
		if(ev.getInventory() instanceof CraftingInventory) {				
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					if(p.getRecipeManager().isValidCompassRecipe(((CraftingInventory) ev.getInventory()).getMatrix())) {
						((CraftingInventory) ev.getInventory()).setResult(new ItemStack(Material.COMPASS));
						((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
					}					
				}
			}, 1L);
		}
	}
}
