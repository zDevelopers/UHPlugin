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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Fired when the game phase changes.
 *
 * <p><strong>Warning:</strong> if the new phase is {@link GamePhase#IN_GAME IN_GAME}
 * or {@link GamePhase#WAIT WAIT}, the game may be going backwards, if the game start
 * was cancelled because of an error, or if a player was resurrected at the end of the
 * game. To check if you are in this case, and e.g. avoid running game-start-code when
 * a player is resurrected, check the old phase with {@link #getOldPhase()}, or simpler,
 * use the {@link #isRunningForward()} method.</p>
 */
public class GamePhaseChangedEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final GamePhase oldPhase;
    private final GamePhase newPhase;

    public GamePhaseChangedEvent(final GamePhase oldPhase, final GamePhase newPhase)
    {
        this.oldPhase = oldPhase;
        this.newPhase = newPhase;
    }

    /**
     * The old phase. May be {@code null} if this is the first phase ever (i.e. {@link GamePhase#WAIT}).
     *
     * @return The old phase.
     */
    public GamePhase getOldPhase()
    {
        return oldPhase;
    }

    /**
     * @return The new phase.
     */
    public GamePhase getNewPhase()
    {
        return newPhase;
    }

    /**
     * Checks if this phase change is running forward regarding the normal phases progression.
     *
     * <h3>Example</h3>
     * <ul>
     * <li>If we have the cycle {@link GamePhase#STARTING STARTING} → {@link GamePhase#IN_GAME IN_GAME},
     * this will return {@code true}.</li>
     * <li>If we have the cycle {@link GamePhase#END END} → {@link GamePhase#IN_GAME IN_GAME},
     * this will return {@code false}.</li>
     * </ul>
     *
     * @return {@code true} if the game is running forward.
     */
    public boolean isRunningForward()
    {
        return oldPhase == null || newPhase.ordinal() > oldPhase.ordinal();
    }

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
