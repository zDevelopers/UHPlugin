package me.azenet.UHPlugin;

import java.util.LinkedList;
import java.util.List;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class UHSpawnsManager {
	
	UHPlugin p = null;
	I18n i = null;
	
	private LinkedList<Location> spawnPoints = new LinkedList<Location>();
	
	
	public UHSpawnsManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
	}
	
	/**
	 * Adds a spawn point at (x;z) in the default world.
	 * 
	 * @param x The X coordinate.
	 * @param z The Z coordinate.
	 */
	public void addSpawnPoint(final Double x, final Double z) {
		addSpawnPoint(p.getServer().getWorlds().get(0), x, z);
	}
	
	/**
	 * Adds a spawn point at (x;z) in the given world.
	 * 
	 * @param world The world.
	 * @param x The X coordinate.
	 * @param z The Z coordinate.
	 */
	public void addSpawnPoint(final World world, final Double x, final Double z) {
		addSpawnPoint(new Location(world, x, 0, z));
	}
	
	/**
	 * Adds a spawn point from a location.
	 * 
	 * @param location The location.
	 */
	public void addSpawnPoint(final Location location) {
		Location spawnPoint = location.clone();
		
		// Initial fall, except in the nether.
		if(!(spawnPoint.getWorld().getEnvironment() == Environment.NETHER)) {
			spawnPoint.setY(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 120);
		}
		
		if(!p.getBorderManager().isInsideBorder(spawnPoint)) {
			throw new IllegalArgumentException("The given spawn location is outside the current border");
		}
		
		spawnPoints.add(spawnPoint);
	}
	
	/**
	 * Returns the registered spawn points.
	 * 
	 * @return The spawn points.
	 */
	public List<Location> getSpawnPoints() {
		return spawnPoints;
	}
	
	/**
	 * Imports spawn points from the configuration.
	 * 
	 * @return The number of spawn points imported.
	 */
	public int importSpawnPointsFromConfig() {
		if(p.getConfig().getList("spawnpoints") != null) {
			int spawnCount = 0;
			for(Object position : p.getConfig().getList("spawnpoints")) {
				if(position instanceof String && position != null) {
					String[] coords = ((String) position).split(",");
					try {
						addSpawnPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
						p.getLogger().info(i.t("load.spawnPointAdded", coords[0], coords[1]));
						spawnCount++;
					} catch(Exception e) { // Not an integer or not enough coords
						p.getLogger().warning(i.t("load.invalidSpawnPoint", (String) position));
					}
				}
			}
			
			return spawnCount;
		}
		
		return 0;
	}
}
