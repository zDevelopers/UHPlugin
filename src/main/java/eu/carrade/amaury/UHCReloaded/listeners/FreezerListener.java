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

package eu.carrade.amaury.UHCReloaded.listeners;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;


public class FreezerListener implements Listener
{

    private UHCReloaded p = null;

    public FreezerListener(UHCReloaded p)
    {
        this.p = p;
    }


    /**
     * Used to prevent frozen players to break blocks.
     *
     * @param ev
     */
    @EventHandler
    public void onBlockBreakEvent(final BlockBreakEvent ev)
    {
        if (p.getFreezer().isPlayerFrozen(ev.getPlayer()))
        {
            ev.setCancelled(true);
        }
    }


    /**
     * Used to prevent frozen players to place blocks.
     *
     * @param ev
     */
    @EventHandler
    public void onBlockPlaceEvent(final BlockPlaceEvent ev)
    {
        if (p.getFreezer().isPlayerFrozen(ev.getPlayer()))
        {
            ev.setCancelled(true);
        }
    }


    /**
     * Used to freeze the players.
     *
     * @param ev
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev)
    {
        p.getFreezer().freezePlayerIfNeeded(ev.getPlayer(), ev.getFrom(), ev.getTo());
    }


    /**
     * Used to prevent the bows to be used while in global freeze mode.
     *
     * @param ev
     */
    @EventHandler
    public void onEntityShoot(EntityShootBowEvent ev)
    {
        if ((ev.getEntity() instanceof Player && p.getFreezer().isPlayerFrozen((Player) ev.getEntity()))
                || p.getFreezer().getGlobalFreezeState())
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
     * Used to prevent items from despawning if the game is frozen.
     *
     * @param ev
     */
    @EventHandler
    public void onItemDespawn(ItemDespawnEvent ev)
    {
        if (p.getFreezer().getGlobalFreezeState())
        {
            ev.setCancelled(true);
        }
    }


    /**
     * Used to freeze the mobs spawning while the game is frozen.
     * @param ev
     */
    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent ev)
    {
        if (p.getFreezer().getGlobalFreezeState() && ev.getEntity() instanceof Creature)
        {
            p.getFreezer().freezeCreature((Creature) ev.getEntity(), true);
        }
    }


    /**
     * Used to disable any damages if the player is frozen.
     *
     * @param ev
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent ev)
    {
        if (ev.getEntity() instanceof Player)
        {
            if (p.getFreezer().isPlayerFrozen((Player) ev.getEntity()))
            {
                ev.setCancelled(true);
            }
        }
    }

    /**
     * Used to cancel any food loss (but the players can still eat).
     *
     * @param ev
     */
    @EventHandler
    public void onFoodUpdate(FoodLevelChangeEvent ev)
    {
        if (p.getFreezer().isPlayerFrozen((Player) ev.getEntity()))
        {
            if (ev.getFoodLevel() < ((Player) ev.getEntity()).getFoodLevel())
            {
                ev.setCancelled(true);
            }
        }
    }
}
