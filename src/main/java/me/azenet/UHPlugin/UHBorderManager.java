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
	
	public UHBorderManager(UHPlugin p) {
		this.p = p;
		this.currentBorderDiameter = p.getConfig().getInt("map.size");
	}
	
	public boolean isInsideBorder(Location location, int diameter) {
		if(!location.getWorld().equals(Bukkit.getWorlds().get(0))) { // The nether is not limited.
			return true;
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
		
		if (x < limitXInf || x > limitXSup || z < limitZInf || z > limitZSup) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean isInsideBorder(Location location) {
		return this.isInsideBorder(location, getCurrentBorderDiameter());
	}
	
	/**
	 * Return the distance from the location to the border, if the location is outside this border.
	 * If it is inside, returns 0.
	 * 
	 * @param location 
	 * @param diameter
	 * @return
	 */
	public int getDistanceToBorder(Location location, int diameter) {
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
	
	public HashSet<Player> getPlayersOutside() {
		return this.getPlayersOutside(getCurrentBorderDiameter());
	}
	
	public int getWarningSize() {
		return this.warningSize;
	}
	
	public void setWarningSize(int diameter) {
		cancelWarning();
		
		this.warningSize = diameter;
		
		warningTask = new BorderWarningTask(p);
		warningTask.runTaskTimer(p, 20L, 20L * p.getConfig().getInt("map.border.warningInterval", 90));
	}
	
	public void cancelWarning() {
		if(warningTask != null) {
			try {
				warningTask.cancel();
			} catch(IllegalStateException e) {
				
			}
		}
	}
	
	public int getCurrentBorderDiameter() {
		return this.currentBorderDiameter;
	}
	
	public void setCurrentBorderDiameter(int diameter) {
		cancelWarning();
		this.currentBorderDiameter = diameter;
		
		p.getWorldBorderIntegration().setupBorders(); // Update the WB border if needed
	}
	
}
