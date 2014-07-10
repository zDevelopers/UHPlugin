package me.azenet.UHPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
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
	private UHTeamManager teamManager = null;
	private UHGameManager gameManager = null;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		
		logger = Bukkit.getLogger();
		teamManager = new UHTeamManager(this);
		gameManager = new UHGameManager(this);
		
		getCommand("uh").setExecutor(new UHPluginCommand(this));
		getServer().getPluginManager().registerEvents(new UHPluginListener(this), this);
		
		addRecipes();
		
		gameManager.initScoreboard();
		gameManager.setMatchInfo();
		gameManager.initEnvironment();
		
		
		File positions = new File("plugins/UHPlugin/positions.txt");
		if (positions.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(positions));
				String line;
				while ((line = br.readLine()) != null) {
					String[] l = line.split(",");
					getLogger().info("Adding position "+Integer.parseInt(l[0])+","+Integer.parseInt(l[1])+" from positions.txt");
					gameManager.addLocation(Integer.parseInt(l[0]), Integer.parseInt(l[1]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try { if (br != null) br.close(); }
				catch (Exception e) { e.printStackTrace(); } //c tré l'inline
			}
			
		}
		
		logger.info("UHPlugin loaded");
	}
	
	public void addRecipes() {
		if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do")) {
			// Golden apple (or notch apple): head in the center and 8 gold ingots.
			
			short damage = 0;
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.craftNotchApple")) {
				damage = 1;
			}
			
			goldenAppleFromHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.numberCrafted.fromPlayer", 1), damage));
			
			goldenAppleFromHead.shape("GGG", "GHG", "GGG");
			goldenAppleFromHead.setIngredient('G', Material.GOLD_INGOT);
			goldenAppleFromHead.setIngredient('H', Material.SKULL_ITEM, SkullType.PLAYER.ordinal()); // TODO: deprecated, but no alternative found...
			
			this.getServer().addRecipe(goldenAppleFromHead);
			
			if(getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.doFromWither")) {
				goldenAppleFromWitherHead = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, getConfig().getInt("gameplay-changes.craftGoldenAppleFromHead.numberCrafted.fromWither", 1), damage));
				
				goldenAppleFromWitherHead.shape("GGG", "GHG", "GGG");
				goldenAppleFromWitherHead.setIngredient('G', Material.GOLD_INGOT);
				goldenAppleFromWitherHead.setIngredient('H', Material.SKULL_ITEM, SkullType.WITHER.ordinal()); // TODO: deprecated, but no alternative found...
				
				this.getServer().addRecipe(goldenAppleFromWitherHead);
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
	public void generateWalls(World w) {
		Integer halfMapSize = (int) Math.floor(this.getConfig().getInt("map.size")/2);
		Integer wallHeight = this.getConfig().getInt("map.wall.height");
		Material wallBlock = Material.getMaterial(this.getConfig().getInt("map.wall.block"));
		
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
	}
	
	public UHTeamManager getTeamManager() {
		return teamManager;
	}
	
	public UHGameManager getGameManager() {
		return gameManager;
	}
	
}
