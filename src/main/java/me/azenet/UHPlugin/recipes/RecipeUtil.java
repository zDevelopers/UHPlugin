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

package me.azenet.UHPlugin.recipes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

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
        if(recipe1 instanceof ShapedRecipe)
        {
            if(!(recipe2 instanceof ShapedRecipe))
            {
            	return false; // if other recipe is not the same type then they're not equal.
            }
            
            ShapedRecipe r1 = (ShapedRecipe)recipe1;
            ShapedRecipe r2 = (ShapedRecipe)recipe2;
            
            // convert both shapes and ingredient maps to common ItemStack array.
            ItemStack[] matrix1 = shapeToMatrix(r1.getShape(), r1.getIngredientMap());
            ItemStack[] matrix2 = shapeToMatrix(r2.getShape(), r2.getIngredientMap());
            
            if(!Arrays.equals(matrix1, matrix2)) // compare arrays and if they don't match run another check with one shape mirrored.
            {
            	 mirrorMatrix(matrix1);
                
                return Arrays.equals(matrix1, matrix2);
            }
            
            return true; // ingredients match.
        }
        else if(recipe1 instanceof ShapelessRecipe)
        {
            if(!(recipe2 instanceof ShapelessRecipe))
            {
                return false; // if other recipe is not the same type then they're not equal.
            }
            
            ShapelessRecipe r1 = (ShapelessRecipe)recipe1;
            ShapelessRecipe r2 = (ShapelessRecipe)recipe2;
            
            // get copies of the ingredient lists
            List<ItemStack> find = r1.getIngredientList();
            List<ItemStack> compare = r2.getIngredientList();
            
            if(find.size() != compare.size())
            {
                return false; // if they don't have the same amount of ingredients they're not equal.
            }
            
            for(ItemStack item : compare)
            {
                if(!find.remove(item))
                {
                    return false; // if ingredient wasn't removed (not found) then they're not equal.
                }
            }
            
            return find.isEmpty(); // if there are any ingredients not removed then they're not equal.
        }
        else if(recipe1 instanceof FurnaceRecipe)
        {
            if(!(recipe2 instanceof FurnaceRecipe))
            {
                return false; // if other recipe is not the same type then they're not equal.
            }
            
            FurnaceRecipe r1 = (FurnaceRecipe)recipe1;
            FurnaceRecipe r2 = (FurnaceRecipe)recipe2;
            
            //return (r1.getInput().equals(r2.getInput()));
            return r1.getInput().getType() == r2.getInput().getType();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported recipe type: '" + recipe1 + "', update this class!");
        }
    }
    
    private static ItemStack[] shapeToMatrix(String[] shape, Map<Character, ItemStack> map)
    {
        ItemStack[] matrix = new ItemStack[9];
        int slot = 0;
        
        for(int r = 0; r < shape.length; r++)
        {
            for(char col : shape[r].toCharArray())
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
        
        for(int r = 0; r < 3; r++)
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
	public static List<ItemStack> getListOfIngredients(Recipe recipe) {
		List<ItemStack> listOfItems = null;
		if(recipe instanceof ShapelessRecipe) {
			listOfItems = ((ShapelessRecipe) recipe).getIngredientList();
		}
		else {
			try {
				listOfItems = new LinkedList<ItemStack>(((ShapedRecipe) recipe).getIngredientMap().values());
			}
			catch(NullPointerException e) { // If the list of items is null
				listOfItems = new LinkedList<ItemStack>(); // empty list
			}
		}
		
		return listOfItems;
	}
}