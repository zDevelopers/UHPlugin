package me.azenet.UHPlugin;

import java.util.logging.Logger;

import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.integration.UHDynmapIntegration;
import me.azenet.UHPlugin.integration.UHSpectatorPlusIntegration;
import me.azenet.UHPlugin.integration.UHWorldBorderIntegration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class UHPlugin extends JavaPlugin {

	private Logger logger = null;
	
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
		
		logger = this.getLogger();
		
		if(getConfig().getString("lang") == null) {
			i18n = new I18n(this);
		}
		else {
			i18n = new I18n(this, getConfig().getString("lang"));
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
		
		getServer().getPluginManager().registerEvents(new UHPluginListener(this), this);
		
		recipeManager.registerRecipes();
		
		gameManager.initEnvironment();
		gameManager.initScoreboard();
		
		// In case of reload
		for(Player player : getServer().getOnlinePlayers()) {
			gameManager.initPlayer(player);
		}
		
		// Import spawnpoints from config
		if(getConfig().getList("spawnpoints") != null) {
			for(Object position : getConfig().getList("spawnpoints")) {
				if(position instanceof String && position != null) {
					String[] coords = ((String) position).split(",");
					try {
						gameManager.addLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
						getLogger().info(i18n.t("load.spawnPointAdded", coords[0], coords[1]));
					} catch(Exception e) { // Not an integer or not enough coords
						getLogger().warning(i18n.t("load.invalidSpawnPoint", (String) position));
					}
				}
			}
		}
		
		// Import teams from config
		if(getConfig().getList("teams") != null) {
			for(Object teamRaw : getConfig().getList("teams")) {
				if(teamRaw instanceof String && teamRaw != null) {
					String[] teamRawSeparated = ((String) teamRaw).split(",");
					ChatColor color = this.teamManager.getChatColorByName(teamRawSeparated[0]);
					if(color == null) {
						getLogger().warning(i18n.t("load.invalidTeam", (String) teamRaw));
					}
					else {
						if(teamRawSeparated.length == 2) { // "color,name"
							this.teamManager.addTeam(color, teamRawSeparated[1]);
							getLogger().info(i18n.t("load.namedTeamAdded", teamRawSeparated[1],teamRawSeparated[0]));
						}
						else if(teamRawSeparated.length == 1) { // "color"
							this.teamManager.addTeam(color, teamRawSeparated[0]);
							getLogger().info(i18n.t("load.teamAdded", teamRawSeparated[0]));
						}
						else {
							getLogger().warning(i18n.t("load.invalidTeam", (String) teamRaw));
						}
					}
				}
			}
		}
		
		logger.info(i18n.t("load.loaded"));
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
