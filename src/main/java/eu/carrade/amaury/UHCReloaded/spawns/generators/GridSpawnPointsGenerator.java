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
package eu.carrade.amaury.UHCReloaded.spawns.generators;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.borders.MapShape;
import eu.carrade.amaury.UHCReloaded.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;


public class GridSpawnPointsGenerator implements SpawnPointsGenerator
{
    private final UHCReloaded p = UHCReloaded.get();

    /**
     * Generates spawn points in a grid.
     *
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
     * @param avoidWater                      True if the generation have to avoid the water.
     *
     * @return The spawn points generated.
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    @Override
    public Set<Location> generate(final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter, final boolean avoidWater) throws CannotGenerateSpawnPointsException
    {
        // We starts the generation on a smaller grid, to avoid false outside tests if the point is on the edge
        final int usedRegionDiameter = regionDiameter - 1;

        // To check the possibility of the generation, we calculates the maximal number of columns/rows
        // possible, based on the minimal distance between two points.
        final int maxColumnsCount = (int) Math.ceil(usedRegionDiameter / minimalDistanceBetweenTwoPoints);

        // The points are on a grid
        int neededColumnsCount = (int) Math.ceil(Math.sqrt(spawnCount));
        if (p.getBorderManager().getMapShape() == MapShape.CIRCULAR)
        {
            // If the border is circular, the distance between two points needs to be decreased.
            // The space available is divided by PI/4, so the column count is multiplied by
            // this number.
            neededColumnsCount = (int) Math.ceil(neededColumnsCount / (Math.PI / 4));
        }

        // IS impossible.
        if (neededColumnsCount > maxColumnsCount)
        {
            throw new CannotGenerateSpawnPointsException("Cannot generate spawn points on a grid: not enough space.");
        }

        // If the map is circular, the generation may be impossible, because this check was
        // performed for a squared map.
        // The test will be done after the generation.


        // We generates the points on a grid in squares, starting by the biggest square.
        final int distanceBetweenTwoPoints = (int) ((double) usedRegionDiameter / ((double) (neededColumnsCount)));

        // Check related to the case the column count was increased.
        if (distanceBetweenTwoPoints < minimalDistanceBetweenTwoPoints)
        {
            throw new CannotGenerateSpawnPointsException("Cannot generate spawn points on a grid: not enough space.");
        }


        int countGeneratedPoints = 0;
        final HashSet<Location> generatedPoints = new HashSet<>();

        final int halfDiameter = (int) Math.floor(usedRegionDiameter / 2);

        Integer currentSquareSize = usedRegionDiameter;
        Location currentSquareStartPoint = new Location(world, xCenter + halfDiameter, 0, zCenter - halfDiameter);
        Location currentPoint;

        // Represents the location to add on each side of the squares
        final Location[] addOnSide = new Location[4];
        addOnSide[0] = new Location(world, -distanceBetweenTwoPoints, 0, 0); // North side, direction east
        addOnSide[1] = new Location(world, 0, 0, distanceBetweenTwoPoints);  // East side,  direction south
        addOnSide[2] = new Location(world, distanceBetweenTwoPoints, 0, 0);  // South side, direction west
        addOnSide[3] = new Location(world, 0, 0, -distanceBetweenTwoPoints); // West side,  direction north

        // We generates the points until there isn't any point left to place. The loop will be broken.
        // On each step of this loop, a square is generated.
        generationLoop:
        while (true)
        {
            currentPoint = currentSquareStartPoint.clone();

            // First point
            if (p.getBorderManager().isInsideBorder(currentPoint, regionDiameter) && UHUtils.searchSafeSpot(currentPoint) != null)
            {
                generatedPoints.add(currentPoint.clone());
                countGeneratedPoints++;

                if (countGeneratedPoints >= spawnCount)
                {
                    break;
                }
            }

            // A step for each side, j is the side (see addOnSide).
            for (int j = 0; j < 4; j++)
            {
                int plottedSize = 0;

                while (plottedSize < currentSquareSize)
                {
                    currentPoint.add(addOnSide[j]);
                    plottedSize += distanceBetweenTwoPoints;

                    // Inside the border?
                    if (!p.getBorderManager().isInsideBorder(currentPoint, regionDiameter))
                    {
                        continue;
                    }

                    final Block surfaceAirBlock = world.getHighestBlockAt(currentPoint);
                    final Block surfaceBlock = surfaceAirBlock.getRelative(BlockFace.DOWN);

                    // Safe spot available?
                    if (!UHUtils.isSafeSpot(surfaceAirBlock.getLocation()))
                    {
                        continue; // not safe: nope
                    }

                    // Not above the water?
                    if (avoidWater)
                    {
                        if (surfaceBlock.getType() == Material.WATER || surfaceBlock.getType() == Material.STATIONARY_WATER)
                        {
                            continue;
                        }
                    }

                    generatedPoints.add(currentPoint.clone());
                    countGeneratedPoints++;

                    if (countGeneratedPoints >= spawnCount)
                    {
                        break generationLoop;
                    }
                }
            }

            // This square is complete; preparing the next one...
            currentSquareSize -= 2 * distanceBetweenTwoPoints;
            currentSquareStartPoint.add(new Location(world, -distanceBetweenTwoPoints, 0, distanceBetweenTwoPoints));

            if (currentSquareSize < distanceBetweenTwoPoints)
            {
                // This may happens if we generates the points for a circular world
                break;
            }
        }

        // If the generation was broken (circular world, not enough positions),
        // the generation was incomplete.
        if (countGeneratedPoints >= spawnCount)
        {
            // Generation OK
            return generatedPoints;
        }
        else
        {
            throw new CannotGenerateSpawnPointsException("Cannot generate the spawn points: not enough space.");
        }
    }
}
