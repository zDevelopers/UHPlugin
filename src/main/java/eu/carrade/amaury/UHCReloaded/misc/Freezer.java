/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.UHCReloaded.misc;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.listeners.FreezerListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class Freezer
{

    private UHCReloaded p = null;

    private boolean isListenerRegistered = false;
    private FreezerListener freezerListener = null;

    private Boolean globalFreeze = false;
    private ArrayList<UUID> frozenPlayers = new ArrayList<UUID>();
    private HashMap<UUID, Boolean> oldAllowFly = new HashMap<UUID, Boolean>();
    private HashMap<UUID, Boolean> oldFlyMode = new HashMap<UUID, Boolean>();

    private boolean hiddenFreeze = false;


    public Freezer(UHCReloaded plugin)
    {
        this.p = plugin;

        this.freezerListener = new FreezerListener(p);
    }


    /**
     * Freezes a player, if needed.
     * The player is blocked inside the block he is currently.
     *
     * This method is intended to be executed when a player moves.
     *
     * @param player The player to freeze
     * @param from The old position from the PlayerMoveEvent
     * @param to The new position from the PlayerMoveEvent
     */
    public void freezePlayerIfNeeded(Player player, Location from, Location to)
    {
        if (frozenPlayers.contains(player.getUniqueId()))
        {
            // If the X, Y or Z coordinate of the player change, he needs to be teleported inside the old block.
            // The yaw and pitch are conserved, to teleport more smoothly.
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
            {
                player.teleport(new Location(from.getWorld(), from.getBlockX() + 0.5, from.getBlockY(), from.getBlockZ() + 0.5, to.getYaw(), to.getPitch()), TeleportCause.PLUGIN);
            }
        }
    }


    /**
     * Enables or disables the global freeze of players, mobs, timer.
     *
     * @param frozen If true the global freeze will be enabled.
     * @param showStateInScoreboard If false, the freeze state will not be displayed in the scoreboard.
     */
    public void setGlobalFreezeState(Boolean frozen, Boolean showStateInScoreboard)
    {
        this.globalFreeze = frozen;
        this.hiddenFreeze = !showStateInScoreboard;

        if (frozen)
        {
            for (Player player : p.getGameManager().getOnlineAlivePlayers())
            {
                this.setPlayerFreezeState(player, true);
            }

            // Freezes the mobs by applying a Slowness effect. There isn't any EntityMoveEvent, so...
            for (World world : p.getServer().getWorlds())
            {
                for (Entity entity : world.getLivingEntities())
                {
                    if (entity instanceof Creature)
                    {
                        freezeCreature((Creature) entity, true);
                    }
                }
            }

            // Freezes the timers.
            p.getTimerManager().pauseAllRunning(true);
        }

        else
        {
            // All the online players are listed, not the internal list of frozen players,
            // to avoid a ConcurrentModificationException if the iterated list is being emptied.
            for (Player player : p.getServer().getOnlinePlayers())
            {
                if (this.isPlayerFrozen(player))
                {
                    this.setPlayerFreezeState(player, false);
                }
            }

            // Removes the slowness effect
            for (World world : p.getServer().getWorlds())
            {
                for (Entity entity : world.getLivingEntities())
                {
                    if (entity instanceof Creature)
                    {
                        freezeCreature((Creature) entity, false);
                    }
                }
            }

            // Unfreezes the timers.
            p.getTimerManager().pauseAllRunning(false);
        }

        updateListenerRegistration();
    }

    /**
     * Enables or disables the global freeze of players, mobs, timer.
     *
     * @param frozen If true the global freeze will be enabled.
     */
    public void setGlobalFreezeState(Boolean frozen)
    {
        setGlobalFreezeState(frozen, true);
    }


    /**
     * Returns the current state of the global freeze.
     *
     * @return True if the global freeze is enabled.
     */
    public boolean getGlobalFreezeState()
    {
        return this.globalFreeze;
    }

    /**
     * Freezes a player.
     *
     * @param player The player to freeze.
     * @param frozen If true the player will be frozen. If false, unfrozen.
     */
    public void setPlayerFreezeState(Player player, Boolean frozen)
    {
        if (frozen && !this.frozenPlayers.contains(player.getUniqueId()))
        {
            this.frozenPlayers.add(player.getUniqueId());
            this.oldAllowFly.put(player.getUniqueId(), player.getAllowFlight());
            this.oldFlyMode.put(player.getUniqueId(), player.isFlying());

            // Used to prevent the player to be kicked for fly if he was frozen during a fall.
            // He is blocked inside his current block anyway.
            player.setAllowFlight(true);
        }

        if (!frozen && this.frozenPlayers.contains(player.getUniqueId()))
        {
            this.frozenPlayers.remove(player.getUniqueId());

            player.setFlying(this.oldFlyMode.get(player.getUniqueId()));
            player.setAllowFlight(this.oldAllowFly.get(player.getUniqueId()));

            this.oldAllowFly.remove(player.getUniqueId());
            this.oldFlyMode.remove(player.getUniqueId());
        }

        updateListenerRegistration();
    }

    /**
     * Returns true if the given player is frozen.
     *
     * @param player The player to be checked.
     * @return true if the given player is frozen.
     */
    public boolean isPlayerFrozen(Player player)
    {
        return frozenPlayers.contains(player.getUniqueId());
    }

    /**
     * Returns {@code true} if the current freeze must be hidden in the sidebar.
     *
     * @return {@code true} to hide it.
     */
    public boolean isHiddenFreeze()
    {
        return hiddenFreeze;
    }

    /**
     * (Un)freezes a creature.
     *
     * @param creature The creature to freeze.
     * @param frozen If true the creature will be frozen. Else...
     */
    public void freezeCreature(Creature creature, Boolean frozen)
    {
        if (frozen)
        {
            // Freezes the creature for about 68 years.
            creature.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, true));
        }
        else
        {
            creature.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    /**
     * Registers the listener if it wasn't registered, and unregisters this listener
     * if there isn't any frozen player.
     *
     * Call this AFTER registering the first frozen player, and AFTER unregistering
     * the last one.
     */
    private void updateListenerRegistration()
    {
        // Registers the listener if needed
        // (i.e if there isn't any frozen player, or if the global freeze is enabled).
        if (!this.isListenerRegistered)
        {
            if (!this.frozenPlayers.isEmpty() || this.getGlobalFreezeState())
            {
                p.getServer().getPluginManager().registerEvents(freezerListener, p);
                this.isListenerRegistered = true;
            }
        }

        // Unregisters the listener if needed
        else
        {
            if (this.frozenPlayers.isEmpty() && !this.getGlobalFreezeState())
            {
                HandlerList.unregisterAll(freezerListener);
                this.isListenerRegistered = false;
            }
        }
    }


    /**
     * Returns the list of the currently frozen players.
     *
     * @return The list.
     */
    public ArrayList<Player> getFrozenPlayers()
    {

        ArrayList<Player> frozenPlayersList = new ArrayList<Player>();

        for (UUID id : frozenPlayers)
        {
            frozenPlayersList.add(p.getServer().getPlayer(id));
        }

        return frozenPlayersList;
    }
}
