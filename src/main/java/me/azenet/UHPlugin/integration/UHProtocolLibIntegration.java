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

import java.util.ArrayList;
import java.util.List;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.listeners.UHPacketsListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public class UHProtocolLibIntegration {
	
	private UHPlugin p = null;
	private ProtocolManager pm = null;
	
	private UHPacketsListener packetsListener = null;
	
	public UHProtocolLibIntegration(UHPlugin p) {
		this.p = p;
		
		Plugin plTest = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
		if(plTest == null || !plTest.isEnabled()) {
			this.p.getLogger().warning("ProtocolLib is not present, so the integration was disabled.");
			return;
		}
		
		
		this.pm = ProtocolLibrary.getProtocolManager();
		this.packetsListener = new UHPacketsListener(p);
		
		if(p.getConfig().getBoolean("hardcore-hearts")) {
			pm.addPacketListener(packetsListener);
		}
		if(p.getConfig().getBoolean("auto-respawn.do")) {
			p.getServer().getPluginManager().registerEvents(packetsListener, p);
		}
		
		
		this.p.getLogger().info("Successfully hooked into ProtocolLib.");
	}
	
	public boolean isProtocolLibIntegrationEnabled() {
		return (this.pm != null);
	}
	
	/**
	 * Checks if there are some enabled option which require ProtocolLib.
	 * 
	 * @return A list of enabled options which requires ProtocolLib, or null 
	 * if there isn't any enabled option that requires ProtocolLib.
	 */
	public List<String> isProtocolLibNeeded() {
		
		ArrayList<String> options = new ArrayList<String>();
		options.add("hardcore-hearts");
		options.add("auto-respawn.do");
		
		ArrayList<String> enabledOptions = new ArrayList<String>();
		
		for(String option : options) {
			if(p.getConfig().getBoolean(option)) {
				enabledOptions.add(option);
			}
		}
		
		if(enabledOptions.size() != 0) {
			return enabledOptions;
		}
		else {
			return null;
		}
	}
}
