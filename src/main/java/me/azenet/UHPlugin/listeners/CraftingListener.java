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

package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.misc.ProTipsSender;
import me.azenet.UHPlugin.recipes.RecipesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class CraftingListener implements Listener {
	private UHPlugin p = null;
	private I18n i = null;
	
	public CraftingListener(UHPlugin p) {
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
		
		if(!p.getRecipesManager().isRecipeAllowed(recipe)) {
			ev.getInventory().setResult(new ItemStack(Material.AIR));
			
			// ProTips
			final String failedRecipe = p.getRecipesManager().getLastFailedRecipe();
			final Player player = (Player) ev.getViewers().get(0); // crafting inventory: only one viewer in all cases.
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					switch(failedRecipe) {
						case RecipesManager.RECIPE_COMPASS:
							switch(p.getRecipesManager().getCompassRecipeType()) {
								case RecipesManager.COMPASS_EASY:
									p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_CRAFT_COMPASS_EASY);
									break;
								case RecipesManager.COMPASS_MEDIUM:
									p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_CRAFT_COMPASS_MEDIUM);
									break;
								case RecipesManager.COMPASS_HARD:
									p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_CRAFT_COMPASS_HARD);
									break;
							}
							
							break;
						
						case RecipesManager.RECIPE_GLISTERING_MELON:
							p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_CRAFT_GLISTERING_MELON);
							break;
						
						case RecipesManager.RECIPE_ENCHANTED_GOLDEN_APPLE:
							p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_CRAFT_NO_ENCHANTED_GOLDEN_APPLE);
							break;
					}
				}
			}, 40L);
			
			return;
		}
		
		
		/** Adds a lore to the golden apples crafted from a head **/
		
		ItemStack loreResult = p.getRecipesManager().addLore(recipe, ev.getInventory());
		if(loreResult != null) {
			ev.getInventory().setResult(loreResult);
			return;
		}
		
		
		/** The lore remover don't change the name of the item **/
		
		ItemStack keepNameResult = p.getRecipesManager().keepNameOnLoreRemover(recipe, ev.getInventory());
		if(keepNameResult != null) {
			ev.getInventory().setResult(keepNameResult);
			return;
		}
	}
	
	
	/**
	 *   - Workaround to fix the crafting grid being not updated when the item is taken
	 *     from the grid.
	 *     <p>
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
			
			/** Workaround to fix the crafting grid being not updated when the item is taken
			    from the grid. **/
			if(inventory instanceof CraftingInventory && ev.getSlotType() == SlotType.RESULT) {
				p.getServer().getScheduler().runTaskLater(p, new BukkitRunnable() {

					@Override
					public void run() {
						for(HumanEntity viewer : ev.getViewers()) {
							if(viewer instanceof Player) {
								((Player) viewer).updateInventory();
							}
						}
					}
				}, 1L);
			}
			
			
			/** Allows any shape for the loots in the compass recipe. **/
			
			if(inventory instanceof CraftingInventory) {
				
				// This is ran one tick after the click because when the event is fired, the inventory 
				// object is not updated, and so the result of the isValidCompassResult is invalid.
				
				Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
					@Override
					public void run() {
						if(p.getRecipesManager().isValidCompassRecipe(((CraftingInventory) inventory).getMatrix())) {
							
							// Puts the compass in the result slot
							if(ev.getSlotType() == SlotType.CRAFTING) {
								((CraftingInventory) inventory).setResult(new ItemStack(Material.COMPASS));
								ev.setResult(Result.ALLOW);
								
								((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
							}
							
							// Consumes the materials in the crafting grid.
							// Because this is not an "official" recipe, we need to do that manually.
							else if(ev.getSlotType() == SlotType.RESULT) {
								int index = 1;
								for(ItemStack stack : ((CraftingInventory) inventory).getMatrix()) {
									if(stack == null) continue;
									
									if(stack.getAmount() != 1) {
										stack.setAmount(stack.getAmount() - 1);
										inventory.setItem(index, stack);
									}
									else {
										inventory.setItem(index, new ItemStack(Material.AIR));
									}
									
									index++;
								}
								
								ev.setCurrentItem(new ItemStack(Material.COMPASS));
								ev.setResult(Result.ALLOW);
								
								((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
							}
							
							return;
						}
					}
					
				}, 1L);
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
							
							HashSet<String> prohibited = new HashSet<String>();
							
							prohibited.add(ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNormal"));
							prohibited.add(ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNotch"));
							
							// It is possible that the client filters the name of the golden apple in the anvil UI,
							// removing all §.
							for(String prohibition : new HashSet<String>(prohibited)) {
								prohibited.add(prohibition.replace("§", ""));
							}
							
							
							// An item can't be renamed to the name of a golden head
							if(meta != null && meta.hasDisplayName()) {
								if(prohibited.contains(meta.getDisplayName())) {
									ev.setCancelled(true); // nope nope nope
								}
							}
							
							// A golden head can't be renamed to any other name
							if(view.getItem(0) != null) { // slot 0 = first slot
								ItemMeta metaOriginal = view.getItem(0).getItemMeta();
								
								if(metaOriginal != null && metaOriginal.hasDisplayName()) {
									if(prohibited.contains(metaOriginal.getDisplayName())) {
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
					if(p.getRecipesManager().isValidCompassRecipe(((CraftingInventory) ev.getInventory()).getMatrix())) {
						((CraftingInventory) ev.getInventory()).setResult(new ItemStack(Material.COMPASS));
						((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
					}					
				}
			}, 1L);
		}
	}
}
