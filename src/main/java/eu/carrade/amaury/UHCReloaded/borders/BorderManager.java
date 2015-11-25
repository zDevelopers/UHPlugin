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

package eu.carrade.amaury.UHCReloaded.borders;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.borders.exceptions.CannotGenerateWallsException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.task.BorderWarningTask;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;


public class BorderManager {

	private UHCReloaded p = null;
	private I18n i = null;

	private Integer currentBorderDiameter = null;

	private Integer warningSize = 0;
	private BukkitRunnable warningTask = null;

	private Boolean warningFinalTimeEnabled = false;
	private String warningTimerName = null;
	private CommandSender warningSender = null;

	private MapShape mapShape = null;


	public BorderManager(UHCReloaded plugin) {
		this.p = plugin;
		this.i = p.getI18n();

		this.warningTimerName = i.t("borders.warning.nameTimer");

		this.currentBorderDiameter = p.getConfig().getInt("map.size");
		this.mapShape              = MapShape.fromString(p.getConfig().getString("map.shape"));

		if(mapShape == null) {
			p.getLogger().warning("Invalid shape '" + p.getConfig().getString("map.shape") + "'; using 'squared' instead.");
			mapShape = MapShape.SQUARED;
		}
	}

	/**
	 * Sets the shape of the map. Updates the WorldBorder too.
	 *
	 * @param shape The shape.
	 */
	public void setMapShape(MapShape shape) {
		this.mapShape = shape;

		p.getWorldBorderIntegration().setupBorders(); // Updates the WB border if needed
	}

	/**
	 * Returns the current shape of the map.
	 *
	 * @return The shape.
	 */
	public MapShape getMapShape() {
		return mapShape;
	}

	/**
	 * Checks if a given location is inside the border with the given diameter.
	 * The check is performed for a circular or squared border, following the configuration.
	 *
	 * @param location
	 * @param diameter
	 * @return
	 */
	public boolean isInsideBorder(Location location, double diameter) {
		if(!location.getWorld().getEnvironment().equals(Environment.NORMAL)) { // The nether/end are not limited.
			return true;
		}

		return mapShape.getShape().isInsideBorder(location, diameter, location.getWorld().getSpawnLocation());
	}

	/**
	 * Checks if a given location is inside the border with the current diameter.
	 * The check is performed for a circular or squared border, following the configuration.
	 *
	 * @param location
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
	public double getDistanceToBorder(Location location, double diameter) {
		return mapShape.getShape().getDistanceToBorder(location, diameter, location.getWorld().getSpawnLocation());
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

		for(final Player player : p.getGameManager().getOnlineAlivePlayers()) {
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
	 * “+3” ? Experimental.
	 *
	 * @return
	 */
	public int getCheckDiameter() {
		if(getMapShape() == MapShape.CIRCULAR) {
			return getCurrentBorderDiameter() + 3;
		}
		else {
			return getCurrentBorderDiameter();
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
		if(timer != null) {
			timer.stop();
			p.getTimerManager().unregisterTimer(timer);
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

		p.getWorldBorderIntegration().setupBorders(); // Updates the WB border if needed
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
				double distance = getDistanceToBorder(player.getLocation(), diameter);
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

	/**
	 *
	 * @param world The world were the walls will be built in.
	 * @throws eu.carrade.amaury.UHCReloaded.borders.exceptions.CannotGenerateWallsException
	 */
	public void generateWalls(World world) throws CannotGenerateWallsException {
		Integer wallHeight = p.getConfig().getInt("map.wall.height");

		Material wallBlockAir = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceAir"));
		Material wallBlockSolid = Material.matchMaterial(p.getConfig().getString("map.wall.block.replaceSolid"));

		if(wallBlockAir == null || !wallBlockAir.isSolid() || wallBlockSolid == null || !wallBlockSolid.isSolid()) {
			throw new CannotGenerateWallsException("Cannot generate the walls: invalid blocks set in the config");
		}

		mapShape.getWallGeneratorInstance(p, wallBlockAir, wallBlockSolid).build(world, getCurrentBorderDiameter(), wallHeight);
	}
}
