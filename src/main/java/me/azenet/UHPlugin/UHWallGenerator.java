package me.azenet.UHPlugin;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class UHWallGenerator {
	
	private UHPlugin p = null;
	private I18n i = null;
	private World w = null;
	
	private Material wallBlockAir = null;
	private Material wallBlockSolid = null;
	
	public UHWallGenerator(UHPlugin p, World w) {
		this.p = p;
		this.i = p.getI18n();
		this.w = w;
	}
	
	/**
	 * Generate the walls around the map.
	 * 
	 * @throws Exception
	 */
	public boolean build() {
		Integer halfMapSize = (int) Math.floor(p.getConfig().getInt("map.size")/2);
		Integer wallHeight = p.getConfig().getInt("map.wall.height");
		
		this.wallBlockAir = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceAir"));
		this.wallBlockSolid = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceSolid"));
		
		if(wallBlockAir == null || !wallBlockAir.isSolid() || wallBlockSolid == null || !wallBlockSolid.isSolid()) {
			p.getLogger().severe(i.t("wall.blocksError"));
			return false;
		}
		
		Location spawn = w.getSpawnLocation();
		Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
		
		spawn = w.getSpawnLocation();
		Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
		
		spawn = w.getSpawnLocation();
		Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
		
		spawn = w.getSpawnLocation();
		Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
		
		for (Integer x = limitXInf; x <= limitXSup; x++) {
			w.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
			w.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);
			
			for (Integer y = 2; y <= wallHeight; y++) {
				setBlock(w.getBlockAt(x, y, limitZInf), WallPosition.NORTH);
				setBlock(w.getBlockAt(x, y, limitZSup), WallPosition.SOUTH);
			}
		} 
		
		for (Integer z = limitZInf; z <= limitZSup; z++) {
			w.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
			w.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);
			
			for (Integer y = 2; y <= wallHeight; y++) {
				setBlock(w.getBlockAt(limitXInf, y, z), WallPosition.WEST);
				setBlock(w.getBlockAt(limitXSup, y, z), WallPosition.EAST);
			}
		}
		
		return true;
	}
	
	/**
	 * Set a block according to his environment.
	 * If the block replaces a "air/tree" block, or if it is next to a transparent block, it needs to be a
	 * "wall.block.replaceAir" block.
	 * In all other cases, it needs to be a "wall.block.replaceSolid" one. 
	 * 
	 * @param block The block to set.
	 * @param position The position of the current wall in the world
	 */
	private void setBlock(Block block, WallPosition position) {
		// The block is a transparent block or a tree
		if(isBlockTransparentOrTree(block.getType())) {
			block.setType(wallBlockAir);
		}
		// We set the block according to the block near it inside the border.
		else {
			Material innerMaterial = getInnerBlock(block, position).getType();
			if(innerMaterial.isTransparent() || innerMaterial.equals(Material.WATER) || innerMaterial.equals(Material.STATIONARY_WATER)) {
				block.setType(wallBlockAir);
			}
			else {
				block.setType(wallBlockSolid);
			}
		}
	}
	
	/**
	 * Checks if a block is transparent or is par of a tree.
	 * Used to generate the wall.
	 * 
	 * @return bool True if the block is transparent, or part of a tree.
	 */
	private Boolean isBlockTransparentOrTree(Material blockType) {
		if(blockType.isTransparent() 
				|| blockType.equals(Material.LEAVES) || blockType.equals(Material.LEAVES_2) 
				|| blockType.equals(Material.LOG) || blockType.equals(Material.LOG_2)
				|| blockType.equals(Material.CHEST) || blockType.equals(Material.TRAPPED_CHEST)
				|| blockType.equals(Material.WATER) || blockType.equals(Material.STATIONARY_WATER)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the block left to the given block inside the border.
	 * 
	 * @param block The reference block.
	 * @param position The position of the wall currently build.
	 */
	private Block getInnerBlock(Block block, WallPosition position) {
		// Just for readability.
		Integer x = block.getX();
		Integer y = block.getY();
		Integer z = block.getZ();
		
		switch(position) {
			case EAST:
				return w.getBlockAt(x - 1, y, z);
			case NORTH:
				return w.getBlockAt(x, y, z + 1);
			case SOUTH:
				return w.getBlockAt(x, y, z - 1);
			case WEST:
				return w.getBlockAt(x + 1, y, z);
			default: // wait what?
				return null;
		}
	}
	
	/**
	 * Used to determine in witch wall we are, to get the "inner" block.
	 * 
	 * North: small Z
	 * South: big Z
	 * East:  big X
	 * West:  small X
	 */
	public enum WallPosition {
		NORTH("N"),
		SOUTH("S"),
		EAST("E"),
		WEST("W"),
		;
		
		WallPosition(String position) {
			
		}
	}
}
