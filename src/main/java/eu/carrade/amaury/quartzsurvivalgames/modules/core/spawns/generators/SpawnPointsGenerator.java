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

import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.exceptions.CannotGenerateSpawnPointsException;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;


/**
 * Represents a spawn points generator.
 *
 * <p>
 * A zero-arguments constructor is needed.
 * </p>
 */
public interface SpawnPointsGenerator {
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
     * @return The spawn points generated.
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    Set<Location> generate(final World world, final int spawnCount, final int regionDiameter,
                           final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter,
                           final boolean avoidWater) throws CannotGenerateSpawnPointsException;
}
