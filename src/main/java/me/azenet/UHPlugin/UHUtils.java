package me.azenet.UHPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class UHUtils {
	
	/**
	 * Extract a string from a list of arguments, starting at the given index.
	 * 
	 * @param args The raw arguments.
	 * @param startIndex The index of the first item in the returned string (first argument given: 0).
	 * 
	 * @return The extracted string.
	 * 
	 * @throws IllegalArgumentException if the index of the first element is out of the bounds of the arguments' list.
	 */
	public static String getStringFromCommandArguments(String[] args, int startIndex) {
		
		System.out.print(args.length);
		System.out.print(startIndex);
		
		if(args.length < startIndex) {
			throw new IllegalArgumentException("The index of the first element is out of the bounds of the arguments' list.");
		}
		
		String text = "";
		
		for(int index = startIndex; index < args.length; index++) {
			if(index < args.length - 1) {
				text += args[index] + " ";
			}
			else {
				text += args[index];
			}
		}
		
		return text;
	}
	
	
	/**
	 * Find a safe spot where teleport the player, and teleport the player to that spot.
	 * If a spot is not found, the player is not teleported, except if the force arg is set to true.
	 * 
	 * Inspiration took in the WorldBorder plugin.
	 * 
	 * @param player
	 * @param location
	 * @param force If true the player will be teleported to the exact given location if there is no safe spot.
	 * @return true if the player was effectively teleported.
	 */
	public static boolean safeTP(Player player, Location location, boolean force) {
		// If the target is safe, let's go
		if(isSafeSpot(location)) {
			player.teleport(location);
			return true;
		}
		
		// If the teleportation is forced, let's go
		if(force) {
			player.teleport(location);
			return true;
		}
		
		// We try to find a spot above or below the target (this is probably the good solution, because
		// if the spot is obstrued, because this is mainly used to teleport players back after their
		// death, the cause is likely to be a falling block or an arrow shot during a fall).
		
		Location safeSpot = null;
		// Max height (thx to WorldBorder)
		final int maxHeight = (location.getWorld().getEnvironment() == World.Environment.NETHER) ? 125 : location.getWorld().getMaxHeight() - 2;
		
		for(int yGrow = (int) location.getBlockY(), yDecr = (int) location.getBlockY(); yDecr >= 1 || yGrow <= maxHeight; yDecr--, yGrow++) {
			// Above?
			if(yGrow < maxHeight) {
				Location spot = new Location(location.getWorld(), location.getBlockX(), yGrow, location.getBlockZ());
				if(isSafeSpot(spot)) {
					safeSpot = spot;
					break;
				}
			}
			
			// Below?
			if(yDecr > 1 && yDecr != yGrow) {
				Location spot = new Location(location.getWorld(), location.getX(), yDecr, location.getZ());
				if(isSafeSpot(spot)) {
					safeSpot = spot;
					break;
				}
			}
		}
		
		// A spot was found, let's teleport.
		if(safeSpot != null) {
			player.teleport(safeSpot);
			return true;
		}
		// No spot found; the teleportation is cancelled.
		else {
			return false;
		}
	}
	
	public static boolean safeTP(Player player, Location location) {
		return safeTP(player, location, false);
	}
	
	/**
	 * Checks if a given location is safe.
	 * A safe location is a location with two breathable blocks (aka transparent block or water)
	 * over something solid
	 * 
	 * @param location
	 * @return true if the location is safe.
	 */
	private static boolean isSafeSpot(Location location) {		
		Block blockCenter = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		Block blockAbove = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
		Block blockBelow = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		
		if((blockCenter.getType().isTransparent() || (blockCenter.isLiquid() && !blockCenter.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA)))
				&& (blockAbove.getType().isTransparent() || (blockAbove.isLiquid() && !blockAbove.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA)))) {
			// two breathable blocks: ok
			
			if(blockBelow.getType().isSolid()) {
				// The block below is solid 
				return true;
			}
			return false;
		}
		return false;
	}
}
