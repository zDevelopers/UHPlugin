package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.azenet.UHPlugin.listeners.UHFreezerListener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UHFreezer {
	
	private UHPlugin p = null;
	
	private boolean isListenerRegistered = false;
	private UHFreezerListener freezerListener = null;
	
	private Boolean globalFreeze = false;
	private ArrayList<UUID> frozenPlayers = new ArrayList<UUID>();
	private HashMap<UUID,Boolean> oldAllowFly = new HashMap<UUID,Boolean>();
	private HashMap<UUID,Boolean> oldFlyMode = new HashMap<UUID,Boolean>();
	
	public UHFreezer(UHPlugin plugin) {
		this.p = plugin;
		
		this.freezerListener = new UHFreezerListener(p);
	}
	
	
	/**
	 * Freezes a player, if needed.
	 * The player is blocked inside the block he is currently.
	 * 
	 * This method is intended to be executed when a player moves.
	 * 
	 * @param player The player to freeze
	 * @param from The old position from the PlayerMoveEvent
	 * @param to The new position from the PlayerMoveEvent
	 */
	public void freezePlayerIfNeeded(Player player, Location from, Location to) {
		if(frozenPlayers.contains(player.getUniqueId())) {
			// If the X, Y or Z coordinate of the player change, he needs to be teleported inside the old block.
			// The yaw and pitch are conserved, to teleport more smoothly.
			if(from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
				player.teleport(new Location(from.getWorld(), from.getBlockX() + 0.5, from.getBlockY(), from.getBlockZ() + 0.5, to.getYaw(), to.getPitch()), TeleportCause.PLUGIN);
			}
		}
	}
	
	
	
	/**
	 * Enables or disables the global freeze of players, mobs, timer.
	 * 
	 * @param freezed If true the global freeze will be enabled.
	 * @param showStateInScoreboard If false, the freeze state will not be displayed in the scoreboard.
	 */
	public void setGlobalFreezeState(Boolean frozen, Boolean showStateInScoreboard) {
		this.globalFreeze = frozen;
		
		if(frozen) {
			for(Player player : p.getGameManager().getAlivePlayers()) {
				this.setPlayerFreezeState(player, true);
			}
			
			// Freezes the mobs by applying a Slowness effect. There isn't any EntityMoveEvent, so...
			for(World world : p.getServer().getWorlds()) {
				for(Entity entity : world.getLivingEntities()) {
					if(entity instanceof Creature) {
						freezeCreature((Creature) entity, true);
					}
				}
			}
			
			// Freezes the timers.
			p.getGameManager().setTimerPause(true);
			p.getBorderManager().setWarningTimePause(true);
		}
		
		else {
			// All the online players are listed, not the internal list of frozen players,
			// to avoid a ConcurrentModificationException if the iterated list is being emptied.
			for(Player player : p.getServer().getOnlinePlayers()) {
				if(this.isPlayerFrozen(player)) {
					this.setPlayerFreezeState(player, false);
				}
			}
			
			// Removes the slowness effect
			for(World world : p.getServer().getWorlds()) {
				for(Entity entity : world.getLivingEntities()) {
					if(entity instanceof Creature) {
						freezeCreature((Creature) entity, false);
					}
				}
			}
			
			// Unfreezes the timers.
			p.getGameManager().setTimerPause(false);
			p.getBorderManager().setWarningTimePause(false);
		}
		
		if(showStateInScoreboard || !frozen) {
			this.p.getGameManager().getScoreboardManager().displayFreezeState();
		}
		
		updateListenerRegistration();
	}
	
	/**
	 * Enables or disables the global freeze of players, mobs, timer.
	 * 
	 * @param freezed If true the global freeze will be enabled.
	 */
	public void setGlobalFreezeState(Boolean frozen) {
		setGlobalFreezeState(frozen, true);
	}
	
	
	/**
	 * Gets the current state of the global freeze.
	 * 
	 * @return True if the global freeze is enabled.
	 */
	public boolean getGlobalFreezeState() {
		return this.globalFreeze;
	}
	
	/**
	 * Freezes a player.
	 * 
	 * @param player The player to freeze.
	 * @param freezed If true the player will be frozen. If false, unfrozen.
	 */
	public void setPlayerFreezeState(Player player, Boolean frozen) {
		if(frozen && !this.frozenPlayers.contains(player.getUniqueId())) {
			this.frozenPlayers.add(player.getUniqueId());
			this.oldAllowFly.put(player.getUniqueId(), player.getAllowFlight());
			this.oldFlyMode.put(player.getUniqueId(), player.isFlying());
			
			// Used to prevent the player to be kicked for fly if he was frozen during a fall.
			// He is blocked inside his current block anyway.
			player.setAllowFlight(true);
		}
		else {
			this.frozenPlayers.remove(player.getUniqueId());
			
			player.setFlying(this.oldFlyMode.get(player.getUniqueId()));
			player.setAllowFlight(this.oldAllowFly.get(player.getUniqueId()));
			
			this.oldAllowFly.remove(player.getUniqueId());
			this.oldFlyMode.remove(player.getUniqueId());
		}
		
		updateListenerRegistration();
	}
	
	/**
	 * Returns true if the given player is frozen.
	 * 
	 * @param player The player to be checked.
	 * @return
	 */
	public boolean isPlayerFrozen(Player player) {
		return frozenPlayers.contains(player.getUniqueId());
	}
	
	/**
	 * (Un)freezes a creature.
	 * 
	 * @param creature The creature to freeze.
	 * @param frozen If true the creature will be frozen. Else...
	 */
	public void freezeCreature(Creature creature, Boolean frozen) {
		if(frozen) {
			// Freezes the creature for about 68 years.
			creature.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, true));
		}
		else {
			creature.removePotionEffect(PotionEffectType.SLOW);
		}
	}
	
	/**
	 * Registers the listener if it wasn't registered, and unregisters this listener
	 * if there isn't any frozen player.
	 * 
	 * Call this AFTER registering the first frozen player, and AFTER unregistering
	 * the last one. 
	 */
	private void updateListenerRegistration() {
		// Registers the listener if needed 
		// (i.e if there isn't any frozen player, or if the global freeze is enabled). 
		if(!this.isListenerRegistered) {
			if(!this.frozenPlayers.isEmpty() || this.getGlobalFreezeState()) {
				p.getServer().getPluginManager().registerEvents(freezerListener, p);
				this.isListenerRegistered = true;
			}
		}
		
		// Unregister the listener if needed
		else {
			if(this.frozenPlayers.isEmpty() && !this.getGlobalFreezeState()) {
				HandlerList.unregisterAll(freezerListener);
				this.isListenerRegistered = false;
			}
		}
	}
	
	
	/**
	 * Returns the list of currently frozen players.
	 * 
	 * @return The list.
	 */
	public ArrayList<Player> getFrozenPlayers() {
		
		ArrayList<Player> frozenPlayersList = new ArrayList<Player>();
		
		for(UUID id : frozenPlayers) {
			frozenPlayersList.add(p.getServer().getPlayer(id));
		}
		
		return frozenPlayersList;
	}
}
