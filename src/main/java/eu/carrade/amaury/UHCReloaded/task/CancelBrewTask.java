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

package eu.carrade.amaury.UHCReloaded.task;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


public class CancelBrewTask extends BukkitRunnable
{

    private BrewerInventory inventory = null;
    private HumanEntity whoClicked = null;

    public CancelBrewTask(BrewerInventory inventory, HumanEntity whoClicked)
    {
        this.inventory = inventory;
        this.whoClicked = whoClicked;
    }

    @Override
    public void run()
    {
        if (inventory.getIngredient() == null)
        {
            return; // Nothing to do!
        }

        if (whoClicked instanceof Player)
        {
            ItemStack ingredient = inventory.getIngredient();

            if (ingredient.getType() != null && ingredient.getType().equals(Material.GLOWSTONE_DUST))
            {
                inventory.setIngredient(new ItemStack(Material.AIR)); // The glowstone is removed.

                // First try: try to add the glowstone to an existing stack
                Boolean added = false;
                for (ItemStack item : whoClicked.getInventory().getContents())
                {
                    if (item != null && item.getType() != null && item.getType().equals(Material.GLOWSTONE_DUST))
                    {
                        if (item.getAmount() + ingredient.getAmount() <= item.getMaxStackSize())
                        {
                            // We can add the glowstone here.
                            item.setAmount(item.getAmount() + ingredient.getAmount());
                            added = true;
                            break;
                        }
                    }
                }

                if (!added)
                {
                    // Failed... We adds the glowstone to the first empty slot found.

                    Integer slotEmpty = whoClicked.getInventory().firstEmpty();

                    if (slotEmpty != -1)
                    { // -1 is returned if there isn't any empty slot
                        whoClicked.getInventory().setItem(slotEmpty, ingredient);
                    }
                    else
                    {
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
