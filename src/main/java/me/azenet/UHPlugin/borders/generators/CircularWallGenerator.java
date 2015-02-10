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
package me.azenet.UHPlugin.borders.generators;

import me.azenet.UHPlugin.UHPlugin;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public class CircularWallGenerator extends WallGenerator {

	public CircularWallGenerator(UHPlugin p, Material wallBlockAir, Material wallBlockSolid) {
		super(p, wallBlockAir, wallBlockSolid);
	}

	/**
	 * Builds a wall in the world.
	 *
	 * @param world      The world the wall will be built in.
	 * @param diameter   The diameter of the wall.
	 * @param wallHeight The height of the wall.
	 */
	@Override
	public void build(World world, int diameter, int wallHeight) {
		// Only one quarter of the circle is explicitly set, the other parts are generated
		// following the first quarter.
		// The quarter chosen to be explicitly generated if the one on the South-East,
		// starting at x = xSpawn+radius ; z = zSpawn and ending at x = xSpawn ; z = zSpawn+radius.

		// In each step we gets the three blocks susceptible to be the next block and we calculates the
		// distance from the center to these blocks.
		// The good block if the one with the closest distance to the radius.

		Integer radius = (int) Math.floor(diameter/2);

		Integer xSpawn = world.getSpawnLocation().getBlockX();
		Integer ySpawn = world.getSpawnLocation().getBlockY();
		Integer zSpawn = world.getSpawnLocation().getBlockZ();

		// First block.
		Block currentBlock = world.getBlockAt((int) (xSpawn + radius), ySpawn, zSpawn);

		Block candidate1;
		Block candidate2;
		Block candidate3;

		// Infinite loop broken when the generation is done.
		while(true) {

			// 1) the current point, the symmetries and the opposite point are built.
			this.buildWallPoint(world, currentBlock.getX(), currentBlock.getZ(), wallHeight, diameter);


			// 2) the two candidates are found, except if the build is finished.
			if(currentBlock.getX() == xSpawn) {
				// END
				break;
			}

			candidate1 = world.getBlockAt(currentBlock.getX() - 1, ySpawn, currentBlock.getZ());
			candidate2 = world.getBlockAt(currentBlock.getX() - 1, ySpawn, currentBlock.getZ() + 1);
			candidate3 = world.getBlockAt(currentBlock.getX(), ySpawn, currentBlock.getZ() + 1);


			// 3) The good block is selected
			Double distanceCandidate1ToRef = Math.abs((candidate1.getLocation().distance(world.getSpawnLocation()) - radius));
			Double distanceCandidate2ToRef = Math.abs((candidate2.getLocation().distance(world.getSpawnLocation()) - radius));
			Double distanceCandidate3ToRef = Math.abs((candidate3.getLocation().distance(world.getSpawnLocation()) - radius));

			if(distanceCandidate1ToRef < distanceCandidate2ToRef && distanceCandidate1ToRef < distanceCandidate3ToRef) { // The first is better
				currentBlock = candidate1;
			}
			else if(distanceCandidate2ToRef < distanceCandidate1ToRef && distanceCandidate2ToRef < distanceCandidate3ToRef) { // The second is better
				currentBlock = candidate2;
			}
			else {
				currentBlock = candidate3;
			}
		}
	}


	/**
	 * Builds 4 "towers" of the wall, from y=0 to y=wallHeight, at the given coordinates, and
	 * the symmetric points.
	 *
	 * @param world
	 * @param x
	 * @param z
	 * @param wallHeight
	 * @param diameter
	 */
	private void buildWallPoint(World world, int x, int z, int wallHeight, int diameter) {

		WallPosition positionOriginal;
		WallPosition positionSymmetricX;
		WallPosition positionSymmetricZ;
		WallPosition positionOpposite;

		Integer xSpawn = world.getSpawnLocation().getBlockX();
		Integer zSpawn = world.getSpawnLocation().getBlockZ();

		// We generates first the bedrock at y=0
		world.getBlockAt(x, 0, z).setType(Material.BEDROCK);
		world.getBlockAt(x - 2*(x - xSpawn), 0, z).setType(Material.BEDROCK);
		world.getBlockAt(x, 0, z + 2*(zSpawn - z)).setType(Material.BEDROCK);
		world.getBlockAt(x - 2*(x - xSpawn), 0, z + 2*(zSpawn - z)).setType(Material.BEDROCK);


		// Following the way the wall is generated, the position of the original
		// "tower" can only be « SOUTH » or « EAST ».
		if(z > Math.floor(diameter/2)) {
			positionOriginal   = WallPosition.SOUTH;
			positionSymmetricX = WallPosition.SOUTH;
			positionSymmetricZ = WallPosition.NORTH;
			positionOpposite   = WallPosition.NORTH;
		}
		else {
			positionOriginal   = WallPosition.EAST;
			positionSymmetricX = WallPosition.WEST;
			positionSymmetricZ = WallPosition.EAST;
			positionOpposite   = WallPosition.WEST;
		}

		// The 4 towers are built.
		for(int y = 1; y <= wallHeight; y++) {
			setBlock(world.getBlockAt(x,                  y, z                 ), positionOriginal);
			setBlock(world.getBlockAt(x - 2*(x - xSpawn), y, z                 ), positionSymmetricX);
			setBlock(world.getBlockAt(x,                  y, z + 2*(zSpawn - z)), positionSymmetricZ);
			setBlock(world.getBlockAt(x - 2*(x - xSpawn), y, z + 2*(zSpawn - z)), positionOpposite);
		}
	}
}
