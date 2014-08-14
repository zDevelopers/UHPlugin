package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;
import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class UHGameListener implements Listener {
	
	private UHPlugin p = null;
	private I18n i = null;
	
	public UHGameListener(UHPlugin p) {
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
		for (Player pp : Bukkit.getServer().getOnlinePlayers()) {
			pp.playSound(pp.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
		}
		
		// Send lightning strike if needed
		if(p.getConfig().getBoolean("death.announcements.lightning-strike")) {
			ev.getEntity().getLocation().getWorld().strikeLightningEffect(ev.getEntity().getLocation());
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
		if(p.getConfig().getInt("death.give-xp-to-killer.levels") > 0) {
			Entity killer = ev.getEntity().getKiller();
			if(killer != null && killer instanceof Player) {
				
				boolean inSameTeam = p.getTeamManager().inSameTeam(ev.getEntity(), (Player) killer);
				boolean onlyOtherTeam = p.getConfig().getBoolean("death.give-xp-to-killer.onlyOtherTeam");
				
				if((onlyOtherTeam && !inSameTeam) || !onlyOtherTeam) {
					((Player) killer).giveExpLevels(p.getConfig().getInt("death.give-xp.levels"));
				}
			}
		}
		
		// Sends a team-death message if needed.
		if(p.getConfig().getBoolean("death.messages.notifyIfTeamHasFallen", false)) {
			final UHTeam team = p.getTeamManager().getTeamForPlayer((Player) ev.getEntity());
			if(team != null) {
				boolean isAliveTeam = false;
				
				for(Player player : team.getPlayers()) {
					if(!p.getGameManager().isPlayerDead(player.getName())) {
						isAliveTeam = true;
						break;
					}
				}
				
				if(!isAliveTeam) {
					// Used to display this message after the death message.
					Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
						@Override
						public void run() {
							p.getServer().broadcastMessage(i.t("death.teamHasFallen", p.getConfig().getString("death.messages.teamDeathMessagesFormat", ""), team.getDisplayName() + p.getConfig().getString("death.messages.teamDeathMessagesFormat", "")));
						}
					}, 1L);
				}
			}
		}
		
		// Customizes the death message
		ev.setDeathMessage(p.getConfig().getString("death.messages.deathMessagesFormat", "") + ev.getDeathMessage());
		
		// Updates the number of alive players/teams
		p.getGameManager().updateAliveCounters();
		p.getGameManager().getScoreboardManager().updateScoreboard();
		
		// Saves the location of the death
		p.getGameManager().addDeathLocation(ev.getEntity(), ev.getEntity().getLocation());
		
		// Shows the death location on the dynmap
		p.getDynmapIntegration().showDeathLocation(ev.getEntity());
	}
	
	
	/**
	 * Used to disable any damages if the game has not started.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (!p.getGameManager().isTakingDamage()) {
				ev.setCancelled(true);
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
		if (ev.getRegainReason() == RegainReason.SATIATED) {
			ev.setCancelled(true);
		}
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
			p.getGameManager().initPlayer(ev.getPlayer());
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
	 * Used to prevent the player to go outside the border.
	 * 
	 * @param ev
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev) {
		if(!p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
			if(!p.getBorderManager().isInsideBorder(ev.getTo(), p.getBorderManager().getCheckDiameter())) {
				ev.setCancelled(true);
			}
		}
	}
}
