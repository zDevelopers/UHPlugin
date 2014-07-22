package me.azenet.UHPlugin.integration;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class UHDynmapIntegration {
	
	private UHPlugin p = null;
	private DynmapAPI api = null;
	private MarkerAPI markerAPI = null;
	private MarkerSet markerSet = null;
	
	public UHDynmapIntegration(UHPlugin plugin) {
		this.p = plugin;
		
		Plugin apiTest = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
		if(apiTest == null || !apiTest.isEnabled()) {
			p.getLogger().warning("Dynmap is not present, so the integration was disabled.");
			return;
		}
		
		this.api = (DynmapAPI) apiTest;
		
		markerAPI = api.getMarkerAPI();
		if (markerAPI == null) {
			p.getLogger().warning("Dynmap is available, but the markers API is not. The integration was disabled.");
			return;
		}
		
		
		// All is good, let's integrate.
		initDynmapIntegration();
		
		p.getLogger().info("Successfully hooked into Dynmap.");
	}
	
	private void initDynmapIntegration() {
		
		markerSet = markerAPI.getMarkerSet("uhplugin.markerset");
		
		if(markerSet == null) {
			markerSet = markerAPI.createMarkerSet("uhplugin.markerset", "UltraHardcore", null, false);
		}
		else {
			markerSet.setMarkerSetLabel("UltraHardcore");
		}
	}
	
	public void shutdownDynmapIntegration() {
		if(isDynmapIntegrationEnabled()) {
			markerSet.deleteMarkerSet();
		}
	}
	
	public boolean isDynmapIntegrationEnabled() {
		return !(this.api == null);
	}
	
	public DynmapAPI getDynmapAPI() {
		return api;
	}
	
	public MarkerAPI getDynmapMarkerAPI() {
		return markerAPI;
	}
	
	/** Death locations **/
	
	public void showDeathLocation(Player player) {
		if(!isDynmapIntegrationEnabled()) {
			return;
		}
		
		if(!p.getConfig().getBoolean("dynmap.showDeathLocations")) {
			return;
		}
		
		if(!p.getGameManager().hasDeathLocation(player)) {
			return;
		}
		
		Location deathPoint = p.getGameManager().getDeathLocation(player);
		
		p.getLogger().info("Adding marker for death. " + deathPoint.toString());
		
		String markerID = getDeathMarkerName(player);
		String markerLabel = "Death point of " + player.getName();
		MarkerIcon icon = markerAPI.getMarkerIcon("skull");
		
		Marker marker = markerSet.createMarker(markerID, markerLabel, true, deathPoint.getWorld().getName(), deathPoint.getX(), deathPoint.getY(), deathPoint.getZ(), icon, false);
		if(marker == null) {
			p.getLogger().warning("Unable to create marker " + markerID);
		}
	}
	
	public void hideDeathLocation(Player player) {
		if(!isDynmapIntegrationEnabled()) {
			return;
		}
		
		if(!p.getConfig().getBoolean("dynmap.showDeathLocations")) {
			return;
		}
		
		Marker marker = markerSet.findMarker(getDeathMarkerName(player));
		if(marker != null) {
			marker.deleteMarker();
		}
	}
	
	private String getDeathMarkerName(Player player) {
		return "uhplugin.death." + player.getName();
	}
	
	/** Spawn locations **/
	
	public void showSpawnLocation(UHTeam team, Location spawnPoint) {
		if(!isDynmapIntegrationEnabled()) {
			return;
		}
		
		if(!p.getConfig().getBoolean("dynmap.showSpawnLocations")) {
			return;
		}
		
		p.getLogger().info("Adding marker for spawn. " + spawnPoint.toString());
		
		// Let's try to find the best icon
		// Available flags:
		// redflag, orangeflag, yellowflag, greenflag, blueflag, purpleflag, pinkflag, pirateflag (black)
		// Ref. https://github.com/webbukkit/dynmap/wiki/Using-markers
		
		MarkerIcon icon = null;
		switch(team.getChatColor()) {
			case BLUE:
			case DARK_BLUE:
			case AQUA:
			case DARK_AQUA:
				icon = markerAPI.getMarkerIcon("blueflag");
				break;
				
			case GREEN:
			case DARK_GREEN:
				icon = markerAPI.getMarkerIcon("greenflag");
				break;
				
			case GOLD:
				icon = markerAPI.getMarkerIcon("orangeflag");
				break;
				
			case YELLOW:
				icon = markerAPI.getMarkerIcon("yellowflag");
				break;
				
			case RED:
			case DARK_RED:
				icon = markerAPI.getMarkerIcon("redflag");
				break;
			
			case DARK_PURPLE:
				icon = markerAPI.getMarkerIcon("purpleflag");
				break;
			
			case LIGHT_PURPLE:
				icon = markerAPI.getMarkerIcon("pinkflag");
				break;
				
			case BLACK:
			case DARK_GRAY:
			case GRAY:
				icon = markerAPI.getMarkerIcon("pirateflag");
				break;
				
			case WHITE: // There is nothing better than pink I think...
				icon = markerAPI.getMarkerIcon("pinkflag");
				break;
				
			default:
				break;
			
		}
		
		String markerID = getSpawnMarkerName(team);
		String markerLabel = "Spawn point of the team " + team.getName();
		
		Marker marker = markerSet.createMarker(markerID, markerLabel, true, spawnPoint.getWorld().getName(), spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), icon, false);
		if(marker == null) {
			p.getLogger().warning("Unable to create marker " + markerID);
		}
	}
	
	private String getSpawnMarkerName(UHTeam team) {
		return "uhplugin.spawn." + team.getName();
	}
}
