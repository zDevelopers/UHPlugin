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
package me.azenet.UHPlugin.borders.shapes;

import org.bukkit.Location;
import org.bukkit.World;


public class SquaredMapShape implements MapShapeDescriptor {

	/**
	 * Returns true if the given location is inside the map.
	 *
	 * @param location The location to check.
	 * @param diameter The diameter of the map.
	 * @param center   The center of the map.
	 *
	 * @return {@code true} if the given location is inside the map.
	 */
	@Override
	public boolean isInsideBorder(final Location location, final Double diameter, final Location center) {
		Integer halfMapSize = (int) Math.floor(diameter/2);
		Integer x = location.getBlockX();
		Integer z = location.getBlockZ();

		Integer limitXInf = center.clone().add(-halfMapSize, 0, 0).getBlockX();
		Integer limitXSup = center.clone().add(halfMapSize, 0, 0) .getBlockX();
		Integer limitZInf = center.clone().add(0, 0, -halfMapSize).getBlockZ();
		Integer limitZSup = center.clone().add(0, 0, halfMapSize) .getBlockZ();

		return !(x < limitXInf || x > limitXSup || z < limitZInf || z > limitZSup);
	}

	/**
	 * Returns the distance between the given location and the border with this diameter.
	 *
	 * @param location The distance will be calculated between this location and the closest point of the border.
	 * @param diameter The diameter of the border.
	 * @param center   The center of the border.
	 *
	 * @return The distance between the given {@code location} and the closest point of the border.<br />
	 * {@code -1} if the location is inside the border.
	 */
	@Override
	public double getDistanceToBorder(final Location location, final Double diameter, final Location center) {
		if(!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) { // The nether/end are not limited.
			return -1;
		}

		if(isInsideBorder(location, diameter, center)) {
			return -1;
		}

		Integer halfMapSize = (int) Math.floor(diameter/2);
		Integer x = location.getBlockX();
		Integer z = location.getBlockZ();

		Integer limitXInf = center.clone().add(-halfMapSize, 0, 0).getBlockX();
		Integer limitXSup = center.clone().add(halfMapSize, 0, 0) .getBlockX();
		Integer limitZInf = center.clone().add(0, 0, -halfMapSize).getBlockZ();
		Integer limitZSup = center.clone().add(0, 0, halfMapSize) .getBlockZ();

		if(x > limitXSup && z < limitZSup && z > limitZInf) { // East of the border
			return Math.abs(x - limitXSup);
		}
		else if(x < limitXInf && z < limitZSup && z > limitZInf) { // West of the border
			return Math.abs(x - limitXInf);
		}
		else if(z > limitZSup && x < limitXSup && x > limitXInf) { // South of the border
			return Math.abs(z - limitZSup);
		}
		else if(z < limitZInf && x < limitXSup && x > limitXInf) { // North of the border
			return Math.abs(z - limitZInf);
		}
		else if(x > limitXSup && z < limitZInf) { // North-East
			return (int) location.distance(new Location(location.getWorld(), limitXSup, location.getBlockY(), limitZInf));
		}
		else if(x > limitXSup && z > limitZSup) { // South-East
			return (int) location.distance(new Location(location.getWorld(), limitXSup, location.getBlockY(), limitZSup));
		}
		else if(x < limitXInf && z > limitZSup) { // South-West
			return (int) location.distance(new Location(location.getWorld(), limitXInf, location.getBlockY(), limitZSup));
		}
		else if(x < limitXInf && z < limitZInf) { // North-West
			return (int) location.distance(new Location(location.getWorld(), limitXInf, location.getBlockY(), limitZInf));
		}
		else {
			return -1; // Should never happen.
		}
	}
}
