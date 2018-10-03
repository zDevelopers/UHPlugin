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
package eu.carrade.amaury.UHCReloaded.game;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Manages the initial teleportation process, and stores the spawn points of the players.
 */
public class Teleporter
{
    /**
     * The spawn point designed for each player.
     */
    private final Map<UUID, Location> spawnPoints = new HashMap<>();

    /**
     * The cages generated for each team
     */
    private final Map<UUID, Cage> cages = new HashMap<>();


    /**
     * Called when a player is teleported, during the teleportation process.
     *
     * <p>NOT called with {@link #teleportPlayer(UUID, Boolean)}.</p>
     */
    private Callback<UUID> onTeleportation = null;

    /**
     * Called when a player is teleported successfully, during the teleportation process.
     *
     * <p>NOT called with {@link #teleportPlayer(UUID, Boolean)}.</p>
     */
    private Callback<UUID> onTeleportationSuccessful = null;

    /**
     * Called when a player cannot be teleported (if offline, or without spawn...).
     */
    private Callback<UUID> onTeleportationFailed = null;

    /**
     * Called when the teleportation process started by {@link #startTeleportationProcess(Boolean)}
     * ends.
     */
    private Callback<Set<UUID>> onTeleportationProcessFinished = null;


    /**
     * Saves the spawn point of a player.
     *
     * @param playerUUID The player's UUID.
     * @param spawn      The spawn location.
     */
    public void setSpawnForPlayer(final UUID playerUUID, final Location spawn)
    {
        spawnPoints.put(playerUUID, spawn);
    }

    /**
     * Checks if a spawn point is registered for the given player.
     *
     * @param playerUUID The player UUID.
     * @return {@code true} if a spawn point is registered.
     */
    public boolean hasSpawnForPlayer(final UUID playerUUID)
    {
        return spawnPoints.containsKey(playerUUID);
    }

    /**
     * @param playerUUID A player's UUID.
     * @return The registered spawn point for that player, or {@code null} if no-one was ever registered.
     */
    public Location getSpawnForPlayer(final UUID playerUUID)
    {
        return spawnPoints.get(playerUUID).clone();
    }


    /**
     * Registers a cage for a player.
     *
     * @param player The player
     * @param cage The cage
     */
    public void setCageForPlayer(final UUID player, final Cage cage)
    {
        cages.put(player, cage);
    }

    /**
     * Checks if a cage is registered for the given player.
     * @param player The player.
     * @return {@code true} if a cage is registered.
     */
    public boolean hasCageForPlayer(final UUID player)
    {
        return cages.containsKey(player);
    }

    /**
     * @param player A player
     * @return The registered {@link Cage} for this player, or {@code null} if no one is registered.
     */
    public Cage getCageForPlayer(final UUID player)
    {
        return cages.get(player);
    }


    /**
     * Teleports the given player to the spawn point.
     *
     * @param playerUUID       The player's UUID.
     * @param teleportOnGround if {@code true} the player will be teleported on the ground; else, at
     *                         the location directly.
     *
     * @return {@code true} if the player was teleported (i.e. was online and with an associated
     * spawn point).
     */
    public boolean teleportPlayer(UUID playerUUID, Boolean teleportOnGround)
    {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null)
            return false;

        Location spawn = spawnPoints.get(playerUUID);

        if (spawn == null)
            return false;

        else if (teleportOnGround)
            spawn = spawn.getWorld().getHighestBlockAt(spawn).getLocation().add(0, 2, 0);

        if (!teleportOnGround)
        {
            final Cage cage = cages.get(playerUUID);
            if (cage != null) cage.build();
        }

        player.teleport(spawn);
        return true;
    }


    /**
     * Registers a callback called while trying to teleport a player.
     *
     * @param callback The callback. Argument: the teleported player's UUID.
     *
     * @return Same instance for chaining.
     */
    public Teleporter whenTeleportationOccurs(Callback<UUID> callback)
    {
        onTeleportation = callback;
        return this;
    }

    /**
     * Registers a callback called when a player is teleported successfully.
     *
     * @param callback The callback. Argument: the non-teleported player's UUID.
     *
     * @return Same instance for chaining.
     */
    public Teleporter whenTeleportationSuccesses(Callback<UUID> callback)
    {
        onTeleportationSuccessful = callback;
        return this;
    }

    /**
     * Registers a callback called when a player cannot be teleported.
     *
     * @param callback The callback. Argument: the non-teleported player's UUID.
     *
     * @return Same instance for chaining.
     */
    public Teleporter whenTeleportationFails(Callback<UUID> callback)
    {
        onTeleportationFailed = callback;
        return this;
    }

    /**
     * Registers a callback called when the whole teleportation process (started with {@link
     * #startTeleportationProcess(Boolean)}) ends.
     *
     * <p>This callback is NOT called when {@link #teleportPlayer(UUID, Boolean)} is used.</p>
     *
     * @param callback The callback. Argument: a set containing the UUID of all non-teleported
     *                 players.
     *
     * @return Same instance for chaining.
     */
    public Teleporter whenTeleportationEnds(Callback<Set<UUID>> callback)
    {
        onTeleportationProcessFinished = callback;
        return this;
    }


    /**
     * Teleports the players.
     *
     * @param slowMode if {@code true}, the players will be slowly teleported one by one, with a
     *                 delay between them.
     */
    public void startTeleportationProcess(Boolean slowMode)
    {
        // Fast mode: we loop on the spawn points and teleport everyone. Bim.
        if (!slowMode)
        {
            Set<UUID> fails = new HashSet<>();

            for (UUID playerUUID : spawnPoints.keySet())
            {
                UHUtils.callIfDefined(onTeleportation, playerUUID);

                if (teleportPlayer(playerUUID, false))
                {
                    UHUtils.callIfDefined(onTeleportationSuccessful, playerUUID);
                }
                else
                {
                    UHUtils.callIfDefined(onTeleportationFailed, playerUUID);
                    fails.add(playerUUID);
                }
            }

            UHUtils.callIfDefined(onTeleportationProcessFinished, fails);
        }

        // Slow mode
        else
        {
            RunTask.timer(
                new TeleportationRunnable(
                        this,
                        spawnPoints.keySet(),
                        onTeleportation,
                        onTeleportationSuccessful,
                        onTeleportationFailed,
                        onTeleportationProcessFinished
                ),
                1L,
                UHConfig.START.SLOW.DELAY_BETWEEN_TP.get() * 20L
            );
        }
    }

    /**
     * Cleanups the cages left by the teleportation process, to be executed when the game really starts.
     */
    public void cleanup()
    {
        for (final Cage nicolas : cages.values()) // sorry
            nicolas.destroy();
    }
}
