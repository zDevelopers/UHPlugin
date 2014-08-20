package me.azenet.UHPlugin;

import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.integration.UHDynmapIntegration;
import me.azenet.UHPlugin.integration.UHSpectatorPlusIntegration;
import me.azenet.UHPlugin.integration.UHWorldBorderIntegration;
import me.azenet.UHPlugin.listeners.UHCraftingListener;
import me.azenet.UHPlugin.listeners.UHGameListener;
import me.azenet.UHPlugin.listeners.UHGameplayListener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class UHPlugin extends JavaPlugin {
	
	private UHTeamManager teamManager = null;
	private UHGameManager gameManager = null;
	private UHPluginCommand commandManager = null;
	private UHBorderManager borderManager = null;
	private UHRecipeManager recipeManager = null;
	
	private UHFreezer freezer = null;
	
	private UHWorldBorderIntegration wbintegration = null;
	private UHSpectatorPlusIntegration spintegration = null;
	private UHDynmapIntegration dynmapintegration = null;
	
	private I18n i18n = null;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		if(getConfig().getString("lang") == null) {
			i18n = new I18n(this);
		}
		else {
			try {
				i18n = new I18n(this, getConfig().getString("lang"));
			} catch(IllegalArgumentException e) {
				i18n = new I18n(this);
			}
		}
		
		teamManager = new UHTeamManager(this);
		gameManager = new UHGameManager(this);
		borderManager = new UHBorderManager(this);
		recipeManager = new UHRecipeManager(this);
		
		freezer = new UHFreezer(this);
		
		wbintegration = new UHWorldBorderIntegration(this);
		spintegration = new UHSpectatorPlusIntegration(this);
		dynmapintegration = new UHDynmapIntegration(this);
		
		commandManager = new UHPluginCommand(this);
		getCommand("uh").setExecutor(commandManager);
		getCommand("uh").setTabCompleter(new UHTabCompleter(this));
		
		getCommand("t").setExecutor(commandManager);
		
		getServer().getPluginManager().registerEvents(new UHGameListener(this), this);
		getServer().getPluginManager().registerEvents(new UHGameplayListener(this), this);
		getServer().getPluginManager().registerEvents(new UHCraftingListener(this), this);
		// The freezer listener is registered by the freezer when needed
		
		recipeManager.registerRecipes();
		
		gameManager.initEnvironment();
		gameManager.initScoreboard();
		
		// In case of reload
		for(Player player : getServer().getOnlinePlayers()) {
			gameManager.initPlayer(player);
		}
		
		// Import spawnpoints from config
		this.gameManager.importSpawnPointsFromConfig();
		
		// Import teams from config
		this.teamManager.importTeamsFromConfig();
		
		getLogger().info(i18n.t("load.loaded"));
	}
	
	
	public UHTeamManager getTeamManager() {
		return teamManager;
	}
	
	public UHGameManager getGameManager() {
		return gameManager;
	}
	
	public UHPluginCommand getCommandManager() {
		return commandManager;
	}
	
	public UHBorderManager getBorderManager() {
		return borderManager;
	}
	
	public UHRecipeManager getRecipeManager() {
		return recipeManager;
	}
	
	public UHFreezer getFreezer() {
		return freezer;
	}
	
	
	public UHWorldBorderIntegration getWorldBorderIntegration() {
		return wbintegration;
	}
	
	public UHSpectatorPlusIntegration getSpectatorPlusIntegration() {
		return spintegration;
	}
	
	public UHDynmapIntegration getDynmapIntegration() {
		return dynmapintegration;
	}
	
	
	public I18n getI18n() {
		return i18n;
	}
}
