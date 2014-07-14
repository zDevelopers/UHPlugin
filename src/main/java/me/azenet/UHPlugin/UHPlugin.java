package me.azenet.UHPlugin;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

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

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		logger = Bukkit.getLogger();
		teamManager = new UHTeamManager(this);
		gameManager = new UHGameManager(this);
		
		wbintegration = new UHWorldBorderIntegration(this);
		
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
		
		logger.info("UHPlugin loaded");
	}
	
	@SuppressWarnings("deprecation")
	public void addRecipes() {
		if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromHuman") || getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromWither")) {
			// Golden apple (or notch apple): head in the center and 8 gold ingots.
			
			short damage = 0;
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.craftNotchApple")) {
				damage = 1;
			}
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromHuman")) {
				goldenAppleFromHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.numberCrafted.fromPlayer", 1), damage));
				
				goldenAppleFromHead.shape("GGG", "GHG", "GGG");
				goldenAppleFromHead.setIngredient('G', Material.GOLD_INGOT);
				goldenAppleFromHead.setIngredient('H', Material.SKULL_ITEM, SkullType.PLAYER.ordinal()); // TODO: deprecated, but no alternative found...
				
				this.getServer().addRecipe(goldenAppleFromHead);
			}
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromWither")) {
				goldenAppleFromWitherHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.numberCrafted.fromWither", 1), damage));
				
				goldenAppleFromWitherHead.shape("GGG", "GHG", "GGG");
				goldenAppleFromWitherHead.setIngredient('G', Material.GOLD_INGOT);
				goldenAppleFromWitherHead.setIngredient('H', Material.SKULL_ITEM, SkullType.WITHER.ordinal()); // TODO: deprecated, but no alternative found...
				
				this.getServer().addRecipe(goldenAppleFromWitherHead);
			}
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.addLore")) {
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
			// then from the top, clockwise: iron, spider eye, iron, rotten flesh, iron, bone, iron, gun powder. 
			
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

	@Override
	public void onDisable() {
		logger.info("UHPlugin unloaded");
	}
	
	
	/**
	 * Generate the walls around the map.
	 * 
	 * @throws Exception
	 */
	public boolean generateWalls(World w) {
		Integer halfMapSize = (int) Math.floor(this.getConfig().getInt("map.size")/2);
		Integer wallHeight = this.getConfig().getInt("map.wall.height");
		Material wallBlock = Material.getMaterial(this.getConfig().getString("map.wall.block").toUpperCase());
		
		if(wallBlock == null || !wallBlock.isSolid()) {
			logger.severe("Unable to build the wall, the block set in the config file is invalid.");
			return false;
		}
		
		Location spawn = w.getSpawnLocation();
		Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
		
		spawn = w.getSpawnLocation();
		Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
		
		spawn = w.getSpawnLocation();
		Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
		
		spawn = w.getSpawnLocation();
		Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
		
		for (Integer x = limitXInf; x <= limitXSup; x++) {
			w.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
			w.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);
			for (Integer y = 2; y <= wallHeight; y++) {
				w.getBlockAt(x, y, limitZInf).setType(wallBlock);
				w.getBlockAt(x, y, limitZSup).setType(wallBlock);
			}
		} 
		
		for (Integer z = limitZInf; z <= limitZSup; z++) {
			w.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
			w.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);
			for (Integer y = 2; y <= wallHeight; y++) {
				w.getBlockAt(limitXInf, y, z).setType(wallBlock);
				w.getBlockAt(limitXSup, y, z).setType(wallBlock);
			}
		}
		
		return true;
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
}
