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
package eu.carrade.amaury.UHCReloaded.old.recipes;

import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Utility class to compare Bukkit recipes.<br>
 * Useful for identifying your recipes in events, where recipes are re-generated in a different manner.
 *
 * @version R1.3
 * @author Digi
 */
public class RecipeUtil
{
    /**
     * The wildcard data value for ingredients.<br>
     * If this is used as data value on an ingredient it will accept any data value.
     */
    public static final short DATA_WILDCARD = Short.MAX_VALUE;

    /**
     * Checks if both recipes are equal.<br>
     * Compares both ingredients and results.<br>
     * <br>
     * NOTE: If both arguments are null it returns true.
     *
     * @param recipe1 the first recipe
     * @param recipe2 the second recipe
     * @return true if ingredients and results match, false otherwise.
     * @throws IllegalArgumentException
     *             if recipe is other than ShapedRecipe, ShapelessRecipe or FurnaceRecipe.
     */
    public static boolean areEqual(Recipe recipe1, Recipe recipe2)
    {
        return recipe1 == recipe2 || !(recipe1 == null || recipe2 == null) && recipe1.getResult().equals(recipe2.getResult()) && match(recipe1, recipe2);

    }

    /**
     * Checks if recipes are similar.<br>
     * Only checks ingredients, not results.<br>
     * <br>
     * NOTE: If both arguments are null it returns true. <br>
     *
     * @param recipe1 the first recipe
     * @param recipe2 the second recipe
     * @return true if ingredients match, false otherwise.
     * @throws IllegalArgumentException
     *             if recipe is other than ShapedRecipe, ShapelessRecipe or FurnaceRecipe.
     */
    public static boolean areSimilar(Recipe recipe1, Recipe recipe2)
    {
        return recipe1 == recipe2 || !(recipe1 == null || recipe2 == null) && match(recipe1, recipe2);
    }

    private static boolean match(Recipe recipe1, Recipe recipe2)
    {
        if (recipe1 instanceof ShapedRecipe)
        {
            if (!(recipe2 instanceof ShapedRecipe))
            {
                return false; // if other recipe is not the same type then they're not equal.
            }

            final ShapedRecipe r1 = (ShapedRecipe) recipe1;
            final ShapedRecipe r2 = (ShapedRecipe) recipe2;

            // convert both shapes and ingredient maps to common ItemStack array.
            final ItemStack[] matrix1 = shapeToMatrix(r1.getShape(), r1.getIngredientMap());
            final ItemStack[] matrix2 = shapeToMatrix(r2.getShape(), r2.getIngredientMap());

            if (!Arrays.equals(matrix1, matrix2)) // compare arrays and if they don't match run another check with one shape mirrored.
            {
                mirrorMatrix(matrix1);

                return Arrays.equals(matrix1, matrix2);
            }

            return true; // ingredients match.
        }
        else if (recipe1 instanceof ShapelessRecipe)
        {
            if (!(recipe2 instanceof ShapelessRecipe))
            {
                return false; // if other recipe is not the same type then they're not equal.
            }

            final ShapelessRecipe r1 = (ShapelessRecipe) recipe1;
            final ShapelessRecipe r2 = (ShapelessRecipe) recipe2;

            // get copies of the ingredient lists
            final List<ItemStack> find = r1.getIngredientList();
            final List<ItemStack> compare = r2.getIngredientList();

            if (find.size() != compare.size())
            {
                return false; // if they don't have the same amount of ingredients they're not equal.
            }

            for (ItemStack item : compare)
            {
                if (!find.remove(item))
                {
                    return false; // if ingredient wasn't removed (not found) then they're not equal.
                }
            }

            return find.isEmpty(); // if there are any ingredients not removed then they're not equal.
        }
        else if (recipe1 instanceof FurnaceRecipe)
        {
            if (!(recipe2 instanceof FurnaceRecipe))
            {
                return false; // if other recipe is not the same type then they're not equal.
            }

            final FurnaceRecipe r1 = (FurnaceRecipe) recipe1;
            final FurnaceRecipe r2 = (FurnaceRecipe) recipe2;

            return r1.getInput().getType() == r2.getInput().getType();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported recipe type: '" + recipe1 + "', update this class!");
        }
    }

    private static ItemStack[] shapeToMatrix(String[] shape, Map<Character, ItemStack> map)
    {
        final ItemStack[] matrix = new ItemStack[9];
        int slot = 0;

        for (int r = 0; r < shape.length; r++)
        {
            for (char col : shape[r].toCharArray())
            {
                matrix[slot] = map.get(col);
                slot++;
            }

            slot = ((r + 1) * 3);
        }

        return matrix;
    }

    private static void mirrorMatrix(ItemStack[] matrix)
    {
        ItemStack tmp;

        for (int r = 0; r < 3; r++)
        {
            tmp = matrix[(r * 3)];
            matrix[(r * 3)] = matrix[(r * 3) + 2];
            matrix[(r * 3) + 2] = tmp;
        }
    }


    /**
     * Returns the list of the ingredients of the given recipe.
     *
     * @author Amaury Carrade
     *
     * @param recipe The recipe to analyze.
     * @return A list of the ingredients.
     */
    public static List<ItemStack> getListOfIngredients(Recipe recipe)
    {
        List<ItemStack> listOfItems;
        if (recipe instanceof ShapelessRecipe)
        {
            listOfItems = ((ShapelessRecipe) recipe).getIngredientList();
        }
        else
        {
            try
            {
                listOfItems = new LinkedList<>(((ShapedRecipe) recipe).getIngredientMap().values());
            }
            catch (NullPointerException e)  // If the list of items is null
            {
                listOfItems = new LinkedList<>(); // empty list
            }
        }

        return listOfItems;
    }
}