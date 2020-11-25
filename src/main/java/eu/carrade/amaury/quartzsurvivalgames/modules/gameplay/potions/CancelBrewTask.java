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

package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay.potions;

import fr.zcraft.quartzlib.tools.items.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;


public class CancelBrewTask extends BukkitRunnable
{
    private final BrewerInventory inventory;
    private final HumanEntity whoClicked;

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
            final ItemStack ingredient = inventory.getIngredient();

            final Set<Material> forbiddenIngredients = new HashSet<>();

            if (Config.DISABLE_EXTENDED.get()) forbiddenIngredients.add(Material.REDSTONE);
            if (Config.DISABLE_LEVEL_II.get()) forbiddenIngredients.add(Material.GLOWSTONE_DUST);
            if (Config.DISABLE_SPLASH.get()) forbiddenIngredients.add(Material.SULPHUR);
            if (Config.DISABLE_LINGERING.get())
            {
                // 1.9 - 1.12
                try { forbiddenIngredients.add(Material.valueOf("DRAGONS_BREATH")); }
                catch (IllegalArgumentException ignored) { }

                // 1.13+
                try { forbiddenIngredients.add(Material.valueOf("DRAGON_BREATH")); }
                catch (IllegalArgumentException ignored) { }
            }

            if (ingredient.getType() != null && forbiddenIngredients.contains(ingredient.getType()))
            {
                // The element is removed and added back to the player's inventory.
                inventory.setIngredient(new ItemStack(Material.AIR));
                ItemUtils.give((Player) whoClicked, ingredient);
            }
        }
    }
}
