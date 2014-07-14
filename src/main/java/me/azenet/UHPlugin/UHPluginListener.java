package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class UHPluginListener implements Listener {

	UHPlugin p = null;
	
	public UHPluginListener(UHPlugin p) {
		this.p = p;
	}
	
	
	/**
	 * Used to:
	 *  - play the death sound;
	 *  - update the scoreboard;
	 *  - kick the player (if needed);
	 *  - broadcast a team-death message (if needed);
	 *  - increase visibility of the death message (if needed);
	 *  - drop the skull of the dead player (if needed);
	 *  - update the number of alive players/teams.
	 *  
	 * @param ev
	 */
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent ev) {
		// This needs to be executed only if the player die as a player, not a spectator.
		// Also, the game needs to be started.
		if(p.getGameManager().isPlayerDead(ev.getEntity().getName()) || !p.getGameManager().isGameRunning()) {
			return;
		}
		
		// Plays sound.
		Player[] ps = Bukkit.getServer().getOnlinePlayers();
		for (Player pp : ps) {
			pp.playSound(pp.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
		}
		
		// Removes the player from the alive players.
		this.p.getGameManager().addDead(ev.getEntity().getName());
		
		// Kicks the player if needed.
		if (this.p.getConfig().getBoolean("kick-on-death.kick", true)) {
			Bukkit.getScheduler().runTaskLater(this.p, new BukkitRunnable() {
				
				@Override
				public void run() {
					ev.getEntity().kickPlayer("jayjay");
				}
			}, 20L*this.p.getConfig().getInt("kick-on-death.time", 30));
		}
		
		// Drops the skull of the player.
		Location l = ev.getEntity().getLocation();
		try { 
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(((Player)ev.getEntity()).getName());
			skullMeta.setDisplayName(ChatColor.RESET + ((Player)ev.getEntity()).getName());
			skull.setItemMeta(skullMeta);
			l.getWorld().dropItem(l, skull);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sends a team-death message if needed.
		if(p.getConfig().getBoolean("death-messages.notifyIfTeamHasFallen", false)) {
			UHTeam team = p.getTeamManager().getTeamForPlayer(ev.getEntity());
			if(team != null) {
				boolean isAliveTeam = false;
				
				for(Player player : team.getPlayers()) {
					if(!p.getGameManager().isPlayerDead(player.getName())) {
						isAliveTeam = true;
						break;
					}
				}
				
				if(!isAliveTeam) {
					p.getServer().broadcastMessage(p.getConfig().getString("death-messages.teamDeathMessagesPrefix", "") + "The team " + ChatColor.RESET + team.getChatColor() + team.getDisplayName() + ChatColor.RESET + p.getConfig().getString("death-messages.teamDeathMessagesPrefix", "") + " has fallen!");
				}
			}
			else {
				p.getLogger().warning("Team null?!");
			}
		}
		
		ev.setDeathMessage(p.getConfig().getString("death-messages.deathMessagesPrefix", "") + ev.getDeathMessage());
		
		// Updates the number of alive players/teams
		p.getGameManager().updateAliveCounters();
	}
	
	
	/**
	 * Used to prevent the user to get a ghast tear, if forbidden by the config.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent ev) {
		if (ev.getItem().getItemStack().getType() == Material.GHAST_TEAR && ev.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && p.getConfig().getBoolean("gameplay-changes.replaceGhastTearsWithGold")) {
			ev.setCancelled(true);
		}
	}
	
	
	
	/**
	 * Used to prevent the player to login after his death (if needed).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent ev) {
		if (this.p.getGameManager().isPlayerDead(ev.getPlayer().getName()) && !this.p.getConfig().getBoolean("kick-on-death.allow-reconnect", true)) {
			ev.setResult(Result.KICK_OTHER);
			ev.setKickMessage("Vous êtes mort !");
		}
	}
	
	/**
	 * Used to:
	 *  - change the gamemode of the player, if the game is not running;
	 *  - teleport the player to the spawn, if the game is not running;
	 *  - update the scoreboard.
	 * @param ev
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev) {
		if (!this.p.getGameManager().isGameRunning()) {
			ev.getPlayer().setGameMode(GameMode.CREATIVE);
			Location l = ev.getPlayer().getWorld().getSpawnLocation();
			ev.getPlayer().teleport(l.add(0,1,0));
			
			ev.getPlayer().setFoodLevel(20);
			ev.getPlayer().setSaturation(14);
			
			// Used to update the "health" objective, to avoid a null one.
			ev.getPlayer().setHealth(19D);
			ev.getPlayer().setHealth(20D);
		}
		
		p.getGameManager().getScoreboardManager().setScoreboardForPlayer(ev.getPlayer());
		
		// A warning to the administrators if WorldBorder is not present.
		if(ev.getPlayer().hasPermission("uh.*") && !p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
			ev.getPlayer().sendMessage(ChatColor.RED + "WorldBorder is not installed, you should use it with the Ultra Hardcore plugin.");
			ev.getPlayer().sendMessage(ChatColor.GRAY + "Why? Optimized border check, pregenerated world (fill)... Also, the border can be reduced during the game.");
			ev.getPlayer().sendMessage(ChatColor.GRAY + "It's as simple as putting the WorldBorder jar inside the plugins directory, UHPlugin will automatically configure it.");
		}
	}
	
	
	/**
	 * Used to prevent players from breaking blocks if the game is not currently running.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockBreakEvent(final BlockBreakEvent ev) {
		if (!this.p.getGameManager().isGameRunning() && !((Player)ev.getPlayer()).hasPermission("uh.build")) {
			ev.setCancelled(true);
		}
	}
	
	/**
	 * Used to prevent players from placing blocks if the game is not currently running.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBlockPlaceEvent(final BlockPlaceEvent ev) {
		if (!this.p.getGameManager().isGameRunning() && !((Player)ev.getPlayer()).hasPermission("uh.build")) {
			ev.setCancelled(true);
		}
	}
	
	/**
	 * Used to:
	 *  - prevent the player to go outside the border;
	 *  - freeze the players during the (slow) start.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev) {
		if(p.getGameManager().getSlowStartInProgress()) {
			ev.setCancelled(true);
		}
		
		if(!p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
			Location l = ev.getTo();
			Integer mapSize = p.getConfig().getInt("map.size");
			Integer halfMapSize = (int) Math.floor(mapSize/2);
			Integer x = l.getBlockX();
			Integer z = l.getBlockZ();
			
			Location spawn = ev.getPlayer().getWorld().getSpawnLocation();
			Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();
			
			spawn = ev.getPlayer().getWorld().getSpawnLocation();
			Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();
			
			spawn = ev.getPlayer().getWorld().getSpawnLocation();
			Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();
			
			spawn = ev.getPlayer().getWorld().getSpawnLocation();
			Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();
			
			if (x < limitXInf || x > limitXSup || z < limitZInf || z > limitZSup) {
				ev.setCancelled(true);
			}
		}
	}
	
	
	/**
	 * Used to:
	 *  - prevent items to be crafted;
	 *  - add a lure to the golden apples crafted from a head.
	 * @param pce
	 */
	@EventHandler
	public void onPreCraftEvent(PrepareItemCraftEvent ev) {
		Recipe recipe = ev.getRecipe();
		
		if(recipe == null) {
			return;
		}
		
		/** Prevent items to be crafted **/
		
		// Original recipes, for comparison
		ShapedRecipe originalCompass = new ShapedRecipe(new ItemStack(Material.COMPASS));
		originalCompass.shape(new String[] {" I ", "IRI", " I "});
		originalCompass.setIngredient('I', Material.IRON_INGOT);
		originalCompass.setIngredient('R', Material.REDSTONE);
		
		ShapedRecipe originalGoldenMelon = new ShapedRecipe(new ItemStack(Material.SPECKLED_MELON));
		originalGoldenMelon.shape(new String[] {"GGG", "GMG", "GGG"});
		originalGoldenMelon.setIngredient('G', Material.GOLD_NUGGET);
		originalGoldenMelon.setIngredient('M', Material.MELON);
		
		// Compass
		if(p.getConfig().getBoolean("gameplay-changes.compass") && RecipeUtil.areSimilar(recipe, originalCompass)) {
			ev.getInventory().setResult(new ItemStack(Material.AIR));
		}
		
		// Golden melon
		if(p.getConfig().getBoolean("gameplay-changes.craftGoldenMelonWithGoldBlock") && RecipeUtil.areSimilar(recipe, originalGoldenMelon)) {
			ev.getInventory().setResult(new ItemStack(Material.AIR));
		}
		
		
		/** Add a lore to the golden apples crafted from a head **/
		
		if((p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromHuman") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.do.fromWither")) && p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.addLore") && (RecipeUtil.areSimilar(recipe, p.getRecipe("goldenAppleFromHead")) || RecipeUtil.areSimilar(recipe, p.getRecipe("goldenAppleFromWitherHead")))) {	   	
			ItemStack result = ev.getInventory().getResult();
			ItemMeta meta = result.getItemMeta();
			
			// Lookup for the head in the recipe
			String name = "a malignant monster";
			for(ItemStack item : ev.getInventory().getContents()) {
				if(item.getType() == Material.SKULL_ITEM && item.getDurability() == (short) SkullType.PLAYER.ordinal()) { // An human head
					SkullMeta sm = (SkullMeta) item.getItemMeta();
					if(sm.hasOwner()) { // An human head
						name = sm.getOwner();
					}
					break;
				}
			}
			
			List<String> lore = Arrays.asList("Made from the fallen head", "of " + name);
			meta.setLore(lore);
			
			result.setItemMeta(meta);
			ev.getInventory().setResult(result);
		}
	}
	
	
	/**
	 * Used to disable ghast tears (if needed).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev) {
		if (ev.getEntity() instanceof Ghast && p.getConfig().getBoolean("gameplay-changes.replaceGhastTearsWithGold")) {
			Bukkit.getLogger().info("Modifying drops for Ghast");
			List<ItemStack> drops = new ArrayList<ItemStack>(ev.getDrops());
			ev.getDrops().clear(); 
			for (ItemStack i : drops) {
				if (i.getType() == Material.GHAST_TEAR) {
					Bukkit.getLogger().info("Added "+i.getAmount()+" ghast tear(s)");
					ev.getDrops().add(new ItemStack(Material.GOLD_INGOT,i.getAmount()));
				} else {
					Bukkit.getLogger().info("Added "+i.getAmount()+" "+i.getType().toString());
					ev.getDrops().add(i);
				}
			}
		}
	}

	
	/**
	 * Used to disable any damages if the game has not started.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (!p.getGameManager().isTakingDamage()) ev.setCancelled(true);
		}
	}
	
	/**
	 * Used to disable enderpearl damages (if needed)
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerTeleport(final PlayerTeleportEvent ev) {
		if(p.getConfig().getBoolean("gameplay-changes.disableEnderpearlsDamages")) {
			Player player = ev.getPlayer();
			TeleportCause cause = ev.getCause();
			Location target = ev.getTo();
			
			if(cause == TeleportCause.ENDER_PEARL) {
				ev.setCancelled(true);
				player.teleport(target);
			}
		}
	}
	
	/**
	 * Used to prevent the life to be gained with food.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityRegainHealth(final EntityRegainHealthEvent ev) {
		if (ev.getRegainReason() == RegainReason.SATIATED) ev.setCancelled(true);
	}
	
	/**
	 * Used to prevent the food level from dropping if the game has not started.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onFoodUpdate(FoodLevelChangeEvent ev) {
		if(!p.getGameManager().isGameRunning()) {
			((Player) ev.getEntity()).setFoodLevel(20);
			((Player) ev.getEntity()).setSaturation(14);

			ev.setCancelled(true);
		}
	}
	
	
	/**
	 * Used to update the compass.
	 * 
	 * @param ev
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent ev) {
		if ((ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK) && ev.getPlayer().getItemInHand().getType() == Material.COMPASS && p.getConfig().getBoolean("gameplay-changes.compass")) {
			Player pl = ev.getPlayer();
			Boolean foundRottenFlesh = false;
			for (ItemStack is : pl.getInventory().getContents()) {
				if (is != null && is.getType() == Material.ROTTEN_FLESH) {
					p.getLogger().info(""+is.getAmount());
					if (is.getAmount() != 1) is.setAmount(is.getAmount()-1);
					else { pl.getInventory().removeItem(is); }
					pl.updateInventory();
					foundRottenFlesh = true;
					break;
				}
			}
			if (!foundRottenFlesh) {
				pl.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Vous n'avez pas de chair de zombie.");
				pl.playSound(pl.getLocation(), Sound.STEP_WOOD, 1F, 1F);
				return;
			}
			pl.playSound(pl.getLocation(), Sound.BURP, 1F, 1F);
			Player nearest = null;
			Double distance = 99999D;
			for (Player pl2 : p.getServer().getOnlinePlayers()) {
				try {	
					Double calc = pl.getLocation().distance(pl2.getLocation());
					if (calc > 1 && calc < distance) {
						distance = calc;
						if (pl2 != pl && !this.p.getTeamManager().inSameTeam(pl, pl2)) nearest = pl2.getPlayer();
					}
				} catch (Exception e) {}
			}
			if (nearest == null) {
				pl.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Seul le silence comble votre requête.");
				return;
			}
			pl.sendMessage(ChatColor.GRAY+"La boussole pointe sur le joueur le plus proche.");
			pl.setCompassTarget(nearest.getLocation());
		}
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent ev) {
		if (!p.getConfig().getBoolean("gameplay-changes.weather")) {
			ev.setCancelled(true);
		}
	}
}
