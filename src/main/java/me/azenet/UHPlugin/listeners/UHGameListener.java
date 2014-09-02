/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

package me.azenet.UHPlugin.listeners;

import java.util.List;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;
import me.azenet.UHPlugin.events.TimerEndsEvent;
import me.azenet.UHPlugin.events.TimerStartsEvent;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
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
	 *  - give XP to the killer (if needed);
	 *  - broadcast the winners and launch the fireworks if needed;
	 *  - notify the player about the possibility of respawn if hardcore hearts are enabled.
	 *  
	 * @param ev
	 */
	
	@EventHandler
	public void onPlayerDeath(final PlayerDeathEvent ev) {
		// This needs to be executed only if the player die as a player, not a spectator.
		// Also, the game needs to be started.
		if(p.getGameManager().isPlayerDead(ev.getEntity()) || !p.getGameManager().isGameRunning()) {
			return;
		}
		
		// Plays sound.
		Sound deathSound = p.getGameManager().getDeathSound();
		if(deathSound != null) {
			for (Player pp : Bukkit.getServer().getOnlinePlayers()) {
				pp.playSound(pp.getLocation(), deathSound, 1F, 1F);
			}
		}
		
		// Send lightning strike if needed.
		if(p.getConfig().getBoolean("death.announcements.lightning-strike")) {
			ev.getEntity().getLocation().getWorld().strikeLightningEffect(ev.getEntity().getLocation());
		}
		
		// Removes the player from the alive players.
		this.p.getGameManager().addDead(ev.getEntity());
		
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
					((Player) killer).giveExpLevels(p.getConfig().getInt("death.give-xp-to-killer.levels"));
				}
			}
		}
		
		// Sends a team-death message if needed.
		if(p.getConfig().getBoolean("death.messages.notifyIfTeamHasFallen", false)) {
			final UHTeam team = p.getTeamManager().getTeamForPlayer((Player) ev.getEntity());
			if(team != null) {
				boolean isAliveTeam = false;
				
				for(Player player : team.getPlayers()) {
					if(!p.getGameManager().isPlayerDead(player)) {
						isAliveTeam = true;
						break;
					}
				}
				
				if(!isAliveTeam) {
					// Used to display this message after the death message.
					Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
						@Override
						public void run() {
							String format = ChatColor.translateAlternateColorCodes('&', p.getConfig().getString("death.messages.teamDeathMessagesFormat", ""));
							p.getServer().broadcastMessage(i.t("death.teamHasFallen", format, team.getDisplayName() + format));
						}
					}, 1L);
				}
			}
		}
		
		// Customizes the death message
		ev.setDeathMessage(ChatColor.translateAlternateColorCodes('&', p.getConfig().getString("death.messages.deathMessagesFormat", "")) + ev.getDeathMessage());
		
		// Updates the number of alive players/teams
		p.getGameManager().updateAliveCounters();
		
		// Saves the location of the death
		p.getGameManager().addDeathLocation(ev.getEntity(), ev.getEntity().getLocation());
		
		// Shows the death location on the dynmap
		p.getDynmapIntegration().showDeathLocation(ev.getEntity());
		
		// Broadcasts the winner(s) and launches some fireworks if needed, a few seconds later
		if(p.getConfig().getBoolean("finish.auto.do")) {
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					try {
						p.getGameManager().finishGame();
					} catch(IllegalStateException ignored) {
						// The game is not finished.
					}
				}
			}, p.getConfig().getInt("finish.auto.timeAfterLastDeath", 3) * 20L);
		}
		
		// Notifies the player about the possibility of respawn if hardcore hearts are enabled
		if(p.getConfig().getBoolean("hardcore-hearts.display") && p.getProtocolLibIntegrationWrapper().isProtocolLibIntegrationEnabled() && p.getConfig().getBoolean("hardcore-hearts.respawnMessage")) {
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {
				@Override
				public void run() {
					ev.getEntity().sendMessage(i.t("death.canRespawn"));
				}
			}, 2L);
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
		if (this.p.getGameManager().isPlayerDead(ev.getPlayer()) && !this.p.getConfig().getBoolean("death.kick.allow-reconnect", true)) {
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
		
		// The display name is reset when the player logs off.
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
			
			// The same for ProtocolLib
			if(!p.getProtocolLibIntegrationWrapper().isProtocolLibIntegrationEnabled()) {
				List<String> enabledOptionsWithProtocolLibNeeded = p.getProtocolLibIntegrationWrapper().isProtocolLibNeeded();
				
				if(enabledOptionsWithProtocolLibNeeded != null) {
					ev.getPlayer().sendMessage(i.t("load.PLNotInstalled1"));
					ev.getPlayer().sendMessage(i.t("load.PLNotInstalled2"));
					for(String option : enabledOptionsWithProtocolLibNeeded) {
						ev.getPlayer().sendMessage(i.t("load.PLNotInstalledItem", option));
					}
					ev.getPlayer().sendMessage(i.t("load.PLNotInstalled3", "http://dev.bukkit.org/bukkit-plugins/protocollib/"));
				}
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
	 * Used to send the chat to the team-chat if this team-chat is enabled.
	 * 
	 * @param ev
	 */
	// Priority LOWEST to be able to cancel the event before all other plugins
	@EventHandler(priority=EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent ev) {
		// If the event is asynchronous, the message was sent by a "real" player.
		// Else, the message was sent by a plugin (like our /g command, or another plugin), and
		// the event is ignored.
		if(ev.isAsynchronous()) {
			if(p.getTeamChatManager().isTeamChatEnabled(ev.getPlayer())) {
				ev.setCancelled(true);
				p.getTeamChatManager().sendTeamMessage(ev.getPlayer(), ev.getMessage());
			}
			else if(p.getTeamChatManager().isOtherTeamChatEnabled(ev.getPlayer())) {
				ev.setCancelled(true);
				p.getTeamChatManager().sendTeamMessage(ev.getPlayer(), ev.getMessage(), p.getTeamChatManager().getOtherTeamEnabled(ev.getPlayer()));
			}
		}
	}
	
	
	@EventHandler
	public void onTimerEnds(TimerEndsEvent ev) {
		p.getTimerManager().updateStartedTimersList();
		
		if(ev.getTimer().equals(p.getTimerManager().getMainTimer())) {
			// If this timer is the main one, we shifts an episode.
			// The shift restarts the timer.
			p.getGameManager().shiftEpisode();
		}
	}
	
	@EventHandler
	public void onTimerStarts(TimerStartsEvent ev) {
		p.getTimerManager().updateStartedTimersList();
	}
}
