package me.azenet.UHPlugin;

import java.util.HashSet;

import me.azenet.UHPlugin.task.BorderWarningTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UHBorderManager {
	
	private UHPlugin p = null;
	private Integer currentBorderDiameter = null;
	
	private Integer warningSize = 0;
	private BukkitRunnable warningTask = null;
	
	private Boolean isCircularBorder = null;
	
	
	public UHBorderManager(UHPlugin plugin) {
		this.p = plugin;
		
		this.currentBorderDiameter = p.getConfig().getInt("map.size");
		this.isCircularBorder      = p.getConfig().getBoolean("map.circular");
	}
	
	/**
	 * Checks if a given location is inside the border with the given diameter.
	 * The check is performed for a circular or squared border, following the configuration.
	 * 
	 * @param location
	 * @param diameter
	 * @return
	 */
	public boolean isInsideBorder(Location location, int diameter) {
		if(!location.getWorld().equals(Bukkit.getWorlds().get(0))) { // The nether is not limited.
			return true;
		}
		
		if(this.isCircularBorder()) {
			return this.isInsideCircularBorder(location, diameter);
		}
		else {
			return this.isInsideSquaredBorder(location, diameter);
		}
	}
	
	/**
	 * Checks if a given location is inside the border with the current diameter.
	 * The check is performed for a circular or squared border, following the configuration.
	 * 
	 * @param location
	 * @param diameter
	 * @return
	 */
	public boolean isInsideBorder(Location location) {
		return this.isInsideBorder(location, getCurrentBorderDiameter());
	}
	
	/**
	 * Return the distance from the location to the border, if the location is outside this border.
	 * If it is inside, or in another world, returns 0.
	 * 
	 * @param location 
	 * @param diameter
	 * @return
	 */
	public int getDistanceToBorder(Location location, int diameter) {		
		if(this.isCircularBorder()) {
			return this.getDistanceToCircularBorder(location, diameter);
		}
		else {
			return this.getDistanceToSquaredBorder(location, diameter);
		}
	}
	
	
	/**
	 * Returns a list of the players outside a border with the given diameter.
	 * The check is performed for a circular or squared border, following the configuration.
	 * 
	 * @param diameter
	 * @return
	 */
	public HashSet<Player> getPlayersOutside(int diameter) {
		HashSet<Player> playersOutside = new HashSet<Player>();
		
		for(final String playerS : p.getGameManager().getAlivePlayers()) {
			Player player = Bukkit.getPlayer(playerS);
			if(player == null) { // offline
				continue;
			}
			if(!isInsideBorder(player.getLocation(), diameter)) {
				playersOutside.add(player);
			}
		}
		
		return playersOutside;
	}
	
	/**
	 * Returns the diameter used to check if the players are inside the wall.
	 * 
	 * If the wall is circular, the diameter used to check must be bigger to avoid false positives
	 * if a player is in an angle of the circular wall.
	 * 
	 * “+4” ? Experimental.
	 * 
	 * @return
	 */
	public int getCheckDiameter() {
		if(this.isCircularBorder()) {
			return this.getCurrentBorderDiameter() + 4;
		}
		else {
			return this.getCurrentBorderDiameter();
		}
	}
	
	/**
	 * Returns the size of the future border, used in the warning messages sent to the
	 * players out of this future border.
	 * 
	 * @return
	 */
	public int getWarningSize() {
		return this.warningSize;
	}
	
	/**
	 * Sets the size of the future border, used in the warning messages sent to the
	 * players out of this future border.
	 * 
	 * This also starts the display of the warning messages, every 90 seconds by default
	 * (configurable, see config.yml, map.border.warningInterval).
	 * 
	 * @param diameter
	 */
	public void setWarningSize(int diameter) {
		cancelWarning();
		
		this.warningSize = diameter;
		
		warningTask = new BorderWarningTask(p);
		warningTask.runTaskTimer(p, 20L, 20L * p.getConfig().getInt("map.border.warningInterval", 90));
	}
	
	/**
	 * Stops the display of the warning messages.
	 */
	public void cancelWarning() {
		if(warningTask != null) {
			try {
				warningTask.cancel();
			} catch(IllegalStateException e) {
				
			}
		}
	}
	
	/**
	 * Returns the current border diameter.
	 * 
	 * @return
	 */
	public int getCurrentBorderDiameter() {
		return this.currentBorderDiameter;
	}
	
	/**
	 * Returns true if the border is circular.
	 * 
	 * @return
	 */
	public boolean isCircularBorder() {
		return this.isCircularBorder;
	}
	
	/**
	 * Changes the current border diameter.
	 * This also reconfigure WorldBorder (if present).
	 * 
	 * If WorldBorder is installed, all players out of this new border will be teleported inside the new one.
	 * Else, these players will be frozen.
	 * 
	 * @param diameter
	 */
	public void setCurrentBorderDiameter(int diameter) {
		cancelWarning();
		this.currentBorderDiameter = diameter;
		
		p.getWorldBorderIntegration().setupBorders(); // Update the WB border if needed
	}
	
	
	/*** Squared border ***/
	
	private boolean isInsideSquaredBorder(Location location, int diameter) {
		Integer halfMapSize = (int) Math.floor(diameter/2);
		Integer x = location.getBlockX();
		Integer z = location.getBlockZ();
		
		Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
		
		return !(x < limitXInf || x > limitXSup || z < limitZInf || z > limitZSup);
	}
	
	private int getDistanceToSquaredBorder(Location location, int diameter) {
		if(!location.getWorld().equals(Bukkit.getWorlds().get(0))) { // The nether is not limited.
			return 0;
		}
		if(isInsideBorder(location, diameter)) {
			return 0;
		}
		
		Integer halfMapSize = (int) Math.floor(diameter/2);
		Integer x = location.getBlockX();
		Integer z = location.getBlockZ();
		
		Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
		
		spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
		Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
		
		if((x > limitXSup || x < limitXInf) && z < limitZSup && z > limitZInf) { // east or west of the border
			return Math.abs(x - limitXSup);
		}
		else if((z > limitZSup || z < limitZInf) && x < limitXSup && x > limitXInf) { // north or south of the border
			return Math.abs(z - limitZSup);
		}
		else if(x > limitXSup && z < limitZInf) { // N-E
			return (int) location.distance(new Location(location.getWorld(), limitXSup, location.getBlockY(), limitZInf));
		}
		else if(x > limitXSup && z > limitZSup) { // S-E
			return (int) location.distance(new Location(location.getWorld(), limitXSup, location.getBlockY(), limitZSup));
		}
		else if(x < limitXInf && z > limitZSup) { // S-O
			return (int) location.distance(new Location(location.getWorld(), limitXInf, location.getBlockY(), limitZSup));
		}
		else if(x < limitXInf && z < limitZInf) { // N-O
			return (int) location.distance(new Location(location.getWorld(), limitXInf, location.getBlockY(), limitZInf));
		}
		else {
			return 0; // Should never happen.
		}
	}
	
	
	/** Circular border **/
	
	private boolean isInsideCircularBorder(Location location, int diameter) {
		Double halfMapSize = Math.floor(diameter/2);
		
		return !(location.distance(Bukkit.getWorlds().get(0).getSpawnLocation()) > halfMapSize);
	}
	
	private int getDistanceToCircularBorder(Location location, int diameter) {
		if(!location.getWorld().equals(Bukkit.getWorlds().get(0))) { // The nether is not limited.
			return 0;
		}
		if(isInsideBorder(location, diameter)) {
			return 0;
		}
		
		return (int) (location.distance(Bukkit.getWorlds().get(0).getSpawnLocation()) - diameter);
	}
}
