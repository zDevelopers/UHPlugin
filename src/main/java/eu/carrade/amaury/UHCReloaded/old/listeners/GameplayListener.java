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
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.old.task.CancelBrewTask;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.BrewerInventory;


public class GameplayListener implements Listener
{
    private final UHCReloaded p;

    public GameplayListener()
    {
        this.p = UHCReloaded.get();
    }


    /**
     * Used to disable power-II potions.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent ev)
    {
        if (UHConfig.GAMEPLAY_CHANGES.DISABLE_LEVEL_II_POTIONS.get() && ev.getInventory() instanceof BrewerInventory)
        {
            RunTask.later(new CancelBrewTask((BrewerInventory) ev.getInventory(), ev.getWhoClicked()), 1L);
        }
    }

    /**
     * Used to disable power-II potions.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev)
    {
        if (UHConfig.GAMEPLAY_CHANGES.DISABLE_LEVEL_II_POTIONS.get() && ev.getInventory() instanceof BrewerInventory)
        {
           RunTask.later(new CancelBrewTask((BrewerInventory) ev.getInventory(), ev.getWhoClicked()), 1L);
        }
    }
}
