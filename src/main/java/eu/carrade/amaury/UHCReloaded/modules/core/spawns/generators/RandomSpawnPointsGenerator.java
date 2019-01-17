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

package eu.carrade.amaury.UHCReloaded.modules.core.spawns.generators;

import eu.carrade.amaury.UHCReloaded.modules.core.border.BorderModule;
import eu.carrade.amaury.UHCReloaded.modules.core.border.MapShape;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


/**
 * Generates the spawn points randomly.
 */
public class RandomSpawnPointsGenerator implements SpawnPointsGenerator
{
    private final Random random = new Random();
    private final BorderModule borderModule = UR.module(BorderModule.class);

    /**
     * Generates randomly some spawn points in the map, with a minimal distance.
     *
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will be generated.<br>
     *                                        This is limited by the size of the map. This will be seen as the diameter of a circular or
     *                                        of a squared map, following the shape of the world set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the region where the points will be generated.
     * @param avoidWater                      True if the generation have to avoid the water.
     *
     * @return The spawn points generated.
     *
     * @throws CannotGenerateSpawnPointsException In case of fail
     */
    @Override
    public Set<Location> generate(final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter, final boolean avoidWater) throws CannotGenerateSpawnPointsException
    {
        final double minimalDistanceBetweenTwoPointsSquared = Math.pow(minimalDistanceBetweenTwoPoints, 2);


        /* *** Possible? *** */

        // If the surface of the map is too close of the sum of the surfaces of the private part
        // around each spawn point (a circle with, as radius, the minimal distance between two spawn
        // points), the generation will fail.

        final double surfacePrivatePartsAroundSpawnPoints = (int) (spawnCount * (Math.PI * minimalDistanceBetweenTwoPointsSquared));
        final double surfaceRegion;

        if (borderModule.getMapShape() == MapShape.CIRCULAR)
        {
            surfaceRegion = (Math.PI * Math.pow(regionDiameter, 2)) / 4;
        }
        else
        {
            surfaceRegion = Math.pow(regionDiameter, 2);
        }

        final double packingDensity = surfacePrivatePartsAroundSpawnPoints / surfaceRegion;

        // According to Lagrange and Thue's works on circles packaging, the highest density possible is
        // approximately 0.9069 (with an hexagonal arrangement of the circles).
        // Even with a packaging density very close to this limit, the generation time is correct.
        // So we uses this as a limit.
        if (packingDensity >= 0.9069)
        {
            throw new CannotGenerateSpawnPointsException("Unable to generate spawn points randomly: packing density of " + packingDensity + " too high");
        }

        /* *** Generation *** */

        final Set<Location> randomSpawnPoints = new HashSet<>();
        int generatedSpawnPoints = 0;

        // If the first points are badly located, and if the density is high, the generation may
        // be impossible to end.
        // So, after 15 generation fails of a point due to this point being placed too close
        // of other ones, we restarts all the generation.
        int currentErrorCount = 0;

        // With the "avoid above water" option, if there's a lot of water, the generation may
        // fail even if the surface seems to be ok to host the requested spawn points.
        // So, after 2*{points requested} points above the water, we cancels the generation.
        int pointsAboveWater = 0;

        generationLoop:
        while (generatedSpawnPoints != spawnCount)
        {
            // "Too many fails" test
            if (currentErrorCount >= 16) // restart
            {
                randomSpawnPoints.clear();
                generatedSpawnPoints = 0;
                currentErrorCount = 0;
            }

            // "Too many points above the water" test
            if (pointsAboveWater >= 2 * spawnCount)
            {
                throw new CannotGenerateSpawnPointsException("Too many spawn points above the water.");
            }


            // We generates a point in the square of side regionDiameter.
            // In case of a circular world, if the point was generated out of the circle, it will be
            // excluded when his presence inside the region will be checked.

            Location randomPoint = new Location(world,
                    random((int) (xCenter - Math.floor(regionDiameter / 2)), (int) (xCenter + (int) Math.floor(regionDiameter / 2))),
                    0,
                    random((int) (zCenter - Math.floor(regionDiameter / 2)), (int) (zCenter + (int) Math.floor(regionDiameter / 2))));

            // Inside the region?
            if (!borderModule.isInsideBorder(randomPoint, regionDiameter))
            {
                continue; // outside: nope
            }

            final Block surfaceAirBlock = world.getHighestBlockAt(randomPoint);
            final Block surfaceBlock = surfaceAirBlock.getRelative(BlockFace.DOWN);

            // Safe spot available?
            if ((world.getEnvironment() == World.Environment.NORMAL || world.getEnvironment() == World.Environment.THE_END) && !UHUtils.isSafeSpot(surfaceAirBlock.getLocation())
                    || UHUtils.searchSafeSpot(randomPoint) == null)
            {
                continue; // not safe: nope
            }

            // Not above the water?
            if (avoidWater)
            {
                if (surfaceBlock.getType() == Material.WATER || surfaceBlock.getType() == Material.STATIONARY_WATER)
                {
                    pointsAboveWater++;
                    continue;
                }
            }

            // Is that point at a correct distance of the other ones?
            for (Location spawn : randomSpawnPoints)
            {
                if (spawn.distanceSquared(randomPoint) < minimalDistanceBetweenTwoPointsSquared)
                {
                    currentErrorCount++;
                    continue generationLoop; // too close: nope
                }
            }

            // Well, all done.
            randomSpawnPoints.add(randomPoint);
            generatedSpawnPoints++;
            currentErrorCount = 0;
        }

        // Generation done, let's register these points.

        return randomSpawnPoints;
    }


    /**
     * Generates a random number between min and max.
     *
     * @param min The minimum value. May be negative. Inclusive.
     * @param max The maximum value. May be negative. Inclusive.
     * @return A random number between these two points.
     */
    public Integer random(int min, int max)
    {
        if (min == max)
        {
            return min;
        }

        if (min > max) // swap
        {
            min = min + max;
            max = min - max;
            min = min - max;
        }

        if (min >= 0 && max >= 0)
        {
            return random.nextInt(max - min + 1) + min;
        }
        else if (min <= 0 && max <= 0)
        {
            return -1 * (random.nextInt(Math.abs(min - max)) + Math.abs(max));
        }
        else // min <= 0 && max >= 0
        {
            return random.nextInt(Math.abs(min) + Math.abs(max)) - Math.abs(min);
        }
    }
}
