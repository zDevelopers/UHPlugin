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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.generators;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class CircularSpawnPointsGenerator implements SpawnPointsGenerator
{
    private final BorderModule borderModule = QSG.module(BorderModule.class);

    /**
     * Generates spawn points in concentric circles.
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
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    @Override
    public Set<Location> generate(final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter, final boolean avoidWater) throws CannotGenerateSpawnPointsException
    {
        // We starts the generation on a smaller grid, to avoid false outside tests if the point is on the edge
        final int usedRegionDiameter = regionDiameter - 1;

        int countGeneratedPoints = 0;
        final Set<Location> generatedPoints = new HashSet<>();

        int currentCircleDiameter = usedRegionDiameter;

        // The generation loop. Each step generates a circle.
        generationLoop:
        while (currentCircleDiameter >= minimalDistanceBetweenTwoPoints)
        {
            // First step. We want to know if all the points left can be in one circle.
            // We calculates the maximal number of points in a circle, taking into account the
            // minimal distance between two points.

            // The link between the angle between two points and the fly distance between them
            // is, where R is the radius, d the fly distance, and a the angle:
            // a = 2 Arcsin((d/2)/R)
            // (Just draw the situation, you'll see.)

            final double denseCircleAngle = 2 * Math.asin(((double) minimalDistanceBetweenTwoPoints / 2) / ((double) currentCircleDiameter / 2));
            final int pointsPerDenseCircles = (int) Math.floor(2 * Math.PI / denseCircleAngle);

            final double angleBetweenTwoPoints;

            // Not all the points can be in this circle. We generate the densest circle.
            if (pointsPerDenseCircles < spawnCount - countGeneratedPoints)
            {
                angleBetweenTwoPoints = 2 * Math.PI / ((double) pointsPerDenseCircles);
            }

            // All the remaining points can be in this circle. We generates the less dense circle with
            // these points.
            else
            {
                angleBetweenTwoPoints = 2 * Math.PI / ((double) (spawnCount - countGeneratedPoints));
            }

            // Let's generate these points.
            final double startAngle = (new Random()).nextDouble() * 2 * Math.PI;
            double currentAngle = startAngle;

            while (currentAngle <= 2 * Math.PI - angleBetweenTwoPoints + startAngle)
            {
                // The coordinates of a point in the circle.
                // Cf. your trigonometry! ;)
                Location point = new Location(
                        world,
                        (currentCircleDiameter / 2) * Math.cos(currentAngle) + xCenter,
                        0,
                        (currentCircleDiameter / 2) * Math.sin(currentAngle) + zCenter
                );

                currentAngle += angleBetweenTwoPoints;

                // Just in case
                if (!borderModule.isInsideBorder(point, regionDiameter))
                {
                    continue;
                }

                final Block surfaceAirBlock = world.getHighestBlockAt(point);
                final Block surfaceBlock = surfaceAirBlock.getRelative(BlockFace.DOWN);

                // Safe spot available?
                if ((world.getEnvironment() == World.Environment.NORMAL || world.getEnvironment() == World.Environment.THE_END) && !QSGUtils
                        .isSafeSpot(surfaceAirBlock.getLocation())
                        || QSGUtils.searchSafeSpot(point) == null)
                {
                    continue; // not safe: nope
                }

                // Not above the water?
                if (avoidWater)
                {
                    if (surfaceBlock.getType() == Material.WATER || surfaceBlock.getType() == Material.WATER)
                    {
                        continue;
                    }
                }

                generatedPoints.add(point);
                countGeneratedPoints++;

                if (countGeneratedPoints >= spawnCount)
                {
                    break generationLoop;
                }
            }

            // So, this circle is done.
            // We prepares the next one.
            currentCircleDiameter -= 2 * minimalDistanceBetweenTwoPoints;
        }


        // Generation done or failed (not enough space)?
        if (generatedPoints.size() < spawnCount)
        {
            // Failed!
            throw new CannotGenerateSpawnPointsException("Cannot generate the spawn point in circles: not enough space");
        }
        else
        {
            return generatedPoints;
        }
    }
}
