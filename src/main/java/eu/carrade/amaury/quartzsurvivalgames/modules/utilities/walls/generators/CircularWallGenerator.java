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
package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.walls.generators;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public class CircularWallGenerator extends WallGenerator
{
    public CircularWallGenerator(Material wallBlockAir, Material wallBlockSolid)
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
        // Only one quarter of the circle is explicitly set, the other parts are generated
        // following the first quarter.
        // The quarter chosen to be explicitly generated if the one on the South-East,
        // starting at x = xSpawn+radius ; z = zSpawn and ending at x = xSpawn ; z = zSpawn+radius.

        // In each step we gets the three blocks susceptible to be the next block and we calculates the
        // distance from the center to these blocks.
        // The good block if the one with the closest distance to the radius.

        final int radius = (int) Math.floor(diameter / 2);

        final int xSpawn = world.getSpawnLocation().getBlockX();
        final int ySpawn = world.getSpawnLocation().getBlockY();
        final int zSpawn = world.getSpawnLocation().getBlockZ();

        // First block.
        Block currentBlock = world.getBlockAt(xSpawn + radius, ySpawn, zSpawn);

        Block candidate1;
        Block candidate2;
        Block candidate3;

        // Infinite loop broken when the generation is done.
        while (true)
        {

            // 1) the current point, the symmetries and the opposite point are built.
            this.buildWallPoint(world, currentBlock.getX(), currentBlock.getZ(), wallHeight, diameter);


            // 2) the two candidates are found, except if the build is finished.
            if (currentBlock.getX() == xSpawn)
            {
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

            // The first is better
            if (distanceCandidate1ToRef < distanceCandidate2ToRef && distanceCandidate1ToRef < distanceCandidate3ToRef)
            {
                currentBlock = candidate1;
            }
            // The second is better
            else if (distanceCandidate2ToRef < distanceCandidate1ToRef && distanceCandidate2ToRef < distanceCandidate3ToRef)
            {
                currentBlock = candidate2;
            }
            else
            {
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
    private void buildWallPoint(World world, int x, int z, int wallHeight, int diameter)
    {
        WallPosition positionOriginal;
        WallPosition positionSymmetricX;
        WallPosition positionSymmetricZ;
        WallPosition positionOpposite;

        final int xSpawn = world.getSpawnLocation().getBlockX();
        final int zSpawn = world.getSpawnLocation().getBlockZ();

        // We generates first the bedrock at y=0
        world.getBlockAt(x, 0, z).setType(Material.BEDROCK);
        world.getBlockAt(x - 2 * (x - xSpawn), 0, z).setType(Material.BEDROCK);
        world.getBlockAt(x, 0, z + 2 * (zSpawn - z)).setType(Material.BEDROCK);
        world.getBlockAt(x - 2 * (x - xSpawn), 0, z + 2 * (zSpawn - z)).setType(Material.BEDROCK);


        // Following the way the wall is generated, the position of the original
        // "tower" can only be « SOUTH » or « EAST ».
        if (z > Math.floor(diameter / 2))
        {
            positionOriginal = WallPosition.SOUTH;
            positionSymmetricX = WallPosition.SOUTH;
            positionSymmetricZ = WallPosition.NORTH;
            positionOpposite = WallPosition.NORTH;
        }
        else
        {
            positionOriginal = WallPosition.EAST;
            positionSymmetricX = WallPosition.WEST;
            positionSymmetricZ = WallPosition.EAST;
            positionOpposite = WallPosition.WEST;
        }

        // The 4 towers are built.
        for (int y = 1; y <= wallHeight; y++)
        {
            setBlock(world.getBlockAt(x, y, z), positionOriginal);
            setBlock(world.getBlockAt(x - 2 * (x - xSpawn), y, z), positionSymmetricX);
            setBlock(world.getBlockAt(x, y, z + 2 * (zSpawn - z)), positionSymmetricZ);
            setBlock(world.getBlockAt(x - 2 * (x - xSpawn), y, z + 2 * (zSpawn - z)), positionOpposite);
        }
    }
}
