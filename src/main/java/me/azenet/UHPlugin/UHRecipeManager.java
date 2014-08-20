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

import java.util.Arrays;
import java.util.List;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class UHRecipeManager {
	
	private UHPlugin p = null;
	private I18n i = null;
	
	public UHRecipeManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
	}
	
	
	/**
	 * Registers the recipes needed, following the configuration.
	 */
	public void registerRecipes() {
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.do")) {
			p.getServer().addRecipe(getGoldenHeadHumanRecipe());
		}
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.do")) {
			p.getServer().addRecipe(getGoldenHeadMonsterRecipe());
		}
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore")) {
			p.getServer().addRecipe(getLoreRemoverNormalRecipe());
			p.getServer().addRecipe(getLoreRemoverNotchRecipe());
		}
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenMelonWithGoldBlock")) {
			p.getServer().addRecipe(getGoldenMelonRecipe());
		}
		
		if (p.getConfig().getBoolean("gameplay-changes.compass")) {
			p.getServer().addRecipe(getCompassRecipe());
		}
			
	}
	
	/**
	 * Checks if a recipe is allowed
	 * 
	 * @param recipe
	 * @return True if the recipe is allowed, false else.
	 */
	public boolean isRecipeAllowed(Recipe recipe) {
		
		// Vanilla compass recipe is disabled if special compass is used.
		if(p.getConfig().getBoolean("gameplay-changes.compass") && RecipeUtil.areSimilar(recipe, getVanillaCompassRecipe())) {
			return false;
		}
		
		// Vanilla golden melon recipe is disabled if the craft with a gold block is enabled.
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenMelonWithGoldBlock") && RecipeUtil.areSimilar(recipe, getVanillaGoldenMelonRecipe())) {
			return false;
		}
		
		// If enchanted golden apples are disabled...
		// The same technique does not work, this is a workaround
		if(p.getConfig().getBoolean("gameplay-changes.goldenApple.disableNotchApples")) {
			if(recipe.getResult().getType() == Material.GOLDEN_APPLE) {
				for(ItemStack item : RecipeUtil.getListOfIngredients(recipe)) {
					if(item.getType() == Material.GOLD_BLOCK) {
						// There is a gold block in a recipe for a golden apple - NOPE NOPE NOPE
						return false;
					}
				}
			}
		}
		
		// The recipe is allowed.
		return true;
	}
	
	/**
	 * Adds the lore to the golden apples, if needed.
	 * 
	 * @param recipe The recipe to change.
	 * @param inventory The crafting inventory (used to access the skull owner)
	 * @return The modified result (ItemStack) if a change was needed. Null if no change is needed.
	 */
	public ItemStack addLore(Recipe recipe, CraftingInventory inventory) {
		if((p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.do") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.do")) 
				&& (p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore")) 
				&& (RecipeUtil.areSimilar(recipe, getGoldenHeadHumanRecipe()) || RecipeUtil.areSimilar(recipe, getGoldenHeadMonsterRecipe()))) {	   	
			
			ItemStack result = recipe.getResult();
			ItemMeta meta = result.getItemMeta();
			
			// Lookup for the head in the recipe
			String name = "";
			Boolean wither = true;
			
			for(ItemStack item : inventory.getContents()) {
				p.getLogger().info(item.toString());
				if(item.getType() == Material.SKULL_ITEM && item.getDurability() == (short) SkullType.PLAYER.ordinal()) { // An human head
					SkullMeta sm = (SkullMeta) item.getItemMeta();
					if(sm.hasOwner()) { // An human head
						name = sm.getOwner();
						wither = false;
					}
					break;
				}
			}
			
			if((wither && p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore"))
					|| (!wither && p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore"))) {
				
				List<String> lore = null;
				if(wither) {
					lore = Arrays.asList(i.t("craft.goldenApple.loreLine1Monster"), i.t("craft.goldenApple.loreLine2Monster"));
				}
				else {
					lore = Arrays.asList(i.t("craft.goldenApple.loreLine1Player", name), i.t("craft.goldenApple.loreLine2Player", name));
				}
				meta.setLore(lore);
			
			}
			
			result.setItemMeta(meta);
			return result;
		}
		
		return null;
	}
	
	/**
	 * Changes the name of the result item of the anti-lore recipe,
	 * to keep the same name than the original.
	 * 
	 * @param recipe The recipe.
	 * @param inventory The crafting inventory. Used to get the name of the item placed in the inventory grid.
	 * @return The ItemStack if a change was needed; null else.
	 */
	public ItemStack keepNameOnLoreRemover(Recipe recipe, CraftingInventory inventory) {
		if((p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore"))
				&& (RecipeUtil.areSimilar(recipe, getLoreRemoverNormalRecipe()) || RecipeUtil.areSimilar(recipe, getLoreRemoverNotchRecipe()))) {
			
			ItemStack original = null;
			for(int slot = 0; slot <= 9; slot++) {
				original = inventory.getMatrix()[slot];
				if(original.getType() != Material.AIR) {
					break; // Found (because there is only one item in the craft).
				}
			}
			
			ItemMeta metaOriginal = original.getItemMeta();
			
			if(metaOriginal != null && metaOriginal.hasDisplayName()) {
				ItemStack result = recipe.getResult();
				ItemMeta metaResult = result.getItemMeta();
				
				metaResult.setDisplayName(metaOriginal.getDisplayName());
				result.setItemMeta(metaResult);
				
				return result;
			}
			
			return null;
		}
		
		return null;
	}
	
	
	
	/**
	 * Returns the recipe that transforms 8 gold ingots and 1 human head into
	 * a golden apple.
	 * 
	 * @return The shaped recipe.
	 */
	public ShapedRecipe getGoldenHeadHumanRecipe() {
		short damage = 0;
		String name = i.t("craft.goldenApple.nameGoldenAppleFromHeadNormal");
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.craftNotchApple")) {
			damage = 1;
			name = i.t("craft.goldenApple.nameGoldenAppleFromHeadNotch");
		}
		
		ItemStack goldenAppleStack = new ItemStack(Material.GOLDEN_APPLE, p.getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.fromHuman.numberCrafted", 1), damage);
		ItemMeta goldenAppleMeta = goldenAppleStack.getItemMeta();
		goldenAppleMeta.setDisplayName(ChatColor.RESET + name);
		goldenAppleStack.setItemMeta(goldenAppleMeta);
		
		ShapedRecipe goldenAppleFromHeadRecipe = new ShapedRecipe(goldenAppleStack);
		
		goldenAppleFromHeadRecipe.shape("GGG", "GHG", "GGG");
		goldenAppleFromHeadRecipe.setIngredient('G', Material.GOLD_INGOT);
		goldenAppleFromHeadRecipe.setIngredient('H', Material.SKULL_ITEM, SkullType.PLAYER.ordinal()); // TODO: deprecated, but no alternative found...
		
		return goldenAppleFromHeadRecipe;
	}
	
	/**
	 * Returns the recipe that transforms 8 gold ingots and 1 wither head into
	 * a golden apple.
	 * 
	 * @return The shaped recipe.
	 */
	public ShapedRecipe getGoldenHeadMonsterRecipe() {
		short damage = 0;
		String name = i.t("craft.goldenApple.nameGoldenAppleFromHeadNormal");
		
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.craftNotchApple")) {
			damage = 1;
			name = i.t("craft.goldenApple.nameGoldenAppleFromHeadNotch");
		}
		
		ItemStack goldenAppleStack = new ItemStack(Material.GOLDEN_APPLE, p.getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.fromWither.numberCrafted", 1), damage);
		ItemMeta goldenAppleMeta = goldenAppleStack.getItemMeta();
		goldenAppleMeta.setDisplayName(ChatColor.RESET + name);
		goldenAppleStack.setItemMeta(goldenAppleMeta);
		
		ShapedRecipe goldenAppleFromWitherHeadRecipe = new ShapedRecipe(goldenAppleStack);
		
		goldenAppleFromWitherHeadRecipe.shape("GGG", "GHG", "GGG");
		goldenAppleFromWitherHeadRecipe.setIngredient('G', Material.GOLD_INGOT);
		goldenAppleFromWitherHeadRecipe.setIngredient('H', Material.SKULL_ITEM, SkullType.WITHER.ordinal()); // TODO: deprecated, but no alternative found...
		
		return goldenAppleFromWitherHeadRecipe;
	}
	
	/**
	 * Returns the recipe that transforms one golden apple into one golden apple.
	 * Used to remove the lore, so two apples from a different head are stackable.
	 * 
	 * @return The shapeless recipe.
	 */
	public ShapelessRecipe getLoreRemoverNormalRecipe() {
		ShapelessRecipe goldenAppleLoreRemoverRecipe = new ShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0));
		goldenAppleLoreRemoverRecipe.addIngredient(Material.GOLDEN_APPLE);
		
		return goldenAppleLoreRemoverRecipe;
	}
	
	/**
	 * Returns the recipe that transforms one enchanted golden apple into one enchanted
	 * golden apple.
	 * Used to remove the lore, so two apples from a different head are stackable.
	 * 
	 * @return The shapeless recipe.
	 */
	public ShapelessRecipe getLoreRemoverNotchRecipe() {
		ShapelessRecipe goldenAppleLoreRemoverNotchRecipe = new ShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));
		goldenAppleLoreRemoverNotchRecipe.addIngredient(Material.GOLDEN_APPLE, 1);
		
		return goldenAppleLoreRemoverNotchRecipe;
	}
	
	/**
	 * Returns the recipe that transforms one melon and one gold block into a golden melon.
	 * 
	 * @return The shapeless recipe.
	 */
	public ShapelessRecipe getGoldenMelonRecipe() {
		ShapelessRecipe goldenMelonRecipe = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
		goldenMelonRecipe.addIngredient(1, Material.GOLD_BLOCK);
		goldenMelonRecipe.addIngredient(1, Material.MELON);
		
		return goldenMelonRecipe;
	}
	
	/**
	 * Returns the recipe that transforms into a compass:
	 *  - in the center, a redstone powder;
	 *  - from the top, clockwise:
	 *     - iron
	 *     - spider eye
	 *     - iron
	 *     - rotten flesh
	 *     - iron
	 *     - bone
	 *     - iron
	 *     - gunpowder.
	 * 
	 * @return The shaped recipe.
	 */
	public ShapedRecipe getCompassRecipe() {
		ShapedRecipe compassRecipe = new ShapedRecipe(new ItemStack(Material.COMPASS));
		compassRecipe.shape(new String[] {"CIE", "IRI", "BIF"});
		
		compassRecipe.setIngredient('I', Material.IRON_INGOT);
		compassRecipe.setIngredient('R', Material.REDSTONE);
		compassRecipe.setIngredient('C', Material.SULPHUR);
		compassRecipe.setIngredient('E', Material.SPIDER_EYE);
		compassRecipe.setIngredient('B', Material.BONE);
		compassRecipe.setIngredient('F', Material.ROTTEN_FLESH);
		
		return compassRecipe;
	}
	
	/**
	 * Returns the vanilla recipe for the compass.
	 * 
	 * @return The shaped recipe.
	 */
	public ShapedRecipe getVanillaCompassRecipe() {
		ShapedRecipe vanillaCompassRecipe = new ShapedRecipe(new ItemStack(Material.COMPASS));
		vanillaCompassRecipe.shape(new String[] {" I ", "IRI", " I "});
		
		vanillaCompassRecipe.setIngredient('I', Material.IRON_INGOT);
		vanillaCompassRecipe.setIngredient('R', Material.REDSTONE);
		
		return vanillaCompassRecipe;
	}
	
	/**
	 * Returns the vanilla recipe for the golden melon.
	 * 
	 * @return The shaped recipe.
	 */
	public ShapedRecipe getVanillaGoldenMelonRecipe() {
		ShapedRecipe vanillaGoldenMelonRecipe = new ShapedRecipe(new ItemStack(Material.SPECKLED_MELON));
		vanillaGoldenMelonRecipe.shape(new String[] {"GGG", "GMG", "GGG"});
		
		vanillaGoldenMelonRecipe.setIngredient('G', Material.GOLD_NUGGET);
		vanillaGoldenMelonRecipe.setIngredient('M', Material.MELON);
		
		return vanillaGoldenMelonRecipe;
	}
}
