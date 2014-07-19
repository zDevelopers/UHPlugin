package me.azenet.UHPlugin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import com.pgcraft.spectatorplus.SpectateAPI;

public final class UHPlugin extends JavaPlugin {

	private Logger logger = null;
	private ShapelessRecipe goldenMelon = null;
	private ShapedRecipe compass = null;
	private ShapedRecipe goldenAppleFromHead = null;
	private ShapedRecipe goldenAppleFromWitherHead = null;
	private ShapelessRecipe goldenAppleLoreRemover = null;
	
	private UHTeamManager teamManager = null;
	private UHGameManager gameManager = null;
	private UHPluginCommand commandManager = null;
	
	private UHWorldBorderIntegration wbintegration = null;
	private UHSpectatorPlusIntegration spintegration = null;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		logger = this.getLogger();
		teamManager = new UHTeamManager(this);
		gameManager = new UHGameManager(this);
		
		wbintegration = new UHWorldBorderIntegration(this);
		spintegration = new UHSpectatorPlusIntegration(this);
		
		commandManager = new UHPluginCommand(this);
		getCommand("uh").setExecutor(commandManager);
		getCommand("uh").setTabCompleter(new UHTabCompleter(this));
		
		getServer().getPluginManager().registerEvents(new UHPluginListener(this), this);
		
		addRecipes();
		
		gameManager.initEnvironment();
		gameManager.initScoreboard();
		
		// In case of reload
		for(Player player : getServer().getOnlinePlayers()) {
			gameManager.getScoreboardManager().setScoreboardForPlayer(player);
		}
		
		// Import spawnpoints from config
		if(getConfig().getList("spawnpoints") != null) {
			for(Object position : getConfig().getList("spawnpoints")) {
				if(position instanceof String && position != null) {
					String[] coords = ((String) position).split(",");
					try {
						gameManager.addLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
						getLogger().info("Spawn point " + Integer.parseInt(coords[0]) + "," + Integer.parseInt(coords[1]) + " added from the config file.");
					} catch(Exception e) { // Not an integer or not enough coords
						getLogger().warning("Invalid spawn point set in config: " + ((String) position));
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
						getLogger().warning("Invalid team set in config: " + ((String) teamRaw));
					}
					else {
						if(teamRawSeparated.length == 2) { // "color,name"
							this.teamManager.addTeam(color, teamRawSeparated[1]);
							getLogger().info("Team " + teamRawSeparated[1] + " (" + teamRawSeparated[0] + ") added from the config file.");
						}
						else if(teamRawSeparated.length == 1) { // "color"
							this.teamManager.addTeam(color, teamRawSeparated[0]);
							getLogger().info("Team " + teamRawSeparated[0] + " added from the config file.");
						}
						else {
							getLogger().warning("Invalid team set in config: " + ((String) teamRaw));
						}
					}
				}
			}
		}
		
		logger.info("UHPlugin loaded");
	}
	
	@SuppressWarnings("deprecation")
	public void addRecipes() {
		if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.do") || getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.do")) {
			// Golden apple (or notch apple): head in the center and 8 gold ingots.
			
			/** From human head **/
			short damage = 0;
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.craftNotchApple")) {
				damage = 1;
			}
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromHuman")) {
				goldenAppleFromHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.fromHuman.numberCrafted", 1), damage));
				
				goldenAppleFromHead.shape("GGG", "GHG", "GGG");
				goldenAppleFromHead.setIngredient('G', Material.GOLD_INGOT);
				goldenAppleFromHead.setIngredient('H', Material.SKULL_ITEM, SkullType.PLAYER.ordinal()); // TODO: deprecated, but no alternative found...
				
				this.getServer().addRecipe(goldenAppleFromHead);
			}
			
			/** From wither head **/
			damage = 0;
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.craftNotchApple")) {
				damage = 1;
			}
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.do")) {
				goldenAppleFromWitherHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.fromWither.numberCrafted", 1), damage));
				
				goldenAppleFromWitherHead.shape("GGG", "GHG", "GGG");
				goldenAppleFromWitherHead.setIngredient('G', Material.GOLD_INGOT);
				goldenAppleFromWitherHead.setIngredient('H', Material.SKULL_ITEM, SkullType.WITHER.ordinal()); // TODO: deprecated, but no alternative found...
				
				this.getServer().addRecipe(goldenAppleFromWitherHead);
			}
			
			/** Craft to remove the lore **/
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore") || getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore")) {
				goldenAppleLoreRemover = new ShapelessRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, damage));
				goldenAppleLoreRemover.addIngredient(Material.GOLDEN_APPLE);
			
				this.getServer().addRecipe(goldenAppleLoreRemover);
			}
			
			logger.info("Added new recipes for golden apple.");
		}
		
		if(getConfig().getBoolean("gameplay-changes.craftGoldenMelonWithGoldBlock")) {
			// Golden melon: gold block + melon
			
			goldenMelon = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
			goldenMelon.addIngredient(1, Material.GOLD_BLOCK);
			goldenMelon.addIngredient(1, Material.MELON);
			
			this.getServer().addRecipe(goldenMelon);
			logger.info("Added new recipe for golden melon.");
		}
		
		if (getConfig().getBoolean("gameplay-changes.compass")) {
			// Compass: redstone in center;
			// then from the top, clockwise: iron, spider eye, iron, rotten flesh, iron, bone, iron, gunpowder. 
			
			compass = new ShapedRecipe(new ItemStack(Material.COMPASS));
			compass.shape(new String[] {"CIE", "IRI", "BIF"});
			compass.setIngredient('I', Material.IRON_INGOT);
			compass.setIngredient('R', Material.REDSTONE);
			compass.setIngredient('C', Material.SULPHUR);
			compass.setIngredient('E', Material.SPIDER_EYE);
			compass.setIngredient('B', Material.BONE);
			compass.setIngredient('F', Material.ROTTEN_FLESH);
			
			this.getServer().addRecipe(compass);
			logger.info("Added new recipe for compass.");
		}
	}
	
	public Recipe getRecipe(String name) {
		switch(name) {
			case "goldenAppleFromHead":
				return this.goldenAppleFromHead;
			case "goldenAppleFromWitherHead":
				return this.goldenAppleFromWitherHead;
			case "goldenMelon":
				return this.goldenMelon;
			case "compass":
				return this.compass;
		}
		throw new IllegalArgumentException("Unknow recipe");
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
	
	public UHWorldBorderIntegration getWorldBorderIntegration() {
		return wbintegration;
	}
	
	public UHSpectatorPlusIntegration getSpectatorPlusIntegration() {
		return spintegration;
	}
}
