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

package eu.carrade.amaury.UHCReloaded;

import eu.carrade.amaury.UHCReloaded.commands.UHCommandExecutor;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.integration.UHDynmapIntegration;
import eu.carrade.amaury.UHCReloaded.integration.UHProtocolLibIntegrationWrapper;
import eu.carrade.amaury.UHCReloaded.listeners.GameplayListener;
import eu.carrade.amaury.UHCReloaded.listeners.SpawnsListener;
import eu.carrade.amaury.UHCReloaded.misc.Freezer;
import eu.carrade.amaury.UHCReloaded.misc.MOTDManager;
import eu.carrade.amaury.UHCReloaded.misc.ProTipsSender;
import eu.carrade.amaury.UHCReloaded.misc.RuntimeCommandsExecutor;
import eu.carrade.amaury.UHCReloaded.recipes.RecipesManager;
import eu.carrade.amaury.UHCReloaded.spawns.SpawnsManager;
import eu.carrade.amaury.UHCReloaded.teams.TeamManager;
import eu.carrade.amaury.UHCReloaded.timers.TimerManager;
import eu.carrade.amaury.UHCReloaded.borders.BorderManager;
import eu.carrade.amaury.UHCReloaded.integration.UHSpectatorPlusIntegration;
import eu.carrade.amaury.UHCReloaded.integration.UHWorldBorderIntegration;
import eu.carrade.amaury.UHCReloaded.listeners.CraftingListener;
import eu.carrade.amaury.UHCReloaded.listeners.GameListener;
import eu.carrade.amaury.UHCReloaded.scoreboard.ScoreboardManager;
import eu.carrade.amaury.UHCReloaded.task.UpdateTimerTask;
import eu.carrade.amaury.UHCReloaded.teams.TeamChatManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;

public class UHCReloaded extends JavaPlugin {

	private TeamManager teamManager = null;
	private SpawnsManager spawnsManager = null;
	private UHGameManager gameManager = null;
	private ScoreboardManager scoreboardManager = null;
	private MOTDManager motdManager = null;
	private BorderManager borderManager = null;
	private RecipesManager recipesManager = null;
	private TeamChatManager teamChatManager = null;
	private TimerManager timerManager = null;

	private RuntimeCommandsExecutor runtimeCommandsExecutor = null;

	private Freezer freezer = null;

	private ProTipsSender protipsSender = null;

	private UHWorldBorderIntegration wbintegration = null;
	private UHSpectatorPlusIntegration spintegration = null;
	private UHDynmapIntegration dynmapintegration = null;
	private UHProtocolLibIntegrationWrapper protocollibintegrationwrapper = null;

	private static I18n i18n;
	private static UHCReloaded instance;

