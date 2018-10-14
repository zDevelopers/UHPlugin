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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;


public class SpawnsListener implements Listener
{
    private final Random random = new Random();

    private final boolean RABBIT_SPAWN_ENABLED;
    private final double RABBIT_SPAWN_PROBABILITY;
    private final String RABBIT_NAME;

    public SpawnsListener()
    {
        RABBIT_SPAWN_ENABLED = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_SPAWN.get();
        RABBIT_SPAWN_PROBABILITY = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_SPAWN_PROBABILITY.get();

        RABBIT_NAME = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_NAME.get().trim();
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRabbitSpawn(CreatureSpawnEvent ev)
    {
        if (!RABBIT_SPAWN_ENABLED)
            return;

        if (ev.getEntity().getType() != EntityType.RABBIT)
            return;

        if (random.nextDouble() >= RABBIT_SPAWN_PROBABILITY)
            return;

        Rabbit rabbit = (Rabbit) ev.getEntity();
        rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);

        if (!RABBIT_NAME.isEmpty())
        {
            rabbit.setCustomName(RABBIT_NAME);
            rabbit.setCustomNameVisible(true);
        }
    }


    /**
     * Used to cancel the spawn of the creatures if the game is not started.
     * <p>
     * We don't use the peaceful difficulty for that because it causes bugs with Minecraft 1.8
     * (the difficulty is not correctly updated client-side when the game starts).
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent ev)
    {
        if (!UHCReloaded.get().getGameManager().isGameStarted()
                && isNaturalSpawn(ev.getSpawnReason())
                && isHostile(ev.getEntityType()))
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to cancel the spawn of hostile entities on the surface only, at the beginning of the game.
     */
    @EventHandler
    public void onSurfaceCreatureSpawn(CreatureSpawnEvent ev)
    {
        if (UHCReloaded.get().getGameManager().isGameStarted()
                && !UHCReloaded.get().getGameManager().isSurfaceSpawnEnabled()
                && isNaturalSpawn(ev.getSpawnReason())
                && isHostile(ev.getEntityType()))
        {
            // We check the blocs above the entity to see if we only find surface blocks.
            final Location spawnLocation = ev.getLocation();
            final World world = spawnLocation.getWorld();
            final int highestBlockY = world.getHighestBlockYAt(spawnLocation);

            final int x = spawnLocation.getBlockX();
            final int z = spawnLocation.getBlockZ();

            boolean surface = true;

            for (int y = spawnLocation.getBlockY(); y <= highestBlockY; y++)
            {
                switch (world.getBlockAt(x, y, z).getType())
                {
                    // Air
                    case AIR:

                    // Trees
                    case LOG:
                    case LOG_2:
                    case LEAVES:
                    case LEAVES_2:
                    case HUGE_MUSHROOM_1:
                    case HUGE_MUSHROOM_2:

                    // Vegetation
                    case DEAD_BUSH:
                    case CROPS:
                    case GRASS:
                    case LONG_GRASS:
                    case DOUBLE_PLANT:
                    case YELLOW_FLOWER:
                    case VINE:
                    case SUGAR_CANE_BLOCK:
                    case BROWN_MUSHROOM:
                    case RED_MUSHROOM:

                    // Nature
                    case SNOW:

                    // Igloos
                    case SNOW_BLOCK:

                    // Villages
                    case WOOD:
                    case WOOD_STAIRS:
                    case SANDSTONE_STAIRS:
                    case BOOKSHELF:

                    // Redstone
                    case REDSTONE_WIRE:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_OFF:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_TORCH_OFF:
                    case REDSTONE_TORCH_ON:

                    // Other blocs frequently used on surface on custom maps
                    case TORCH:
                    case RAILS:
                    case ACTIVATOR_RAIL:
                    case DETECTOR_RAIL:
                    case POWERED_RAIL:
                        break;

                    default:
                        surface = false;
                }

                if (!surface) break;
            }

            if (surface) ev.setCancelled(true);
        }
    }


    /**
     * Checks if a spawn is natural.
     *
     * @param reason The spawn reason.
     * @return {@code true} if it's a natural spawn (not from a player or an interaction
     * with another entity, as example).
     */
    private boolean isNaturalSpawn(CreatureSpawnEvent.SpawnReason reason)
    {
        switch (reason)
        {
            case NATURAL:
            case NETHER_PORTAL:
            case LIGHTNING:
            case SPAWNER:
                return true;

            default:
                return false;
        }
    }

    /**
     * Checks if the given mod is hostile.
     * @param entity The entity.
     * @return {@code true} if hostile.
     */
    private boolean isHostile(EntityType entity)
    {
        Class<? extends Entity> entityClass = entity.getEntityClass();

        return Monster.class.isAssignableFrom(entityClass)
                || Slime.class.isAssignableFrom(entityClass)
                || Ghast.class.isAssignableFrom(entityClass);
    }
}
