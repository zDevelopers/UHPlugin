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
import me.azenet.UHPlugin.i18n.I18n;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;


public abstract class WallGenerator {

	private UHPlugin p;
	private I18n i;

	private Material wallBlockAir = null;
	private Material wallBlockSolid = null;

	private int blocksSet = 0;

	public WallGenerator(UHPlugin p, Material wallBlockAir, Material wallBlockSolid) {
		this.p = p;
		this.i = p.getI18n();

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
	protected void setBlock(Block block, WallPosition position) {
		// The block is a transparent block or a tree
		if(isBlockTransparentOrNatural(block.getType())) {
			block.setType(wallBlockAir);
		}
		// We set the block according to the block near it inside the border.
		else {
			Material innerMaterial = getInnerBlock(block, position).getType();
			if(isBlockTransparentOrNatural(innerMaterial)) {
				block.setType(wallBlockAir);
			}
			else {
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
	protected Boolean isBlockTransparentOrNatural(Material blockType) {
		if(blockType.isTransparent()) {
			return true;
		}

		switch(blockType) {
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
	protected Block getInnerBlock(Block block, WallPosition position) {
		// Just for readability.
		World world = block.getWorld();
		Integer x   = block.getX();
		Integer y   = block.getY();
		Integer z   = block.getZ();

		switch(position) {
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

	public int getBlocksSet() {
		return blocksSet;
	}
}
