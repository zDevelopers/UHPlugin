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

package me.azenet.UHPlugin.integration;

import me.azenet.UHPlugin.UHPlugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.plugin.Plugin;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;

public class UHWorldBorderIntegration {
	
	private UHPlugin p = null;
	private WorldBorder wb = null;
	
	public UHWorldBorderIntegration(UHPlugin p) {
		this.p = p;
		
		Plugin wbTest = Bukkit.getServer().getPluginManager().getPlugin("WorldBorder");
		if(wbTest == null || !wbTest.isEnabled()) {
			p.getLogger().warning("WorldBorder is not present, so the integration was disabled.");
			return;
		}
		
		this.wb = (WorldBorder) wbTest;
		
		
		try {
			Class.forName("com.wimbli.WorldBorder.BorderData");
			Class.forName("com.wimbli.WorldBorder.Config");
		}
		catch(ClassNotFoundException e) {
			p.getLogger().warning("WorldBorder is available, but the version you are using is too old.");
			p.getLogger().warning("This plugin is tested and works with WorldBorder 1.8.0 or later.");
			
			this.wb = null;
			return;
		}
		
		
		// All is good, let's integrate.
		setupBorders();
		
		p.getLogger().info("Successfully hooked into WorldBorder.");
	}
	
	public void setupBorders() {
		if(!isWBIntegrationEnabled()) {
			return;
		}
		
		/** General configuration **/
		
		Config.setPortalRedirection(true); // Because the nether is border-less.
		
		
		/** Overworld border **/
		
		World overworld = getOverworld();		
		BorderData borderOverworld = wb.getWorldBorder(overworld.getName());
		
		if(borderOverworld == null) { // The border needs to be created from scratch
			borderOverworld = new BorderData(0, 0, 0); // Random values, overwritten later. 
		}
		
		borderOverworld.setShape(p.getBorderManager().isCircularBorder()); // Squared border
		
		borderOverworld.setX(overworld.getSpawnLocation().getX()); // A border centered on the spawn point
		borderOverworld.setZ(overworld.getSpawnLocation().getZ());
		
		borderOverworld.setRadius((int) Math.floor(p.getBorderManager().getCheckDiameter()/2));
		
		Config.setBorder(overworld.getName(), borderOverworld);
		
		p.getLogger().info("Overworld border configured using WorldBorder (world \"" + overworld.getName() + "\").");
		
		
		/** Nether border **/
		
		// There is not any border set for the Nether, because WorldBorder handles portal redirection
		// if a player rebuild a portal far from the Nether' spawn point.
		
	}
	
	/**
	 * Returns the overworld.
	 * 
	 * @return the... overworld?
	 */
	private World getOverworld() {
		for(World world : Bukkit.getServer().getWorlds()) {
			if(world.getEnvironment() != Environment.NETHER && world.getEnvironment() != Environment.THE_END) {
				return world;
			}
		}
		return null;
	}
	
	public boolean isWBIntegrationEnabled() {
		return !(this.wb == null);
	}
	
	public WorldBorder getWorldBorder() {
		return wb;
	}
}
