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

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimersModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.QuartzLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;


@ModuleInfo (
        name = "Freezer",
        description = "Provides a command to freeze some or all players, " +
                "allowing to “pause” the game.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.UTILITIES,
        icon = Material.PACKED_ICE
)
public class FreezerModule extends QSGModule
{
    private boolean isListenerRegistered = false;
    private FreezerListener freezerListener;

    private boolean globalFreeze = false;
    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Map<UUID, Boolean> oldAllowFly = new HashMap<>();
    private final Map<UUID, Boolean> oldFlyMode = new HashMap<>();

    private boolean hiddenFreeze = false;


    @Override
    public void onEnable()
    {
        this.freezerListener = new FreezerListener();
    }

    @Override
    protected void onDisable()
    {
        QuartzLib.unregisterEvents(freezerListener);
        freezerListener = null;
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(FreezeCommand.class);
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        if (!hiddenFreeze)
        {
            if (globalFreeze)
            {
                /// Notice displayed at the bottom of the sidebar if the game is paused (/uh freeze all).
                injector.injectLines(SidebarInjector.SidebarPriority.BOTTOM, true, I.t("{darkaqua}Game frozen"));
            }
            else if (isPlayerFrozen(player))
            {
                injector.injectLines(SidebarInjector.SidebarPriority.BOTTOM, true, I.t("{darkaqua}You are frozen"));
            }
        }
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
            QSG.game().getAlivePlayers().forEach(player -> setPlayerFreezeState(player, true));

            // Freezes the mobs by applying a Slowness effect. There isn't any EntityMoveEvent, so...
            QSG.get().getWorlds().stream()
                    .flatMap(world -> world.getLivingEntities().stream())
                    .filter(entity -> entity instanceof Creature)
                    .forEach(entity -> freezeCreature((Creature) entity, true));

            // Freezes the timers.
            QSG.module(TimersModule.class).pauseAllRunning(true);
        }

        else
        {
            // All the online players are listed, not the internal list of frozen players,
            // to avoid a ConcurrentModificationException if the iterated list is being emptied.
            QSG.game().getAlivePlayers().stream()
                    .filter(this::isPlayerFrozen)
                    .forEach(player -> this.setPlayerFreezeState(player, false));

            // Removes the slowness effect
            QSG.get().getWorlds().stream()
                    .flatMap(world -> world.getLivingEntities().stream())
                    .filter(entity -> entity instanceof Creature)
                    .forEach(entity -> freezeCreature((Creature) entity, false));

            // Unfreezes the timers.
            QSG.module(TimersModule.class).pauseAllRunning(false);
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
    public void setPlayerFreezeState(OfflinePlayer player, Boolean frozen)
    {
        if (frozen && !frozenPlayers.contains(player.getUniqueId()))
        {
            this.frozenPlayers.add(player.getUniqueId());
            this.oldAllowFly.put(player.getUniqueId(), player.isOnline() && player.getPlayer().getAllowFlight());
            this.oldFlyMode.put(player.getUniqueId(), player.isOnline() && player.getPlayer().isFlying());

            // Used to prevent the player to be kicked for fly if he was frozen during a fall.
            // He is blocked inside his current block anyway.
            if (player.isOnline()) player.getPlayer().setAllowFlight(true);
        }

        if (!frozen && frozenPlayers.contains(player.getUniqueId()))
        {
            this.frozenPlayers.remove(player.getUniqueId());

            if (player.isOnline())
            {
                player.getPlayer().setFlying(oldFlyMode.get(player.getUniqueId()));
                player.getPlayer().setAllowFlight(oldAllowFly.get(player.getUniqueId()));
            }

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
    public boolean isPlayerFrozen(final OfflinePlayer player)
    {
        return frozenPlayers.contains(player.getUniqueId());
    }

    /**
     * (Un)freezes a creature.
     *
     * @param creature The creature to freeze.
     * @param frozen If true the creature will be frozen. Else...
     */
    public void freezeCreature(final Creature creature, final boolean frozen)
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
                QuartzLib.registerEvents(freezerListener);
                this.isListenerRegistered = true;
            }
        }

        // Unregisters the listener if needed
        else
        {
            if (this.frozenPlayers.isEmpty() && !this.getGlobalFreezeState())
            {
                QuartzLib.unregisterEvents(freezerListener);
                this.isListenerRegistered = false;
            }
        }
    }


    /**
     * Returns the list of the currently frozen players.
     *
     * @return The list.
     */
    public Set<OfflinePlayer> getFrozenPlayers()
    {
        return frozenPlayers.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
    }
}
