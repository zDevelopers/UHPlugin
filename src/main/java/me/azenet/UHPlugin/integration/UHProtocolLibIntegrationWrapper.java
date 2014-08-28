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

import org.bukkit.Bukkit;


public class UHProtocolLibIntegrationWrapper {
	
	private UHPlugin p = null;
	private UHProtocolLibIntegration integration = null;
	
	public UHProtocolLibIntegrationWrapper(UHPlugin p) {
		this.p = p;
		
		// Needed to avoid a NoClassDefFoundError.
		// I don't like this way of doing this, but else, the plugin will not load without ProtocolLib.
		if(Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
			integration = new UHProtocolLibIntegration(p);
		}
		else {
			p.getLogger().warning("ProtocolLib is not present, so the integration was disabled.");
		}
	}
	
	/**
	 * Returns true if ProtocolLib is installed and integrated into the plugin.
	 * @return
	 */
	public boolean isProtocolLibIntegrationEnabled() {
		return (this.integration != null);
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
	
	/**
	 * Returns the wrapped integration.
	 * 
	 * @return
	 */
	public UHProtocolLibIntegration getIntegration() {
		return integration;
	}
}
