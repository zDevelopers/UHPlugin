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

package me.azenet.UHPlugin.integration;

import me.azenet.UHPlugin.TeamColor;
import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;
import me.azenet.UHPlugin.i18n.I18n;

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
	private I18n i = null;
	private DynmapAPI api = null;
	private MarkerAPI markerAPI = null;
	private MarkerSet markerSet = null;
	
	public UHDynmapIntegration(UHPlugin plugin) {
		this.p = plugin;
		this.i = p.getI18n();
		
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
	
	/**
	 * Displays the death location of the given player.
	 * 
	 * @param player The player.
	 */
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
		
		String markerID = getDeathMarkerName(player);
		String markerLabel = i.t("dynmap.markerLabelDeath", player.getName());
		MarkerIcon icon = markerAPI.getMarkerIcon("skull");
		
		Marker marker = markerSet.createMarker(markerID, markerLabel, true, deathPoint.getWorld().getName(), deathPoint.getX(), deathPoint.getY(), deathPoint.getZ(), icon, false);
		if(marker == null) {
			p.getLogger().warning("Unable to create marker " + markerID);
		}
	}
	
	/**
	 * Hides the death location of the given player.
	 * 
	 * @param player The player.
	 */
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
	
	/**
	 * Returns the internal ID of the marker of the death point of the given player.
	 * 
	 * @param player The player.
	 * @return The ID.
	 */
	private String getDeathMarkerName(Player player) {
		return "uhplugin.death." + player.getName();
	}
	
	
	
	/** Spawn locations **/
	
	/**
	 * Displays the spawn point of the given team.
	 * 
	 * @param team The team.
	 * @param spawnPoint The location of the spawn point.
	 */
	public void showSpawnLocation(UHTeam team, Location spawnPoint) {
		if(!isDynmapIntegrationEnabled()) {
			return;
		}
		
		if(!p.getConfig().getBoolean("dynmap.showSpawnLocations")) {
			return;
		}
		
		// Let's try to find the best icon
		// Available flags:
		// redflag, orangeflag, yellowflag, greenflag, blueflag, purpleflag, pinkflag, pirateflag (black)
		// Ref. https://github.com/webbukkit/dynmap/wiki/Using-markers
		
		MarkerIcon icon = null;
		
		TeamColor teamColor = team.getColor();
		if(teamColor == null) {
			teamColor = TeamColor.GREEN; // green flags for solo games without colors
		}
		
		switch(teamColor) {
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
			default:
				icon = markerAPI.getMarkerIcon("pinkflag");
				break;
		}
		
		String markerID = getSpawnMarkerName(team);
		String markerLabel = null;
		if(p.getGameManager().isGameWithTeams()) {
			markerLabel = i.t("dynmap.markerLabelSpawn", team.getName());
		}
		else {
			markerLabel = i.t("dynmap.markerLabelSpawnNoTeam", team.getName());
		}
		
		Marker marker = markerSet.createMarker(markerID, markerLabel, true, spawnPoint.getWorld().getName(), spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ(), icon, false);
		
		if(marker == null) {
			p.getLogger().warning("Unable to create marker " + markerID);
		}
	}
	
	/**
	 * Returns the internal ID of the marker of the spawn point of the given team.
	 * 
	 * @param team The team.
	 * @return The ID.
	 */
	private String getSpawnMarkerName(UHTeam team) {
		return "uhplugin.spawn." + team.getName();
	}
}
