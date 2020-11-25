/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay.compass;

import eu.carrade.amaury.quartzsurvivalgames.utils.RecipesUtils;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.items.CraftingRecipes;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;

public class CompassRecipes extends QuartzComponent implements Listener
{
    private final Recipe VANILLA_RECIPE = CraftingRecipes.shaped(
            new ItemStack(Material.COMPASS),
            " A ",
            "ABA",
            " A ",
            Material.IRON_INGOT, Material.REDSTONE
    );

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
    public boolean isValidCompassRecipe(final ItemStack[] matrix)
    {
        if (matrix.length <= 5)
        {
            return false; // Small crafting grid
        }


        // 0: is it fully filled?

        for (int i = 0; i < 9; i++)
        {
            if (matrix[i] == null) return false;
        }


        // 1: check of the static part (central ingredient + iron)

        final Material iron1 = matrix[1].getType();
        final Material iron2 = matrix[3].getType();
        final Material iron3 = matrix[5].getType();
        final Material iron4 = matrix[7].getType();
        final Material centralIngredient = matrix[4].getType();

        if (!(iron1.equals(Material.IRON_INGOT)
                && iron2.equals(Material.IRON_INGOT)
                && iron3.equals(Material.IRON_INGOT)
                && iron4.equals(Material.IRON_INGOT)
                && centralIngredient.equals(getCentralIngredient())))
        {
            return false;
        }


        // 2: check of the dynamic part (loots)

        final ArrayList<Material> corners = new ArrayList<>();
        corners.add(matrix[0].getType());
        corners.add(matrix[2].getType());
        corners.add(matrix[6].getType());
        corners.add(matrix[8].getType());

        return corners.contains(Material.BONE)
                && corners.contains(Material.ROTTEN_FLESH)
                && corners.contains(Material.SPIDER_EYE)
                && corners.contains(Material.GUNPOWDER);
    }

    private Material getCentralIngredient()
    {
        switch (Config.RECIPE.get())
        {
            case MEDIUM:
                return Material.ENDER_PEARL;

            case HARD:
                return Material.ENDER_EYE;

            default:
                return Material.REDSTONE;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent ev)
    {
        if (Config.RECIPE.get() == CompassRecipe.DEFAULT) return;

        if (ev.getWhoClicked() instanceof Player)
        {
            final Inventory inventory = ev.getInventory();

            // Workaround to fix the crafting grid being not updated when the item is taken
            // from the grid.
            if (inventory instanceof CraftingInventory && ev.getSlotType() == InventoryType.SlotType.RESULT)
            {
                RunTask.later(
                        () -> ev.getViewers().stream()
                                .filter(viewer -> viewer instanceof Player)
                                .forEach(viewer -> ((Player) viewer).updateInventory()),
                        1L
                );
            }


            /* *** Allows any shape for the loots in the compass recipe. *** */

            if (inventory instanceof CraftingInventory)
            {
                // This is ran one tick after the click because when the event is fired, the inventory
                // object is not updated, and so the result of the isValidCompassResult is invalid.

                RunTask.later(() ->
                {
                    if (isValidCompassRecipe(((CraftingInventory) inventory).getMatrix()))
                    {
                        // Puts the compass in the result slot
                        if (ev.getSlotType() == InventoryType.SlotType.CRAFTING)
                        {
                            ((CraftingInventory) inventory).setResult(new ItemStack(Material.COMPASS));
                            ev.setResult(Event.Result.ALLOW);

                            ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                        }

                        // Consumes the materials in the crafting grid.
                        // Because this is not an "official" recipe, we need to do that manually.
                        else if (ev.getSlotType() == InventoryType.SlotType.RESULT)
                        {
                            int index = 1;
                            for (ItemStack stack : ((CraftingInventory) inventory).getMatrix())
                            {
                                if (stack == null) continue;

                                if (stack.getAmount() != 1)
                                {
                                    stack.setAmount(stack.getAmount() - 1);
                                    inventory.setItem(index, stack);
                                }
                                else
                                {
                                    inventory.setItem(index, new ItemStack(Material.AIR));
                                }

                                index++;
                            }

                            ev.setCurrentItem(new ItemStack(Material.COMPASS));
                            ev.setResult(Event.Result.ALLOW);

                            ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                        }
                    }
                }, 1L);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent ev)
    {
        if (Config.RECIPE.get() == CompassRecipe.DEFAULT) return;

        if (ev.getInventory() instanceof CraftingInventory)
        {
            RunTask.later(() ->
            {
                if (isValidCompassRecipe(((CraftingInventory) ev.getInventory()).getMatrix()))
                {
                    ((CraftingInventory) ev.getInventory()).setResult(new ItemStack(Material.COMPASS));
                    ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                }
            }, 1L);
        }
    }

    @EventHandler
    private void onPreCraft(final PrepareItemCraftEvent ev)
    {
        if (Config.RECIPE.get() != CompassRecipe.DEFAULT && RecipesUtils.areSimilar(ev.getRecipe(), VANILLA_RECIPE))
        {
            ev.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    public enum CompassRecipe
    {
        /**
         * The default recipe with redstone and iron.
         */
        DEFAULT,

        /**
         * The same recipe as the default, but with the four main mobs loots
         * in the corners: one string, one spider eye, one gun powder and
         * one zombie flesh.
         */
        EASY,

        /**
         * The same recipe as the easy one, but with an ender pearl instead
         * of the redstone at the center.
         */
        MEDIUM,

        /**
         * The same recipe as the medium one, but wyth an eye of ender instead
         * of the ender pearl.
         */
        HARD
    }
}
