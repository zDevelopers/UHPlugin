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

package eu.carrade.amaury.UHCReloaded.spawns;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.UHCReloaded.spawns.exceptions.UnknownGeneratorException;
import eu.carrade.amaury.UHCReloaded.spawns.generators.SpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class SpawnsManager
{
    private final boolean AVOID_WATER;

    UHCReloaded p = null;
    I18n i = null;

    private LinkedList<Location> spawnPoints = new LinkedList<Location>();


    public SpawnsManager(UHCReloaded plugin)
    {
        this.p = plugin;
        this.i = p.getI18n();

        AVOID_WATER = p.getConfig().getBoolean("map.spawnPoints.dontGenerateAboveWater");
    }

    /**
     * Adds a spawn point at (x;z) in the default world.
     *
     * @param x The X coordinate.
     * @param z The Z coordinate.
     */
    public void addSpawnPoint(final Double x, final Double z)
    {
        addSpawnPoint(p.getServer().getWorlds().get(0), x, z);
    }

    /**
     * Adds a spawn point at (x;z) in the given world.
     *
     * @param world The world.
     * @param x     The X coordinate.
     * @param z     The Z coordinate.
     */
    public void addSpawnPoint(final World world, final Double x, final Double z)
    {
        addSpawnPoint(new Location(world, x, 0, z));
    }

    /**
     * Adds a spawn point from a location.
     *
     * @param location The location. Cloned, so you can use the same location object with
     *                 modifications between two calls.
     *
     * @throws RuntimeException         If the spawn point is in the Nether and no safe spot was
     *                                  found.
     * @throws IllegalArgumentException If the spawn point is out of the current border.
     */
    public void addSpawnPoint(final Location location)
    {
        Location spawnPoint = location.clone();

        // Initial fall, except in the nether.
        if (!(spawnPoint.getWorld().getEnvironment() == Environment.NETHER))
        {
            spawnPoint.setY(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 120);
        }
        else
        {
            Location safeSpot = UHUtils.searchSafeSpot(location);
            if (safeSpot == null)
            {
                throw new RuntimeException("Unable to find a safe spot to set the spawn point " + location.toString());
            }

            spawnPoint.setY(safeSpot.getY());
        }

        if (!p.getBorderManager().isInsideBorder(spawnPoint))
        {
            throw new IllegalArgumentException("The given spawn location is outside the current border");
        }

        spawnPoints.add(spawnPoint);
    }

    /**
     * Returns the registered spawn points.
     *
     * @return The spawn points.
     */
    public List<Location> getSpawnPoints()
    {
        return spawnPoints;
    }

    /**
     * Removes all spawn points with the same coordinates as the given location object (X, Z,
     * world).
     *
     * @param location The location to be removed.
     * @param precise  If true, only the spawn points at the exact same location will be removed.
     *                 Else, the points in the same block.
     *
     * @return true if something were removed.
     */
    public boolean removeSpawnPoint(Location location, boolean precise)
    {
        List<Location> toRemove = new LinkedList<>();

        for (Location spawn : getSpawnPoints())
        {
            if (location.getWorld().equals(spawn.getWorld()))
            {
                if (precise
                        && location.getX() == spawn.getX()
                        && location.getZ() == spawn.getZ())
                {
                    toRemove.add(spawn);
                }
                else if (!precise
                        && location.getBlockX() == spawn.getBlockX()
                        && location.getBlockZ() == spawn.getBlockZ())
                {
                    toRemove.add(spawn);
                }
            }
        }

        for (Location spawnToRemove : toRemove)
        {
            // Used to remove all occurrences of the spawn point
            while (spawnPoints.remove(spawnToRemove))
                ;
        }

        return toRemove.size() != 0;
    }

    /**
     * Removes all registered spawn points.
     *
     * CANNOT BE CANCELLED.
     */
    public void reset()
    {
        spawnPoints = new LinkedList<Location>();
    }


    /**
     * Imports spawn points from the configuration.
     *
     * @return The number of spawn points imported.
     */
    public int importSpawnPointsFromConfig()
    {
        if (p.getConfig().getList("spawnpoints") != null)
        {
            int spawnCount = 0;
            for (Object position : p.getConfig().getList("spawnpoints"))
            {
                if (position instanceof String && !((String) position).isEmpty())
                {
                    String[] coords = ((String) position).split(",");
                    try
                    {
                        addSpawnPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
                        p.getLogger().info(i.t("load.spawnPointAdded", coords[0], coords[1]));
                        spawnCount++;
                    }
                    catch (Exception e) // Not an integer or not enough coords
                    {
                        p.getLogger().warning(i.t("load.invalidSpawnPoint", (String) position));
                    }
                }
            }

            return spawnCount;
        }

        return 0;
    }


    /**
     * Generates spawn points with the given generator.
     *
     * @param generatorName                   The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     *
     * @throws CannotGenerateSpawnPointsException In case of fail.
     * @throws UnknownGeneratorException          If no generator was found by the given name.
     */
    public void generateSpawnPoints(String generatorName, World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter) throws CannotGenerateSpawnPointsException, UnknownGeneratorException
    {
        Generator generator = Generator.fromString(generatorName);
        if (generator != null)
        {
            generateSpawnPoints(generator, world, spawnCount, regionDiameter, minimalDistanceBetweenTwoPoints, xCenter, zCenter);
        }
        else
        {
            throw new UnknownGeneratorException("The generator '" + generatorName + "' does not exists.");
        }
    }

    /**
     * Generates spawn points with the given generator.
     *
     * @param generator                       The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     *
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    public void generateSpawnPoints(Generator generator, World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter) throws CannotGenerateSpawnPointsException
    {
        generateSpawnPoints(generator.getInstance(), world, spawnCount, regionDiameter, minimalDistanceBetweenTwoPoints, xCenter, zCenter);
    }

    /**
     * Generates spawn points with the given generator.
     *
     * @param generator                       The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     *
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    public void generateSpawnPoints(SpawnPointsGenerator generator, World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter) throws CannotGenerateSpawnPointsException
    {
        Set<Location> spawnPoints = generator.generate(world, spawnCount, regionDiameter, minimalDistanceBetweenTwoPoints, xCenter, zCenter, AVOID_WATER);

        for (Location spawn : spawnPoints)
        {
            addSpawnPoint(spawn);
        }
    }
}
