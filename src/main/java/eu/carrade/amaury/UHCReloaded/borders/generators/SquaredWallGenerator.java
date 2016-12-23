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
package eu.carrade.amaury.UHCReloaded.borders.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;


public class SquaredWallGenerator extends WallGenerator
{
    public SquaredWallGenerator(Material wallBlockAir, Material wallBlockSolid)
    {
        super(wallBlockAir, wallBlockSolid);
    }

    /**
     * Builds a wall in the world.
     *
     * @param world      The world the wall will be built in.
     * @param diameter   The diameter of the wall.
     * @param wallHeight The height of the wall.
     */
    @Override
    public void build(World world, int diameter, int wallHeight)
    {
        Integer halfDiameter = (int) Math.floor(diameter / 2);

        Location spawn = world.getSpawnLocation();
        Integer limitXInf = spawn.add(-halfDiameter, 0, 0).getBlockX();

        spawn = world.getSpawnLocation();
        Integer limitXSup = spawn.add(halfDiameter, 0, 0).getBlockX();

        spawn = world.getSpawnLocation();
        Integer limitZInf = spawn.add(0, 0, -halfDiameter).getBlockZ();

        spawn = world.getSpawnLocation();
        Integer limitZSup = spawn.add(0, 0, halfDiameter).getBlockZ();

        for (Integer x = limitXInf; x <= limitXSup; x++)
        {
            world.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
            world.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);

            for (Integer y = 2; y <= wallHeight; y++)
            {
                setBlock(world.getBlockAt(x, y, limitZInf), WallPosition.NORTH);
                setBlock(world.getBlockAt(x, y, limitZSup), WallPosition.SOUTH);
            }
        }

        for (Integer z = limitZInf + 1; z <= limitZSup - 1; z++)
        {
            world.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
            world.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);

            for (Integer y = 2; y <= wallHeight; y++)
            {
                setBlock(world.getBlockAt(limitXInf, y, z), WallPosition.WEST);
                setBlock(world.getBlockAt(limitXSup, y, z), WallPosition.EAST);
            }
        }
    }
}
