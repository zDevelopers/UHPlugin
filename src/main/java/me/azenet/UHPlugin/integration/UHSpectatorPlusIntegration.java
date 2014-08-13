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
		spAPI.setArenaClock(false, true);
		spAPI.setSpectateOnDeath(true, true);
		spAPI.setColouredTabList(false, true);
		spAPI.setOutputMessages(false, true);
		spAPI.setBlockCommands(true, true);
		spAPI.setAllowAdminBypassCommandBlocking(true, true);
		spAPI.setSeeSpectators(false, true);
		
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
