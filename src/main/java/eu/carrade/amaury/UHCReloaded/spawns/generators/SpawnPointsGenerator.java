/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.spawns.generators;

import eu.carrade.amaury.UHCReloaded.spawns.exceptions.CannotGenerateSpawnPointsException;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;


/**
 * Represents a spawn points generator.
 *
 * <p>
 *     A zero-arguments constructor is needed.
 * </p>
 */
public interface SpawnPointsGenerator
{
    /**
     * Generates the spawn points.
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
    Set<Location> generate(World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter, boolean avoidWater) throws CannotGenerateSpawnPointsException;
}
