/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

package me.azenet.UHPlugin;

import java.util.HashSet;

import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.task.BorderWarningTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UHBorderManager {
	
	private UHPlugin p = null;
	private I18n i = null;
	
	private Integer currentBorderDiameter = null;
	
	private Integer warningSize = 0;
	private BukkitRunnable warningTask = null;
	
	private Boolean warningFinalTimeEnabled = false;
	private String warningTimerName = null;
	private CommandSender warningSender = null;
	
	private Boolean isCircularBorder = null;
	
	
	public UHBorderManager(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		
		this.warningTimerName = i.t("borders.warning.nameTimer");
		
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
	 * Returns the distance from the location to the border, if the location is outside this border.
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
		
		for(final Player player : p.getGameManager().getAlivePlayers()) {
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
	 * Returns true if there is currently a warning with a time left displayed.
	 * 
	 * @return
	 */
	public boolean getWarningFinalTimeEnabled() {
		return this.warningFinalTimeEnabled;
	}
	
	/**
	 * Returns the sender of the last warning configured.
	 * 
	 * @return
	 */
	public CommandSender getWarningSender() {
		return this.warningSender;
	}
	
	/**
	 * Sets the size of the future border, used in the warning messages sent to the
	 * players out of this future border.
	 * 
	 * This also starts the display of the warning messages, every 90 seconds by default
	 * (configurable, see config.yml, map.border.warningInterval).
	 * 
	 * If timeLeft is not null, the time available for the players to go inside the future
	 * border is displayed in the warning message.
	 * 
	 * @param diameter
	 * @param timeLeft The time available for the players to go inside the future border (minutes).
	 */
	public void setWarningSize(int diameter, int timeLeft, CommandSender sender) {
		cancelWarning();
		
		this.warningSize = diameter;
		
		if(timeLeft != 0) {
			UHTimer timer = new UHTimer(this.warningTimerName);
			timer.setDuration(timeLeft * 60);
			
			p.getTimerManager().registerTimer(timer);
			
			timer.start();
		}
		
		if(sender != null) {
			this.warningSender = sender;
		}
		
		warningTask = new BorderWarningTask(p);
		warningTask.runTaskTimer(p, 20L, 20L * p.getConfig().getInt("map.border.warningInterval", 90));
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
		setWarningSize(diameter, 0, null);
	}
	
	/**
	 * Returns the UHTimer object representing the countdown before the next border reduction.
	 * <p>
	 * Returns null if there isn't any countdown running currently.
	 * 
	 * @return The timer.
	 */
	public UHTimer getWarningTimer() {
		return p.getTimerManager().getTimer(this.warningTimerName);
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
		
		UHTimer timer = getWarningTimer();
		timer.stop();
		p.getTimerManager().unregisterTimer(timer);
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
	 * This also reconfigures WorldBorder (if present).
	 * 
	 * If WorldBorder is installed, all players out of this new border will be teleported inside the new one.
	 * Else, nothing will happens.
	 * 
	 * @param diameter
	 */
	public void setCurrentBorderDiameter(int diameter) {
		cancelWarning();
		this.currentBorderDiameter = diameter;
		
		p.getWorldBorderIntegration().setupBorders(); // Update the WB border if needed
	}
	
	
	/**
	 * Sends a list of the players outside the given border to the specified sender.
	 * 
	 * @param to The player/console to send the check.
	 * @param diameter The diameter of the border to be checked.
	 */
	public void sendCheckMessage(CommandSender to, int diameter) {
		HashSet<Player> playersOutside = getPlayersOutside(diameter);
		
		if(playersOutside.size() == 0) {
			to.sendMessage(i.t("borders.check.allPlayersInside"));
		}
		else {
			to.sendMessage(i.t("borders.check.countPlayersOutside", String.valueOf(playersOutside.size())));
			for(Player player : getPlayersOutside(diameter)) {
				int distance = getDistanceToBorder(player.getLocation(), diameter);
				if(distance > 150) {
					to.sendMessage(i.t("borders.check.itemPlayerFar", player.getName()));
				}
				else if(distance > 25) {
					to.sendMessage(i.t("borders.check.itemPlayerClose", player.getName()));
				}
				else {
					to.sendMessage(i.t("borders.check.itemPlayerVeryClose", player.getName()));
				}
			}
		}
	}
	
	
	/*** Squared border ***/
	
	/**
	 * Checks if the given location is inside the given squared border.
	 * 
	 * @param location The location to be checked.
	 * @param diameter The "diameter" of the squared wall (i.e. the size of a side of the wall).
	 * 
	 * @return true if the location is inside the border.
	 */
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
	
	/**
	 * Returns the distance from the given location to the border.
	 * 
	 * If the location is inside the border, or not in the same world, this returns 0.
	 * 
	 * @param location The location to be checked.
	 * @param diameter The "diameter" of the squared wall (i.e. the size of a side of the wall).
	 * 
	 * @return the distance from the given location to the border.
	 */
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
	
	/**
	 * Checks if the given location is inside the given circular border.
	 * 
	 * @param location The location to be checked.
	 * @param diameter The diameter of the circular wall.
	 * 
	 * @return true if the location is inside the border.
	 */
	private boolean isInsideCircularBorder(Location location, int diameter) {
		Double radius = Math.floor(diameter/2);
		
		return !(location.distance(Bukkit.getWorlds().get(0).getSpawnLocation()) >= radius);
	}
	
	/**
	 * Returns the distance from the given location to the border.
	 * 
	 * If the location is inside the border, or not in the same world, this returns 0.
	 * 
	 * @param location The location to be checked.
	 * @param diameter The diameter of the circular wall.
	 * 
	 * @return the distance from the given location to the border.
	 */
	private int getDistanceToCircularBorder(Location location, int diameter) {
		if(!location.getWorld().equals(Bukkit.getWorlds().get(0))) { // The nether is not limited.
			return 0;
		}
		if(isInsideBorder(location, diameter)) {
			return 0;
		}
		
		return (int) (location.distance(Bukkit.getWorlds().get(0).getSpawnLocation()) - Math.floor(diameter/2));
	}
}
