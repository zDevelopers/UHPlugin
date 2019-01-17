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

package eu.carrade.amaury.UHCReloaded.old.listeners;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.protips.ProTips;
import eu.carrade.amaury.UHCReloaded.old.recipes.RecipesManager;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;


public class CraftingListener implements Listener
{
    private UHCReloaded p = null;

    public CraftingListener(UHCReloaded p)
    {
        this.p = p;
    }


    /**
     * Used to:
     *  - prevent items to be crafted;
     *  - send a ProTip containing the craft to use, if an error occurred;
     *  - add a lure to the golden apples crafted from a head;
     *  - keep the name of the item when the anti-lore craft is used.
     */
    @EventHandler
    public void onPreCraftEvent(PrepareItemCraftEvent ev)
    {
        Recipe recipe = ev.getRecipe();

        if (recipe == null)
        {
            return;
        }

        /* *** Prevents items to be crafted *** */

        if (!p.getRecipesManager().isRecipeAllowed(recipe))
        {
            ev.getInventory().setResult(new ItemStack(Material.AIR));

            // ProTips
            final String failedRecipe = p.getRecipesManager().getLastFailedRecipe();
            final Player player = (Player) ev.getViewers().get(0); // crafting inventory: only one viewer in all cases.

            RunTask.later(() ->
            {
                switch (failedRecipe)
                {
                    case RecipesManager.RECIPE_COMPASS:
                        switch (p.getRecipesManager().getCompassRecipeType())
                        {
                            case RecipesManager.COMPASS_EASY:
                                ProTips.CRAFT_COMPASS_EASY.sendTo(player);
                                break;
                            case RecipesManager.COMPASS_MEDIUM:
                                ProTips.CRAFT_COMPASS_MEDIUM.sendTo(player);
                                break;
                            case RecipesManager.COMPASS_HARD:
                                ProTips.CRAFT_COMPASS_HARD.sendTo(player);
                                break;
                        }

                        break;

                    case RecipesManager.RECIPE_GLISTERING_MELON:
                        ProTips.CRAFT_GLISTERING_MELON.sendTo(player);
                        break;

                    case RecipesManager.RECIPE_ENCHANTED_GOLDEN_APPLE:
                        ProTips.CRAFT_NO_ENCHANTED_GOLDEN_APPLE.sendTo(player);
                        break;
                }
            }, 40L);

            return;
        }


        /* *** Adds a lore to the golden apples crafted from a head *** */

        ItemStack loreResult = p.getRecipesManager().addLore(recipe, ev.getInventory());
        if (loreResult != null)
        {
            ev.getInventory().setResult(loreResult);
            return;
        }


        /* *** The lore remover don't change the name of the item *** */

        ItemStack keepNameResult = p.getRecipesManager().keepNameOnLoreRemover(recipe, ev.getInventory());
        if (keepNameResult != null)
        {
            ev.getInventory().setResult(keepNameResult);
            return;
        }
    }
}
