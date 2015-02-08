/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package me.azenet.UHPlugin.spawns.generators;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.utils.UHUtils;
import me.azenet.UHPlugin.spawns.exceptions.CannotGenerateSpawnPointsException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class CircularSpawnPointsGenerator implements SpawnPointsGenerator {

    private UHPlugin p;

	public CircularSpawnPointsGenerator(UHPlugin p) {
		this.p = p;
	}


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
     * @throws me.azenet.UHPlugin.spawns.exceptions.CannotGenerateSpawnPointsException In case of fail.
     */
    @Override
    public Set<Location> generate(World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter, boolean avoidWater) throws CannotGenerateSpawnPointsException {

        // We starts the generation on a smaller grid, to avoid false outside tests if the point is on the edge
        int usedRegionDiameter = regionDiameter - 1;

        int countGeneratedPoints = 0;
        Set<Location> generatedPoints = new HashSet<Location>();

        int currentCircleDiameter = usedRegionDiameter;

        // The generation loop. Each step generates a circle.
        generationLoop: while(currentCircleDiameter >= minimalDistanceBetweenTwoPoints) {
            // First step. We want to know if all the points left can be in one circle.
            // We calculates the maximal number of points in a circle, taking into account the
            // minimal distance between two points.

            // The link between the angle between two points and the fly distance between them
            // is, where R is the radius, d the fly distance, and a the angle:
            // a = 2 Arcsin((d/2)/R)
            // (Just draw the situation, you'll see.)

            double denseCircleAngle = 2 * Math.asin(((double) minimalDistanceBetweenTwoPoints / 2) / ((double) currentCircleDiameter / 2));
            int pointsPerDenseCircles = (int) Math.floor(2 * Math.PI / denseCircleAngle);

            double angleBetweenTwoPoints;

            // Not all the points can be in this circle. We generate the densiest circle.
            if(pointsPerDenseCircles < spawnCount - countGeneratedPoints) {
                angleBetweenTwoPoints = 2 * Math.PI / ((double) pointsPerDenseCircles);
            }
            // All the remaining points can be in this circle. We generates the less dense circle with
            // these points.
            else {
                angleBetweenTwoPoints = 2 * Math.PI / ((double) (spawnCount - countGeneratedPoints));
            }

            // Let's generate these points.
            double startAngle   = (new Random()).nextDouble() * 2 * Math.PI;
            double currentAngle = startAngle;

            circleLoop: while(currentAngle <= 2 * Math.PI - angleBetweenTwoPoints + startAngle) {
                // The coordinates of a point in the circle.
                // Cf. your trigonometry! ;)
                Location point = new Location(
                        world,
                        (currentCircleDiameter / 2) * Math.cos(currentAngle) + xCenter,
                        0,
                        (currentCircleDiameter / 2) * Math.sin(currentAngle) + zCenter
                );

                currentAngle += angleBetweenTwoPoints;

                if(!p.getBorderManager().isInsideBorder(point, regionDiameter)) { // Just in case
                    continue circleLoop;
                }

                Block surfaceBlock = world.getHighestBlockAt(point);

                // Safe spot available?
                if(!UHUtils.isSafeSpot(surfaceBlock.getLocation())) {
                    continue circleLoop; // not safe: nope
                }

                // Not above the water?
                if(avoidWater) {
                    if(surfaceBlock.getType() == Material.WATER || surfaceBlock.getType() == Material.STATIONARY_WATER) {
                        continue circleLoop;
                    }
                }

                generatedPoints.add(point);
                countGeneratedPoints++;

                if(countGeneratedPoints >= spawnCount) {
                    break generationLoop;
                }
            }

            // So, this circle is done.
            // We prepares the next one.
            currentCircleDiameter -= 2 *minimalDistanceBetweenTwoPoints;
        }


        // Generation done or failed (not enough space)?
        if(generatedPoints.size() < spawnCount) {
            // Failed!
            throw new CannotGenerateSpawnPointsException("Cannot generate the spawn point in circles: not enough space");
        }

        else {
            return generatedPoints;
        }
    }
}
