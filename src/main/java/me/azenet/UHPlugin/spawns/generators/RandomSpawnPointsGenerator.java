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
import me.azenet.UHPlugin.borders.MapShape;
import me.azenet.UHPlugin.spawns.exceptions.CannotGenerateSpawnPointsException;
import me.azenet.UHPlugin.utils.UHUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Generates the spawn points randomly.
 */
public class RandomSpawnPointsGenerator implements SpawnPointsGenerator {

	private UHPlugin p;

	public RandomSpawnPointsGenerator(UHPlugin p) {
		this.p = p;
	}

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
	public Set<Location> generate(World world, int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints, double xCenter, double zCenter, boolean avoidWater) throws CannotGenerateSpawnPointsException {
		/** Possible? **/

		// If the surface of the map is too close of the sum of the surfaces of the private part
		// around each spawn point (a circle with, as radius, the minimal distance between two spawn
		// points), the generation will fail.

		double surfacePrivatePartsAroundSpawnPoints = (int) (spawnCount * (Math.PI * Math.pow(minimalDistanceBetweenTwoPoints, 2)));
		double surfaceRegion;
		if(p.getBorderManager().getMapShape() == MapShape.CIRCULAR) {
			surfaceRegion = (Math.PI * Math.pow(regionDiameter, 2)) / 4;
		}
		else {
			surfaceRegion = Math.pow(regionDiameter, 2);
		}

		Double packingDensity = surfacePrivatePartsAroundSpawnPoints / surfaceRegion;

		// According to Lagrange and Thue's works on circle packagings, the highest density possible is
		// approximately 0.9069 (with an hexagonal arrangement of the circles).
		// Even with a packaging density very close to this limit, the generation time is correct.
		// So we uses this as a limit.
		if(packingDensity >= 0.9069) {
			throw new CannotGenerateSpawnPointsException("Unable to generate spawn points randomly: packing density too high");
		}

		/** Generation **/

		Set<Location> randomSpawnPoints = new HashSet<Location>();
		int generatedSpawnPoints = 0;

		// If the first points are badly located, and if the density is high, the generation may
		// be impossible to end.
		// So, after 15 generation fails of a point due to this point being placed too close
		// of other ones, we restarts all the generation.
		int currentErrorCount = 0;

		// With the "avoid above water" option, if there's a lot of water, the genaration may
		// fail even if the surface seems to be ok to host the requested spawn points.
		// So, after 2*{points requested} points above the water, we cancels the generation.
		int pointsAboveWater = 0;

		generationLoop: while(generatedSpawnPoints != spawnCount) {

			// "Too many fails" test
			if(currentErrorCount >= 16) { // restart
				randomSpawnPoints = new HashSet<Location>();
				generatedSpawnPoints = 0;
				currentErrorCount = 0;
			}

			// "Too many points above the water" test
			if(pointsAboveWater >= 2*spawnCount) {
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
			if(!p.getBorderManager().isInsideBorder(randomPoint, regionDiameter)) {
				continue generationLoop; // outside: nope
			}

			Block surfaceBlock = world.getHighestBlockAt(randomPoint);

			// Safe spot available?
			if(!UHUtils.isSafeSpot(surfaceBlock.getLocation())) {
				continue generationLoop; // not safe: nope
			}

			// Not above the water?
			if(avoidWater) {
				if(surfaceBlock.getType() == Material.WATER || surfaceBlock.getType() == Material.STATIONARY_WATER) {
					pointsAboveWater++;
					continue generationLoop;
				}
			}

			// Is that point at a correct distance of the other ones?
			for(Location spawn : randomSpawnPoints) {
				if(spawn.distance(randomPoint) < minimalDistanceBetweenTwoPoints) {
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
	public Integer random(int min, int max) {
		if(min == max) {
			return min;
		}

		Random rand = new Random();

		if(min > max) { // swap
			min = min + max;
			max = min - max;
			min = min - max;
		}

		if(min >= 0 && max >= 0) {
			return rand.nextInt(max - min + 1) + min;
		}
		else if(min <= 0 && max <= 0) {
			return -1 * (rand.nextInt(Math.abs(min - max)) + Math.abs(max));
		}
		else { // min <= 0 && max >= 0
			return rand.nextInt(Math.abs(min) + Math.abs(max)) - Math.abs(min);
		}
	}
}
