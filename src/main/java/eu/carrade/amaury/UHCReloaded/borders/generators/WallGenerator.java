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

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public abstract class WallGenerator
{
    final private Material wallBlockAir;
    final private Material wallBlockSolid;

    private int blocksSet = 0;

    public WallGenerator(Material wallBlockAir, Material wallBlockSolid)
    {
        this.wallBlockAir = wallBlockAir;
        this.wallBlockSolid = wallBlockSolid;
    }


    /**
     * Builds a wall in the world.
     *
     * @param world      The world the wall will be built in.
     * @param diameter   The diameter of the wall.
     * @param wallHeight The height of the wall.
     */
    public abstract void build(World world, int diameter, int wallHeight);


    /**
     * Sets a block according to his environment.
     * If the block replaces a "air/tree" block, or if it is next to a transparent block, it needs to be a
     * "wall.block.replaceAir" block.
     * In all other cases, it needs to be a "wall.block.replaceSolid" one.
     *
     * @param block The block to set.
     * @param position The position of the current wall in the world
     */
    protected void setBlock(Block block, WallPosition position)
    {
        // The block is a transparent block or a tree
        if (isBlockTransparentOrNatural(block.getType()))
        {
            block.setType(wallBlockAir);
        }
        // We set the block according to the block near it inside the border.
        else
        {
            final Material innerMaterial = getInnerBlock(block, position).getType();
            if (isBlockTransparentOrNatural(innerMaterial))
            {
                block.setType(wallBlockAir);
            }
            else
            {
                block.setType(wallBlockSolid);
            }
        }

        this.blocksSet++;
    }

    /**
     * Checks if a block is transparent or is part of a tree.
     * Used to generate the wall.
     *
     * @return boolean True if the block is transparent, or part of a tree/a giant mushroom/a
     * generated structure/etc.
     */
    protected Boolean isBlockTransparentOrNatural(Material blockType)
    {
        if (blockType.isTransparent())
        {
            return true;
        }

        switch (blockType)
        {
            case GLASS: // The glass isn't a transparent block for the `isTransparent` method.
            case STAINED_GLASS:
            case THIN_GLASS:
            case STAINED_GLASS_PANE:
            case LEAVES:
            case LEAVES_2:
            case LOG:
            case LOG_2:
            case CHEST: // Avoid a cube of the solid block where there where a chest.
            case TRAPPED_CHEST:
            case ENDER_CHEST:
            case WATER:
            case STATIONARY_WATER:
            case BED_BLOCK:
            case PISTON_STICKY_BASE: // Same idea (in jungle temples).
            case PISTON_BASE:
            case BOOKSHELF: // Same idea (in villages & fortresses).
            case MOB_SPAWNER: // Same idea (in dungeons).
            case SIGN_POST:
            case WALL_SIGN:
            case ICE: // Same idea (in cold biomes).
            case PACKED_ICE:
            case CACTUS: // Same idea (in deserts)
            case FENCE:
            case FENCE_GATE:
            case IRON_FENCE:
            case NETHER_FENCE:
            case PUMPKIN:
            case MELON_BLOCK: // Same idea (in jungles)
            case GLOWSTONE: // Same idea (in the Nether - why not?)
            case JACK_O_LANTERN:
            case HUGE_MUSHROOM_1: // Same idea (in dark forests).
            case HUGE_MUSHROOM_2:
            case CAKE_BLOCK: // It may be a lie, but hey, why not.
            case BEACON:
            case COBBLE_WALL:
            case ANVIL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the block left to the given block inside the border.
     *
     * @param block The reference block.
     * @param position The position of the wall currently build.
     */
    protected Block getInnerBlock(Block block, WallPosition position)
    {
        // Just for readability.
        final World world = block.getWorld();
        final int x = block.getX();
        final int y = block.getY();
        final int z = block.getZ();

        switch (position)
        {
            case EAST:
                return world.getBlockAt(x - 1, y, z);
            case NORTH:
                return world.getBlockAt(x, y, z + 1);
            case SOUTH:
                return world.getBlockAt(x, y, z - 1);
            case WEST:
                return world.getBlockAt(x + 1, y, z);
            default: // wait what?
                return null;
        }
    }

    public int getBlocksSet()
    {
        return blocksSet;
    }
}
