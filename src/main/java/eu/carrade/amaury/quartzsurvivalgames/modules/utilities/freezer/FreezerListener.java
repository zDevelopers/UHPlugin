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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.freezer;

import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;


public class FreezerListener implements Listener
{
    /**
     * Used to prevent frozen players to break blocks.
     */
    @EventHandler
    public void onBlockBreakEvent(final BlockBreakEvent ev)
    {
        if (QSG.module(FreezerModule.class).isPlayerFrozen(ev.getPlayer()))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to prevent frozen players to place blocks.
     */
    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent ev)
    {
        if (QSG.module(FreezerModule.class).isPlayerFrozen(ev.getPlayer()))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to freeze the players.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev)
    {
        QSG.module(FreezerModule.class).freezePlayerIfNeeded(ev.getPlayer(), ev.getFrom(), ev.getTo());
    }

    /**
     * Used to prevent the bows to be used while in global freeze mode.
     */
    @EventHandler
    public void onEntityShoot(EntityShootBowEvent ev)
    {
        if ((ev.getEntity() instanceof Player && QSG.module(FreezerModule.class).isPlayerFrozen((Player) ev.getEntity()))
                || QSG.module(FreezerModule.class).getGlobalFreezeState())
        {
            ev.setCancelled(true);

            // If a shoot from a player is cancelled, the arrow seems to be
            // consumed in the player' screen.
            // The inventory needs to be updated for the arrow to "come back".
            if (ev.getEntity() instanceof Player)
            {
                ((Player) ev.getEntity()).updateInventory();
            }
        }
    }

    /**
     * Used to prevent items from de-spawning if the game is frozen.
     */
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent ev)
    {
        if (QSG.module(FreezerModule.class).getGlobalFreezeState())
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to freeze the mobs spawning while the game is frozen.
     */
    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent ev)
    {
        if (QSG.module(FreezerModule.class).getGlobalFreezeState() && ev.getEntity() instanceof Creature)
        {
            QSG.module(FreezerModule.class).freezeCreature((Creature) ev.getEntity(), true);
        }
    }

    /**
     * Used to disable any damages if the player is frozen.
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent ev)
    {
        if (ev.getEntity() instanceof Player)
        {
            if (QSG.module(FreezerModule.class).isPlayerFrozen((Player) ev.getEntity()))
            {
                ev.setCancelled(true);
            }
        }
    }

    /**
     * Used to cancel any food loss (but the players can still eat).
     */
    @EventHandler
    public void onFoodUpdate(FoodLevelChangeEvent ev)
    {
        if (QSG.module(FreezerModule.class).isPlayerFrozen((Player) ev.getEntity()))
        {
            if (ev.getFoodLevel() < ((Player) ev.getEntity()).getFoodLevel())
            {
                ev.setCancelled(true);
            }
        }
    }
}
