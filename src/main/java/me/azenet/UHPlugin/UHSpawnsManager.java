package me.azenet.UHPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
	
	/**
	 * Generates a random number between min and max.
	 * 
	 * @param min The minimum value. May be negative. Inclusive.
	 * @param max The maximum value. May be negative. Inclusive.
	 * @return A random number between these two points.
	 */
	public Integer random(int min, int max) {
		if(min == max) {
			return min;
		}
		
		Random rand = new Random();
		
		if(min > max) { // swap
			min = min + max;
			max = min - max;
			min = min - max;
		}
		
		if(min >= 0 && max >= 0) {
			return rand.nextInt(max - min + 1) + min;
		}
		else if(min <= 0 && max <= 0) {
			return -1 * (rand.nextInt(Math.abs(max) - Math.abs(min) + 1) + Math.abs(min));
		}
		else { // min <= 0 && max >= 0
			return rand.nextInt(Math.abs(min) + Math.abs(max)) - Math.abs(min);
		}
	}
	
	/**
	 * Generates randomly some spawn points in the map, with a minimal distance.
	 * 
	 * @param spawnCount The number of spawn points to generate.
	 * @param regionDiameter The diameter of the region where the spawn points will be generated.<br>
	 * This is limited by the size of the map. This will be seen as the diameter of a circular or
	 * of a squared map, following the shape of the world set in the configuration.
	 * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
	 * 
	 * @return false if there's too many spawn points / not enough surface to generate them.
	 * True of the generation succeeded.
	 */
	public boolean generateRandomSpawnPoints(int spawnCount, int regionDiameter, int minimalDistanceBetweenTwoPoints) {
		
		/** Possible? **/
		
		// If the surface of the map is too close of the sum of the surfaces of the private part
		// around each spawn point (a circle with, as radius, the minimal distance between two spawn
		// points), the generation will fail.
		
		int surfacePrivatePartsAroundSpawnPoints = (int) (spawnCount * (Math.PI * Math.pow(minimalDistanceBetweenTwoPoints, 2)));
		int surfaceRegion;
		if(p.getBorderManager().isCircularBorder()) {
			surfaceRegion = (int) ((Math.PI * Math.pow(regionDiameter, 2)) / 4);
		}
		else {
			surfaceRegion = (int) Math.pow(regionDiameter, 2);
		}
		
		Double packingDensity = Double.valueOf(surfacePrivatePartsAroundSpawnPoints) / Double.valueOf(surfaceRegion);
		
		// According to Lagrange and Thue's works on circle packagings, the highest density possible is
		// approximately 0.9069 (with an hexagonal arrangement of the circles).
		// Even with a packaging density very close to this limit, the generation time is correct.
		// So we uses this as a limit.
		if(packingDensity >= 0.9069) {
			return false;
		}
		
		/** Generation **/
		
		World world = p.getServer().getWorlds().get(0);
		LinkedList<Location> randomSpawnPoints = new LinkedList<Location>();
		int generatedSpawnPoints = 0;
		
		generationLoop: while(generatedSpawnPoints != spawnCount) {
			// We generates a point in the square of side regionDiameter.
			// In case of a circular world, if the point was generated out of the circle, it will be
			// excluded when his presence inside the region will be checked.
			
			Location randomPoint = new Location(world,
					random((int) (-1 * Math.floor(regionDiameter / 2)), (int) Math.floor(regionDiameter / 2)),
					0,
					random((int) (-1 * Math.floor(regionDiameter / 2)), (int) Math.floor(regionDiameter / 2)));
			
			// Inside the region?
			if(!p.getBorderManager().isInsideBorder(randomPoint, regionDiameter)) {
				p.getLogger().info("outside");
				continue generationLoop; // outside: nope
			}
			
			// Is that point at a correct distance of the other ones?
			for(Location spawn : randomSpawnPoints) {
				if(spawn.distance(randomPoint) < minimalDistanceBetweenTwoPoints) {
					p.getLogger().info("too close");
					continue generationLoop; // too close: nope
				}
			}
			
			// Well, all done.
			randomSpawnPoints.add(randomPoint);
			generatedSpawnPoints++;
		}
		
		// Generation done, let's register these points.
		
		for(Location spawn : randomSpawnPoints) {
			addSpawnPoint(spawn);
		}
		
		return true;
	}
}
