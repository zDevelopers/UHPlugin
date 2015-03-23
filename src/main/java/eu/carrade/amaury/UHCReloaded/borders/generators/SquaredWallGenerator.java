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
package eu.carrade.amaury.UHCReloaded.borders.generators;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;


public class SquaredWallGenerator extends WallGenerator {

	public SquaredWallGenerator(UHCReloaded p, Material wallBlockAir, Material wallBlockSolid) {
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
		Integer halfDiameter = (int) Math.floor(diameter/2);

		Location spawn = world.getSpawnLocation();
		Integer limitXInf = spawn.add(-halfDiameter, 0, 0).getBlockX();

		spawn = world.getSpawnLocation();
		Integer limitXSup = spawn.add(halfDiameter, 0, 0).getBlockX();

		spawn = world.getSpawnLocation();
		Integer limitZInf = spawn.add(0, 0, -halfDiameter).getBlockZ();

		spawn = world.getSpawnLocation();
		Integer limitZSup = spawn.add(0, 0, halfDiameter).getBlockZ();

		for (Integer x = limitXInf; x <= limitXSup; x++) {
			world.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
			world.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);

			for (Integer y = 2; y <= wallHeight; y++) {
				setBlock(world.getBlockAt(x, y, limitZInf), WallPosition.NORTH);
				setBlock(world.getBlockAt(x, y, limitZSup), WallPosition.SOUTH);
			}
		}

		for (Integer z = limitZInf + 1; z <= limitZSup - 1; z++) {
			world.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
			world.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);

			for (Integer y = 2; y <= wallHeight; y++) {
				setBlock(world.getBlockAt(limitXInf, y, z), WallPosition.WEST);
				setBlock(world.getBlockAt(limitXSup, y, z), WallPosition.EAST);
			}
		}
	}
}
