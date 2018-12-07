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

package eu.carrade.amaury.UHCReloaded.modules.core.game.events.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * Fired when a player playing an UHC match is dead.
 *
 * When this event is called, the player is not yet removed from the alive
 * players (i.e. {@code UR.module(GameModule.class).isAlive(ev.getPlayer())}
 * will return {@code true}).
 *
 * If the event is cancelled, the player will die but will not be removed
 * from the alive players.
 */
public class AlivePlayerDeathEvent extends Event implements Cancellable
{
    private final OfflinePlayer player;
    private final PlayerDeathEvent playerDeathEvent;

    private boolean cancelled = false;

    public AlivePlayerDeathEvent(final OfflinePlayer player, final PlayerDeathEvent playerDeathEvent)
    {
        this.player = player;
        this.playerDeathEvent = playerDeathEvent;
    }

    /**
     * Returns the dead player.
     * @return The player.
     */
    public OfflinePlayer getPlayer()
    {
        return player;
    }

    /**
     * Returns the underlying {@link PlayerDeathEvent}. Can be {@code null} if the player
     * was killed using {@code /uh kill} or programmatically.
     *
     * @return The {@link PlayerDeathEvent}.
     */
    public PlayerDeathEvent getPlayerDeathEvent()
    {
        return playerDeathEvent;
    }


    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }


    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
