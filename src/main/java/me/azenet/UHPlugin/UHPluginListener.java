package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.event.inventory.BrewEvent;
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
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class UHPluginListener implements Listener {

	private UHPlugin p = null;
	private I18n i = null;
	
	public UHPluginListener(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	
	/**
	 * Used to:
	 *  - play the death sound;
	 *  - update the scoreboard;
	 *  - kick the player (if needed);
	 *  - broadcast a team-death message (if needed);
	 *  - increase visibility of the death message (if needed);
	 *  - drop the skull of the dead player (if needed);
	 *  - update the number of alive players/teams;
	 *  - save the location of the death of the player, to allow a teleportation later;
	 *  - show the death location on the dynmap (if needed);
	 *  - give XP to the killer (if needed).
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
		if (this.p.getConfig().getBoolean("death.kick.do", true)) {
			Bukkit.getScheduler().runTaskLater(this.p, new BukkitRunnable() {
				
				@Override
				public void run() {
					ev.getEntity().kickPlayer(i.t("death.kickMessage"));
				}
			}, 20L * this.p.getConfig().getInt("death.kick.time", 30));
		}
		
		// Drops the skull of the player.
		if(p.getConfig().getBoolean("death.head.drop")) {
			if(!p.getConfig().getBoolean("death.head.pvpOnly")
					|| (p.getConfig().getBoolean("death.head.pvpOnly") && ev.getEntity().getKiller() != null && ev.getEntity().getKiller() instanceof Player)) {
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
			}
		}
		
		// Give XP to the killer (if needed)
		if(p.getConfig().getInt("death.give-xp.levels") > 0) {
			Entity killer = ev.getEntity().getKiller();
			if(killer != null && killer instanceof Player) {
				((Player) killer).giveExpLevels(p.getConfig().getInt("death.give-xp.levels"));
			}
		}
		
		// Sends a team-death message if needed.
		if(p.getConfig().getBoolean("death.messages.notifyIfTeamHasFallen", false)) {
			UHTeam team = p.getTeamManager().getTeamForPlayer((Player) ev.getEntity());
			if(team != null) {
				boolean isAliveTeam = false;
				
				for(Player player : team.getPlayers()) {
					if(!p.getGameManager().isPlayerDead(player.getName())) {
						isAliveTeam = true;
						break;
					}
				}
				
				if(!isAliveTeam) {
					p.getServer().broadcastMessage(i.t("death.teamHasFallen", p.getConfig().getString("death.messages.teamDeathMessagesPrefix", ""), team.getChatColor() + team.getDisplayName() + p.getConfig().getString("death.messages.teamDeathMessagesPrefix", "")));
				}
			}
		}
		
		// Customizes the death message
		ev.setDeathMessage(p.getConfig().getString("death.messages.deathMessagesPrefix", "") + ev.getDeathMessage());
		
		// Updates the number of alive players/teams
		p.getGameManager().updateAliveCounters();
		p.getGameManager().getScoreboardManager().updateScoreboard();
		
		// Saves the location of the death
		p.getGameManager().addDeathLocation(ev.getEntity(), ev.getEntity().getLocation());
		
		// Shows the death location on the dynmap
		p.getDynmapIntegration().showDeathLocation(ev.getEntity());
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
		if (this.p.getGameManager().isPlayerDead(ev.getPlayer().getName()) && !this.p.getConfig().getBoolean("death.kick.allow-reconnect", true)) {
			ev.setResult(Result.KICK_OTHER);
			ev.setKickMessage(i.t("death.banMessage"));
		}
	}
	
	/**
	 * Used to:
	 *  - change the gamemode of the player, if the game is not running;
	 *  - teleport the player to the spawn, if the game is not running;
	 *  - update the scoreboard;
	 *  - resurrect a player (if the player was offline).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent ev) {
		if (!this.p.getGameManager().isGameRunning()) {
			Location l = ev.getPlayer().getWorld().getSpawnLocation();
			ev.getPlayer().teleport(l.add(0,1,0));
			
			ev.getPlayer().setFoodLevel(20);
			ev.getPlayer().setSaturation(14);
			
			// Used to update the "health" objective, to avoid a null one.
			p.getGameManager().getScoreboardManager().updateHealthScore(ev.getPlayer());
			
			// Disable the spectator mode if the game is not started.
			if(p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
				p.getSpectatorPlusIntegration().getSPAPI().setSpectating(ev.getPlayer(), false);
			}

			ev.getPlayer().setGameMode(GameMode.CREATIVE);
		}
		
		// Mainly useful on the first join.
		p.getGameManager().getScoreboardManager().setScoreboardForPlayer(ev.getPlayer());
		
		// The display name is reset when the player log off.
		p.getTeamManager().colorizePlayer(ev.getPlayer());
		
		if(!p.getGameManager().isGameRunning() && ev.getPlayer().hasPermission("uh.*")) {
			// A warning to the administrators if WorldBorder is not present.
			if(!p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
				ev.getPlayer().sendMessage(i.t("load.WBNotInstalled1"));
				ev.getPlayer().sendMessage(i.t("load.WBNotInstalled2"));
				ev.getPlayer().sendMessage(i.t("load.WBNotInstalled3"));
			}
			
			// The same for SpectatorPlus
			if(!p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
				ev.getPlayer().sendMessage(i.t("load.SPNotInstalled1"));
				ev.getPlayer().sendMessage(i.t("load.SPNotInstalled2"));
			}
		}
		
		// If the player needs to be resurrected...
		if(p.getGameManager().isDeadPlayersToBeResurrected(ev.getPlayer())) {
			p.getGameManager().resurrectPlayerOnlineTask(ev.getPlayer());
			p.getGameManager().markPlayerAsResurrected(ev.getPlayer());
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
		
		/** Prevents items to be crafted **/
		
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
		
		
		/** Adds a lore to the golden apples crafted from a head **/
		
		if((p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.do") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.do")) 
				&& (p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore") || p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore")) 
				&& (RecipeUtil.areSimilar(recipe, p.getRecipe("goldenAppleFromHead")) || RecipeUtil.areSimilar(recipe, p.getRecipe("goldenAppleFromWitherHead")))) {	   	
			
			ItemStack result = ev.getInventory().getResult();
			ItemMeta meta = result.getItemMeta();
			
			// Lookup for the head in the recipe
			String name = "";
			Boolean wither = true;
			for(ItemStack item : ev.getInventory().getContents()) {
				if(item.getType() == Material.SKULL_ITEM && item.getDurability() == (short) SkullType.PLAYER.ordinal()) { // An human head
					SkullMeta sm = (SkullMeta) item.getItemMeta();
					if(sm.hasOwner()) { // An human head
						name = sm.getOwner();
						wither = false;
					}
					break;
				}
			}
			
			if((wither && p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromWither.addLore"))
					|| (!wither && p.getConfig().getBoolean("gameplay-changes.craftGoldenAppleFromHead.fromHuman.addLore"))) {
				
				List<String> lore = null;
				if(wither) {
					lore = Arrays.asList(i.t("craft.goldenApple.loreLine1Monster"), i.t("craft.goldenApple.loreLine2Monster"));
				}
				else {
					lore = Arrays.asList(i.t("craft.goldenApple.loreLine1Player", name), i.t("craft.goldenApple.loreLine2Player", name));
				}
				meta.setLore(lore);
			
			}
			
			result.setItemMeta(meta);
			ev.getInventory().setResult(result);
		}
	}
	
	/**
	 * Used to disable power-II potions.
	 * 
	 * TODO find a better way to do this, by simulating the same behavior as when the player
	 * tries to brew a potion that doesn't exists.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onBrew(BrewEvent ev) {
		if(p.getConfig().getBoolean("gameplay-changes.disableLevelIIPotions")) {
			BrewerInventory brewerContent = ev.getContents();
			ItemStack brewerInvContent = brewerContent.getIngredient();
			
			if(brewerInvContent.getType().equals(Material.GLOWSTONE_DUST)) {
				
				ev.setCancelled(true);
				
				for(HumanEntity player : brewerContent.getViewers()) {
					if(player instanceof Player) {
						((Player) player).sendMessage(i.t("potions.disabled"));
					}
				}
			}
		}
	}
	
	/**
	 * Used to replace ghast tears with gold (if needed).
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev) {
		if (ev.getEntity() instanceof Ghast && p.getConfig().getBoolean("gameplay-changes.replaceGhastTearsWithGold")) {
			List<ItemStack> drops = new ArrayList<ItemStack>(ev.getDrops());
			ev.getDrops().clear(); 
			for (ItemStack i : drops) {
				if (i.getType() == Material.GHAST_TEAR) {
					ev.getDrops().add(new ItemStack(Material.GOLD_INGOT,i.getAmount()));
				} else {
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
		if ((ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK) && ev.getPlayer().getItemInHand().getType() == Material.COMPASS && p.getConfig().getBoolean("gameplay-changes.compass") && !p.getGameManager().isPlayerDead(ev.getPlayer().getName())) {
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
				pl.sendMessage(i.t("compass.noRottenFlesh"));
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
				pl.sendMessage(i.t("compass.nothingFound"));
				return;
			}
			pl.sendMessage(i.t("compass.success"));
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
