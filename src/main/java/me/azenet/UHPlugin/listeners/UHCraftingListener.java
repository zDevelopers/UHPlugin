package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

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
		}
		
		
		/** Adds a lore to the golden apples crafted from a head **/
		
		ItemStack loreResult = p.getRecipeManager().addLore(recipe, ev.getInventory());
		if(loreResult != null) {
			ev.getInventory().setResult(loreResult);
			return;
		}
		
		
		/** The lore removed don't change the name of the item **/
		
		ItemStack keepNameResult = p.getRecipeManager().keepNameOnLoreRemover(recipe, ev.getInventory());
		if(keepNameResult != null) {
			ev.getInventory().setResult(keepNameResult);
			return;
		}
	}
	
	
	/**
	 * Used to prevent an apple to be renamed to/from the name of an head apple.
	 *    
	 * (In vanilla clients, it is not possible to rename an apple to that name because of the
	 * ChatColor.RESET before, but some modded clients allows the player to write §r.)
	 *    
	 * (Thanks to Zelnehlun on BukkitDev.)
	 * 
	 * @param ev
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent ev) {
		if(ev.getWhoClicked() instanceof Player) { // Just in case
			Inventory inventory = ev.getInventory();
			
			if(inventory instanceof AnvilInventory) {
				InventoryView view = ev.getView();
				int rawSlot = ev.getRawSlot();
				
				if(rawSlot == view.convertSlot(rawSlot)) { // ensure we are talking about the upper inventory
					if(rawSlot == 2) { // "result" slot
						ItemStack item = ev.getCurrentItem();
						if(item != null) { // result slot non empty
							ItemMeta meta = item.getItemMeta();
							
							String prohibedNameNormal = ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNormal");
							String prohibedNameNotch  = ChatColor.RESET + i.t("craft.goldenApple.nameGoldenAppleFromHeadNotch");
							
							// It is possible that the client filter the name of the golden apple in the anvil UI,
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
}
