/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package eu.carrade.amaury.UHCReloaded.recipes;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
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

import java.util.ArrayList;


public class RecipesManager
{
    private UHCReloaded p = null;

    private Material compassCentralIngredient = null;
    private int compassRecipeType = -1;

    public static final String RECIPE_COMPASS = "compass";
    public static final String RECIPE_GLISTERING_MELON = "glistering";
    public static final String RECIPE_ENCHANTED_GOLDEN_APPLE = "EGA";

    public static final int COMPASS_DISABLED = 0;
    public static final int COMPASS_EASY = 1;
    public static final int COMPASS_MEDIUM = 2;
    public static final int COMPASS_HARD = 3;

    private String lastFailedRecipe = null;


    public RecipesManager(UHCReloaded plugin)
    {
        this.p = plugin;
    }


    /**
     * Registers the recipes needed, following the configuration.
     */
    public void registerRecipes()
    {

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.DO.get())
        {
            p.getServer().addRecipe(getGoldenHeadHumanRecipe());
        }

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.DO.get())
        {
            p.getServer().addRecipe(getGoldenHeadMonsterRecipe());
        }

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.ADD_LORE.get() || UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.ADD_LORE.get())
        {
            p.getServer().addRecipe(getLoreRemoverNormalRecipe());
            p.getServer().addRecipe(getLoreRemoverNotchRecipe());
        }

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_MELON_WITH_GOLD_BLOCK.get())
        {
            p.getServer().addRecipe(getGoldenMelonRecipe());
        }

        // Because the compass recipe is "semi-shapeless" (the central part is fixed, but the
        // loots can be placed into any configuration), there isn't a registered recipe for it
        // (I don't want to register 16 recipes for each difficulty).
        // Instead, using the inventoryClickEvent/inventoryDragEvent, we checks manually if the
        // recipe is valid.
        switch (this.getCompassRecipeType())
        {
            case COMPASS_EASY:
                compassCentralIngredient = Material.REDSTONE;
                break;
            case COMPASS_MEDIUM:
                compassCentralIngredient = Material.ENDER_PEARL;
                break;
            case COMPASS_HARD:
                compassCentralIngredient = Material.EYE_OF_ENDER;
                break;
            case COMPASS_DISABLED:
                break;
        }
    }

    /**
     * Checks if a recipe is allowed.
     *
     * @param recipe
     * @return True if the recipe is allowed, false else.
     */
    public boolean isRecipeAllowed(Recipe recipe)
    {

        // Vanilla compass recipe is disabled if the special compass is used.
        if (UHConfig.GAMEPLAY_CHANGES.COMPASS.ENABLED.get() && RecipeUtil.areSimilar(recipe, getVanillaCompassRecipe()))
        {
            this.lastFailedRecipe = RECIPE_COMPASS;
            return false;
        }

        // Vanilla golden melon recipe is disabled if the craft with a gold block is enabled.
        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_MELON_WITH_GOLD_BLOCK.get() && RecipeUtil.areSimilar(recipe, getVanillaGoldenMelonRecipe()))
        {
            this.lastFailedRecipe = RECIPE_GLISTERING_MELON;
            return false;
        }

        // If enchanted golden apples are disabled...
        // The same technique does not work, this is a workaround
        if (UHConfig.GAMEPLAY_CHANGES.GOLDEN_APPLE.DISABLE_NOTCH_APPLES.get())
        {
            if (recipe.getResult().getType() == Material.GOLDEN_APPLE)
            {
                for (ItemStack item : RecipeUtil.getListOfIngredients(recipe))
                {
                    if (item.getType() == Material.GOLD_BLOCK)
                    {
                        // There is a gold block in a recipe for a golden apple - NOPE NOPE NOPE
                        this.lastFailedRecipe = RECIPE_ENCHANTED_GOLDEN_APPLE;
                        return false;
                    }
                }
            }
        }

        // The recipe is allowed.
        return true;
    }

    /**
     * Checks if the recipe is a valid compass recipe.
     * <p>
     * A valid compass recipe is a recipe with:
     * <ul>
     *  <li>
     *    in the center, the valid ingredient for the current compass craft
     *    (redstone, ender pearl or eye of ender);
     *  </li>
     *  <li>
     *    four iron ingots placed like the vanilla compass recipe;
     *  </li>
     *  <li>
     *    in the four corners, a bone, a rotten flesh, a spider eye and a gunpowder,
     *    placed in any shape.
     *  </li>
     * </ul>
     * <p>
     * Executed in the  {@code onInventoryClick} and {@code onInventoryDrag} events, to allow this to be recognized even if
     * the recipe is not registered.
     *
     * @param matrix The content of the crafting inventory.
     * @return true if the recipe is an alternate recipe for the compass.
     */
    public boolean isValidCompassRecipe(ItemStack[] matrix)
    {
        if (matrix.length <= 5)
        {
            return false; // Small crafting grid
        }

        if (this.getCompassRecipeType() == COMPASS_DISABLED)
        {
            return false;
        }


        // 1: check of the static part (central ingredient + iron)

        Material iron1 = matrix[1].getType();
        Material iron2 = matrix[3].getType();
        Material iron3 = matrix[5].getType();
        Material iron4 = matrix[7].getType();
        Material centralIngredient = matrix[4].getType();

        if (!(iron1.equals(Material.IRON_INGOT)
                && iron2.equals(Material.IRON_INGOT)
                && iron3.equals(Material.IRON_INGOT)
                && iron4.equals(Material.IRON_INGOT)
                && centralIngredient.equals(compassCentralIngredient)))
        {
            return false;
        }

        // 2: check of the dynamic part (loots)

        ArrayList<Material> corners = new ArrayList<>();
        corners.add(matrix[0].getType());
        corners.add(matrix[2].getType());
        corners.add(matrix[6].getType());
        corners.add(matrix[8].getType());

        return corners.contains(Material.BONE)
                && corners.contains(Material.ROTTEN_FLESH)
                && corners.contains(Material.SPIDER_EYE)
                && corners.contains(Material.SULPHUR);
    }


    /**
     * Adds the lore to the golden apples, if needed.
     *
     * @param recipe The recipe to change.
     * @param inventory The crafting inventory (used to access the skull owner)
     * @return The modified result (ItemStack) if a change was needed. Null if no change is needed.
     */
    public ItemStack addLore(Recipe recipe, CraftingInventory inventory)
    {
        if ((UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.DO.get() || UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.DO.get())
                && (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.ADD_LORE.get() || UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.ADD_LORE.get())
                && (RecipeUtil.areSimilar(recipe, getGoldenHeadHumanRecipe()) || RecipeUtil.areSimilar(recipe, getGoldenHeadMonsterRecipe())))
        {
            ItemStack result = recipe.getResult();
            ItemMeta meta = result.getItemMeta();

            // Lookup for the head in the recipe
            String name = "";
            Boolean wither = true;

            for (ItemStack item : inventory.getContents())
            {
                if (item.getType() == Material.SKULL_ITEM && item.getDurability() == (short) SkullType.PLAYER.ordinal())
                { // An human head
                    SkullMeta sm = (SkullMeta) item.getItemMeta();
                    if (sm.hasOwner())
                    { // An human head
                        name = sm.getOwner();
                        wither = false;
                    }
                    break;
                }
            }

            if ((wither && UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.ADD_LORE.get())
                    || (!wither && UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.ADD_LORE.get()))
            {
                if (wither)
                {
                    /// Golden head lore for withers
                    meta.setLore(GuiUtils.generateLore(I.t("Made from the fallen head of a malignant monster")));
                }
                else
                {
                    /// Golden head lore for players. {0} = player name.
                    meta.setLore(GuiUtils.generateLore(I.t("Made from the fallen head of {0}", name)));
                }
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
    public ItemStack keepNameOnLoreRemover(Recipe recipe, CraftingInventory inventory)
    {
        if ((UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.ADD_LORE.get() || UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.ADD_LORE.get())
                && (RecipeUtil.areSimilar(recipe, getLoreRemoverNormalRecipe()) || RecipeUtil.areSimilar(recipe, getLoreRemoverNotchRecipe())))
        {

            ItemStack original = null;
            for (int slot = 0; slot <= 9; slot++)
            {
                original = inventory.getMatrix()[slot];
                if (original.getType() != Material.AIR)
                {
                    break; // Found (because there is only one item in the craft).
                }
            }

            ItemMeta metaOriginal = original.getItemMeta();

            if (metaOriginal != null && metaOriginal.hasDisplayName())
            {
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
     * Returns the current compass recipe.
     *
     * @return {@link RecipesManager#COMPASS_DISABLED}, {@link RecipesManager#COMPASS_EASY},
     * {@link RecipesManager#COMPASS_MEDIUM} or {@link RecipesManager#COMPASS_HARD}.
     */
    public int getCompassRecipeType()
    {
        if (compassRecipeType != -1)
        {
            return compassRecipeType;
        }

        if (UHConfig.GAMEPLAY_CHANGES.COMPASS.ENABLED.get())
        {
            switch (UHConfig.GAMEPLAY_CHANGES.COMPASS.RECIPE.get().toLowerCase())
            {
                case "easy":
                    compassRecipeType = COMPASS_EASY;
                    break;

                case "hard":
                    compassRecipeType = COMPASS_HARD;
                    break;

                default:
                    compassRecipeType = COMPASS_MEDIUM;
                    break;
            }
        }
        else
        {
            compassRecipeType = COMPASS_DISABLED;
        }

        return compassRecipeType;
    }

    /**
     * Returns the recipe that transforms 8 gold ingots and 1 human head into
     * a golden apple.
     *
     * @return The shaped recipe.
     */
    public ShapedRecipe getGoldenHeadHumanRecipe()
    {
        short damage = 0;
        /// Item name of a golden head (from a player)
        String name = I.tc("player_head", "{aqua}Golden head");

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.CRAFT_NOTCH_APPLE.get())
        {
            damage = 1;
            /// Item name of an enchanted golden head (from a player)
            name = I.tc("player_head", "{lightpurple}Golden head");
        }

        ItemStack goldenAppleStack = new ItemStack(Material.GOLDEN_APPLE, UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_HUMAN.NUMBER_CRAFTED.get(), damage);
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
    public ShapedRecipe getGoldenHeadMonsterRecipe()
    {
        short damage = 0;
        /// Item name of a golden head (from a monster)
        String name = I.tc("monster_head", "{aqua}Golden head");

        if (UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.CRAFT_NOTCH_APPLE.get())
        {
            damage = 1;
            /// Item name of an enchanted golden head (from a monster)
            name = I.tc("monster_head", "{lightpurple}Golden head");
        }

        ItemStack goldenAppleStack = new ItemStack(Material.GOLDEN_APPLE, UHConfig.GAMEPLAY_CHANGES.CRAFT_GOLDEN_APPLE_FROM_HEAD.FROM_WITHER.NUMBER_CRAFTED.get(), damage);
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
    public ShapelessRecipe getLoreRemoverNormalRecipe()
    {
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
    public ShapelessRecipe getLoreRemoverNotchRecipe()
    {
        ShapelessRecipe goldenAppleLoreRemoverNotchRecipe = new ShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));
        goldenAppleLoreRemoverNotchRecipe.addIngredient(Material.GOLDEN_APPLE, 1);

        return goldenAppleLoreRemoverNotchRecipe;
    }

    /**
     * Returns the recipe that transforms one melon and one gold block into a golden melon.
     *
     * @return The shapeless recipe.
     */
    public ShapelessRecipe getGoldenMelonRecipe()
    {
        ShapelessRecipe goldenMelonRecipe = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
        goldenMelonRecipe.addIngredient(1, Material.GOLD_BLOCK);
        goldenMelonRecipe.addIngredient(1, Material.MELON);

        return goldenMelonRecipe;
    }

    /**
     * Returns the vanilla recipe for the compass.
     *
     * @return The shaped recipe.
     */
    public ShapedRecipe getVanillaCompassRecipe()
    {
        ShapedRecipe vanillaCompassRecipe = new ShapedRecipe(new ItemStack(Material.COMPASS));
        vanillaCompassRecipe.shape(" I ", "IRI", " I ");

        vanillaCompassRecipe.setIngredient('I', Material.IRON_INGOT);
        vanillaCompassRecipe.setIngredient('R', Material.REDSTONE);

        return vanillaCompassRecipe;
    }

    /**
     * Returns the vanilla recipe for the golden melon.
     *
     * @return The shaped recipe.
     */
    public ShapedRecipe getVanillaGoldenMelonRecipe()
    {
        ShapedRecipe vanillaGoldenMelonRecipe = new ShapedRecipe(new ItemStack(Material.SPECKLED_MELON));
        vanillaGoldenMelonRecipe.shape("GGG", "GMG", "GGG");

        vanillaGoldenMelonRecipe.setIngredient('G', Material.GOLD_NUGGET);
        vanillaGoldenMelonRecipe.setIngredient('M', Material.MELON);

        return vanillaGoldenMelonRecipe;
    }


    /**
     * Returns the last failed recipe.
     *
     * Use {@link RecipesManager#RECIPE_COMPASS}, {@link RecipesManager#RECIPE_GLISTERING_MELON} and
     * {@link RecipesManager#RECIPE_ENCHANTED_GOLDEN_APPLE} to get the type of the failed recipe.
     *
     * @return the lastFailedRecipe
     */
    public String getLastFailedRecipe()
    {
        return lastFailedRecipe;
    }
}
