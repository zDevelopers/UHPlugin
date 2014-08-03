package me.azenet.UHPlugin;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
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
	
	private Integer blocksSet = 0;
	
	public UHWallGenerator(UHPlugin p, World w) {
		this.p = p;
		this.i = p.getI18n();
		this.w = w;
	}
	
	/**
	 * Generates the walls around the map, following the configuration.
	 * 
	 */
	public boolean build() {
		Integer wallHeight = p.getConfig().getInt("map.wall.height");
		
		this.wallBlockAir = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceAir"));
		this.wallBlockSolid = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceSolid"));
		
		if(wallBlockAir == null || !wallBlockAir.isSolid() || wallBlockSolid == null || !wallBlockSolid.isSolid()) {
			p.getLogger().severe(i.t("wall.blocksError"));
			return false;
		}
		
		
		if(p.getBorderManager().isCircularBorder()) {
			this.buildCircularWall(w, p.getBorderManager().getCurrentBorderDiameter(), wallHeight);
		}
		else {
			this.buildSquaredWall(w, p.getBorderManager().getCurrentBorderDiameter(), wallHeight);
		}
		
		Bukkit.getLogger().info(this.blocksSet + " blocks affected.");
		this.blocksSet = 0;
		
		return true;
	}
	
	
	/**
	 * Sets a block according to his environment.
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
			if(isBlockTransparentOrTree(innerMaterial)) {
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
	 * @return bool True if the block is transparent, or part of a tree.
	 */
	private Boolean isBlockTransparentOrTree(Material blockType) {
		if(blockType.isTransparent()) {
			return true;
		}
		
		switch(blockType) {
			case LEAVES:
			case LEAVES_2:
			case LOG:
			case LOG_2:
			case CHEST:
			case TRAPPED_CHEST:
			case WATER:
			case STATIONARY_WATER:
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
	
	
	
	/*** *** SQUARED WALL *** ***/
	
	
	/**
	 * Builds a squared wall around the map.
	 * 
	 * @param w The wall will be built in this world.
	 * @param diameter The size of the wall.
	 * @param wallHeight The height of the wall.
	 */
	private void buildSquaredWall(World world, int diameter, int wallHeight) {
		
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
	
	
	/*** *** CIRCULAR WALL *** ***/
	
	
	/**
	 * Builds a circular wall around the map.
	 * 
	 * @param w The wall will be built in this world.
	 * @param diameter The size of the wall.
	 * @param wallHeight The height of the wall.
	 */
	private void buildCircularWall(World world, int diameter, int wallHeight) {
		
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
		
		Block candidate1 = null;
		Block candidate2 = null;
		Block candidate3 = null;
		
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
	 * Builds 4 "towers" of the wall, from y=0 to y=wallHeight, at the given coordinates and
	 * the symmetric points.
	 * 
	 * @param world
	 * @param x
	 * @param z
	 * @param wallHeight
	 * @param diameter
	 */
	private void buildWallPoint(World world, int x, int z, int wallHeight, int diameter) {
		
		WallPosition positionOriginal = null;
		WallPosition positionSymmetricX = null;
		WallPosition positionSymmetricZ = null;
		WallPosition positionOpposite = null;
		
		Integer xSpawn = Bukkit.getWorlds().get(0).getSpawnLocation().getBlockX();
		Integer zSpawn = Bukkit.getWorlds().get(0).getSpawnLocation().getBlockZ();
		
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
			this.setBlock(world.getBlockAt(x,                  y, z                 ), positionOriginal);
			this.setBlock(world.getBlockAt(x - 2*(x - xSpawn), y, z                 ), positionSymmetricX);
			this.setBlock(world.getBlockAt(x,                  y, z + 2*(zSpawn - z)), positionSymmetricZ);
			this.setBlock(world.getBlockAt(x - 2*(x - xSpawn), y, z + 2*(zSpawn - z)), positionOpposite);
		}
	}
}
