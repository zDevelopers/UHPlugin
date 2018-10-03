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
package eu.carrade.amaury.UHCReloaded.misc;

import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.mojang.UUIDFetcher;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class OfflinePlayersLoader extends Worker
{
    private static Map<UUID, OfflinePlayer> offlinePlayers = new HashMap<>();

    /**
     * Returns a list of offline players, including the players who logged in the server and the explicitly
     * loaded players.
     *
     * @return A list of OfflinePlayers.
     */
    public static Set<OfflinePlayer> getOfflinePlayers()
    {
        final Set<OfflinePlayer> players = new HashSet<>(offlinePlayers.values());
        Collections.addAll(players, Bukkit.getOfflinePlayers());

        return Collections.unmodifiableSet(players);
    }

    /**
     * Retrieves an OfflinePlayer by ID.
     *
     * Tries to load a logged-in player, then an explicitly loaded player, then
     * a server-wide OfflinePlayer.
     *
     * @param id The player's UUID.
     * @return An OfflinePlayer. The retrieved object will never be null but may not
     * be loaded with a name and other data if the player never came to the server and
     * was not explicitly loaded.
     */
    public static OfflinePlayer getOfflinePlayer(UUID id)
    {
        OfflinePlayer player = Bukkit.getPlayer(id);
        if (player == null) player = offlinePlayers.get(id);
        if (player == null) player = Bukkit.getOfflinePlayer(id);

        return player;
    }

    /**
     * Retrieves an OfflinePlayer by name.
     *
     * Tries to load a logged-in player, then an explicitly loaded player, then
     * a server-wide OfflinePlayer.
     *
     * @param name The player's name.
     * @return An OfflinePlayer. {@code null} if no player was found with this name.
     */
    public static OfflinePlayer getOfflinePlayer(String name)
    {
        OfflinePlayer player = Bukkit.getOnlinePlayers().stream()
                .filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);

        if (player == null)
        {
            player = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        }

        if (player == null)
        {
            player = offlinePlayers.values().stream()
                    .filter(offlinePlayer -> offlinePlayer.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
        }

        return player;
    }

    /**
     * Loads the given players list in the system, making it available in the players list and to be added in teams.
     *
     * @param pseudonym A pseudonym.
     * @param successCallback A callback called when the process ended. Called with {@code null} if no player was found.
     */
    public static void loadPlayer(final String pseudonym, final Callback<OfflinePlayer> successCallback)
    {
        loadPlayers(Collections.singletonList(pseudonym), retrieved ->
        {
            if (successCallback != null)
            {
                if (retrieved.size() == 1)
                    successCallback.call(retrieved.values().iterator().next());
                else
                    successCallback.call(null);
            }
        });
    }

    /**
     * Loads the given players list in the system, making it available in the players list and to be added in teams.
     *
     * @param pseudonyms A list of pseudonyms.
     * @param callbackSuccess A callback called when the process ended.
     */
    public static void loadPlayers(final List<String> pseudonyms, final Callback<Map<UUID, OfflinePlayer>> callbackSuccess)
    {
        loadPlayers(
                pseudonyms,
                callbackSuccess,
                errors -> PluginLogger.error("Unable to retrieve the following names: {0}", StringUtils.join(errors, ", "))
        );
    }

    /**
     * Loads the given players list in the system, making it available in the players list
     * and to be added in teams.
     *
     * Only works in online mode. In offline mode, the already known players will be sent
     * to the success callback and the others to the errors one. Use {@link UUIDFetcher}
     * directly if needed.
     *
     * @param pseudonyms A list of pseudonyms.
     * @param callbackSuccess A callback called when the process ended.
     */
    public static void loadPlayers(final List<String> pseudonyms, final Callback<Map<UUID, OfflinePlayer>> callbackSuccess, final Callback<List<String>> callbackErrors)
    {
        final List<String> toRetrieve = new ArrayList<>(pseudonyms);
        final Map<UUID, OfflinePlayer> alreadyKnown = new HashMap<>();

        for (String pseudonym : pseudonyms)
        {
            OfflinePlayer player = getOfflinePlayer(pseudonym);
            if (player != null)
            {
                alreadyKnown.put(player.getUniqueId(), player);
                toRetrieve.remove(pseudonym);
            }
        }

        if (toRetrieve.size() == 0)
        {
            if (callbackSuccess != null) callbackSuccess.call(alreadyKnown);
            return;
        }

        // If the server is in offline mode, we don't even try to load the players, as they will
        // not be valid and be unusable.
        if (!Bukkit.getOnlineMode())
        {
            if (callbackSuccess != null) callbackSuccess.call(alreadyKnown);
            if (callbackErrors != null && toRetrieve.size() > 0) callbackErrors.call(toRetrieve);

            return;
        }

        submitQuery(new WorkerRunnable<Map<String, UUID>>()
        {
            @Override
            public Map<String, UUID> run() throws Throwable
            {
                final Map<String, UUID> uuids = UUIDFetcher.fetch(toRetrieve);
                UUIDFetcher.fetchRemaining(toRetrieve, uuids);

                return uuids;
            }
        }, new WorkerCallback<Map<String, UUID>>()
        {
            @Override
            public void finished(final Map<String, UUID> result)
            {
                final Map<UUID, OfflinePlayer> added = new HashMap<>(alreadyKnown);
                final Class<?> gameProfileClass;

                try
                {
                    gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                }
                catch (ClassNotFoundException e)
                {
                    PluginLogger.error("Cannot load GameProfile class required to load OfflinePlayers.", e);
                    return;
                }

                for (Map.Entry<String, UUID> playerProfile : result.entrySet())
                {
                    final String name = playerProfile.getKey();
                    final UUID uuid = playerProfile.getValue();

                    if (uuid == null)
                    {
                        PluginLogger.error("Unable to load the player {0}, skipping.", name);
                        continue;
                    }

                    try
                    {
                        final Object profile = Reflection.instantiate(gameProfileClass, uuid, name);
                        final OfflinePlayer player = (OfflinePlayer) Reflection.call(Bukkit.getServer(), "getOfflinePlayer", profile);

                        offlinePlayers.put(uuid, player);
                        added.put(uuid, player);
                    }
                    catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
                    {
                        PluginLogger.error("Unable to load the player {0}, skipping.", e, playerProfile.getKey());
                    }
                }

                if (callbackSuccess != null) callbackSuccess.call(added);

                if (callbackErrors != null)
                {
                    final List<String> notRetrieved = toRetrieve.stream()
                            .filter(pseudonym -> !result.keySet().contains(pseudonym))
                            .collect(Collectors.toList());

                    if (notRetrieved.size() > 0) callbackErrors.call(notRetrieved);
                }
            }

            @Override
            public void errored(Throwable exception)
            {
                PluginLogger.error("Unable to load players", exception);
            }
        });
    }
}
