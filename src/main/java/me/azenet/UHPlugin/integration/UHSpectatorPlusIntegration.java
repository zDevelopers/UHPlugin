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
import org.bukkit.plugin.Plugin;

import com.pgcraft.spectatorplus.SpectateAPI;
import com.pgcraft.spectatorplus.SpectatorPlus;


public class UHSpectatorPlusIntegration {
	
	private UHPlugin p = null;
	private SpectatorPlus sp = null;
	private SpectateAPI spAPI = null;
	
	public UHSpectatorPlusIntegration(UHPlugin p) {
		this.p = p;
		
		Plugin spTest = Bukkit.getServer().getPluginManager().getPlugin("SpectatorPlus");
		if(spTest == null || !spTest.isEnabled()) {
			this.p.getLogger().warning("SpectatorPlus is not present, so the integration was disabled.");
			return;
		}
		
		this.sp = (SpectatorPlus) spTest;
		
		
		try {
			Class.forName("com.pgcraft.spectatorplus.SpectateAPI");
			
			if(sp.getDescription().getVersion().equals("1.9.1")) {
				// The API of SpectatorPlus 1.9.1 was not working.
				throw new ClassNotFoundException();
			}
		}
		catch(ClassNotFoundException e) {
			this.p.getLogger().warning("SpectatorPlus is available, but the version you are using is too old.");
			this.p.getLogger().warning("This plugin is tested and works with SpectatorPlus 1.9.2 or later. The SpectateAPI is needed.");
			
			this.sp = null;
			return;
		}
		
		
		// All is OK, let's integrate.
		this.spAPI = sp.getAPI();
		
		spAPI.setCompass(true, true);
		spAPI.setSpectateOnDeath(true, true);
		spAPI.setColouredTabList(false, true); // potential conflict with our scoreboard
		
		this.p.getLogger().info("Successfully hooked into SpectatorPlus.");
	}
	
	public boolean isSPIntegrationEnabled() {
		return !(this.sp == null);
	}
	
	public SpectatorPlus getSP() {
		return this.sp;
	}
	
	public SpectateAPI getSPAPI() {
		return this.spAPI;
	}
}
