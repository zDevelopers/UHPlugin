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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.border.shapes;

import org.bukkit.Location;
import org.bukkit.World;


public class CircularMapShape implements MapShapeDescriptor {
    /**
     * Returns true if the given location is inside the map.
     *
     * @param location The location to check.
     * @param diameter The diameter of the map.
     * @param center   The center of the map.
     * @return {@code true} if the given location is inside the map.
     */
    @Override
    public boolean isInsideBorder(final Location location, final Double diameter, final Location center) {
        final Location centerRef = center.clone();
        centerRef.setY(location.getY());

        return (location.distance(centerRef) <= Math.floor(diameter / 2));
    }

    /**
     * Returns the distance between the given location and the border with this diameter.
     *
     * @param location The distance will be calculated between this location and the closest point of the border.
     * @param diameter The diameter of the border.
     * @param center   The center of the border.
     * @return The distance between the given {@code location} and the closest point of the border.<br />
     * {@code -1} if the location is inside the border.
     */
    @Override
    public double getDistanceToBorder(final Location location, final Double diameter, final Location center) {
        // The nether/end are not limited.
        if (!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            return -1;
        }

        if (isInsideBorder(location, diameter, center)) {
            return -1;
        }

        final Location centerRef = center.clone();
        centerRef.setY(location.getY());

        return (location.distance(centerRef) - Math.floor(diameter / 2));
    }
}