	@Override
	public void onEnable() {
		instance = this;

		this.saveDefaultConfig();

		if(getConfig().getString("lang") == null) {
			i18n = new I18n(this);
		}
		else {
			i18n = new I18n(this, getConfig().getString("lang"));
		}

		teamManager = new TeamManager(this);
		gameManager = new UHGameManager(this);
		spawnsManager = new SpawnsManager(this);
		borderManager = new BorderManager(this);
		recipesManager = new RecipesManager(this);
		teamChatManager = new TeamChatManager(this);
		timerManager = new TimerManager();

		runtimeCommandsExecutor = new RuntimeCommandsExecutor(this);

		freezer = new Freezer(this);

		protipsSender = new ProTipsSender(this);

		scoreboardManager = new ScoreboardManager(this);
		motdManager = new MOTDManager(this);

		wbintegration = new UHWorldBorderIntegration(this);
		spintegration = new UHSpectatorPlusIntegration(this);
		dynmapintegration = new UHDynmapIntegration(this);

		// Needed to avoid a NoClassDefFoundError.
		// I don't like this way of doing this, but else, the plugin will not load without ProtocolLib.
		protocollibintegrationwrapper = new UHProtocolLibIntegrationWrapper(this);

		UHCommandExecutor executor = new UHCommandExecutor(this);
		for(String commandName : getDescription().getCommands().keySet()) {
			getCommand(commandName).setExecutor(executor);
			getCommand(commandName).setTabCompleter(executor);
		}

		getServer().getPluginManager().registerEvents(new GameListener(this), this);
		getServer().getPluginManager().registerEvents(new GameplayListener(this), this);
		getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
		getServer().getPluginManager().registerEvents(new SpawnsListener(this), this);
		// The freezer listener is registered by the freezer when it is needed.

		recipesManager.registerRecipes();
		gameManager.initEnvironment();

		motdManager.updateMOTDBeforeStart();

		// In case of reload
		for(Player player : getServer().getOnlinePlayers()) {
			gameManager.initPlayer(player);
		}

		// Imports spawnpoints from the config.
		this.spawnsManager.importSpawnPointsFromConfig();

		// Imports teams from the config.
		this.teamManager.importTeamsFromConfig();

		// Starts the task that updates the timers.
		// Started here, so a timer can be displayed before the start of the game
		// (example: countdown before the start).
		new UpdateTimerTask(this).runTaskTimer(this, 20l, 20l);

		// Schedule commands
		runtimeCommandsExecutor.registerCommandsInScheduler(RuntimeCommandsExecutor.AFTER_SERVER_START);

		// Launch metrics
		if(getConfig().getBoolean("metrics")) {
			try {
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
		else {
			getLogger().info("Metrics disabled for this plugin in the configuration: nothing was sent.");
		}

		getLogger().info(i18n.t("load.loaded"));
	}

	/**
	 * Returns the team manager.
	 *
	 * @return
	 */
	public TeamManager getTeamManager() {
		return teamManager;
	}

	/**
	 * Returns the game manager.
	 *
	 * @return
	 */
	public UHGameManager getGameManager() {
		return gameManager;
	}

	/**
	 * Returns the scoreboard manager.
	 *
	 * @return
	 */
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	/**
	 * Returns the MOTD manager.
	 *
	 * @return
	 */
	public MOTDManager getMOTDManager() {
		return motdManager;
	}

	/**
	 * Returns the spawns points manager.
	 *
	 * @return
	 */
	public SpawnsManager getSpawnsManager() {
		return spawnsManager;
	}

	/**
	 * Returns the border manager.
	 *
	 * @return
	 */
	public BorderManager getBorderManager() {
		return borderManager;
	}

	/**
	 * Returns the recipe manager.
	 * @return
	 */
	public RecipesManager getRecipesManager() {
		return recipesManager;
	}

	/**
	 * Returns the team-chat manager.
	 * @return
	 */
	public TeamChatManager getTeamChatManager() {
		return teamChatManager;
	}

	/**
	 * Returns the timer manager.
	 * @return
	 */
	public TimerManager getTimerManager() {
		return timerManager;
	}

	/**
	 * Returns the manager used to manage the commands executed after the start/the end of the
	 * game (or any other moment using the generic API).
	 * @return
	 */
	public RuntimeCommandsExecutor getRuntimeCommandsExecutor() {
		return runtimeCommandsExecutor;
	}

	/**
	 * Returns the freezer.
	 *
	 * @return
	 */
	public Freezer getFreezer() {
		return freezer;
	}

	/**
	 * Returns the ProTips sender.
	 *
	 * @return
	 */
	public ProTipsSender getProtipsSender() {
		return protipsSender;
	}

	/**
	 * Returns the representation of the WorldBorder integration in the plugin.
	 *
	 * @return
	 */
	public UHWorldBorderIntegration getWorldBorderIntegration() {
		return wbintegration;
	}

	/**
	 * Returns the representation of the SpectatorPlus integration in the plugin.
	 *
	 * @return
	 */
	public UHSpectatorPlusIntegration getSpectatorPlusIntegration() {
		return spintegration;
	}

	/**
	 * Returns the representation of the dynmap integration in the plugin.
	 *
	 * @return
	 */
	public UHDynmapIntegration getDynmapIntegration() {
		return dynmapintegration;
	}

	/**
	 * Returns a wrapper of the representation of the ProtocolLib integration in the plugin.
	 *
	 * @return
	 */
	public UHProtocolLibIntegrationWrapper getProtocolLibIntegrationWrapper() {
		return protocollibintegrationwrapper;
	}


	/**
	 * Returns the internationalization manager.
	 *
	 * @return
	 */
	public I18n getI18n() {
		return i18n;
	}

	/**
	 * Returns the internationalization manager
	 *
	 * @return
	 */
	public static I18n i() {
		return i18n;
	}

	/**
	 * Returns the plugin's instance.
	 *
	 * @return
	 */
	public static UHCReloaded get()
	{
		return instance;
	}
}
