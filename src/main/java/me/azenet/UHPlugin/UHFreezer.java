package me.azenet.UHPlugin;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class UHFreezer {
	
	private UHPlugin p = null;
	
	private Boolean globalFreeze = false;
	private ArrayList<String> frozenPlayers = new ArrayList<String>();
	
	public UHFreezer(UHPlugin plugin) {
		this.p = plugin;
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
		if(frozenPlayers.contains(player.getName())) {
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
	 */
	public void setGlobalFreezeState(Boolean frozen) {
		this.globalFreeze = frozen;
		
		if(frozen) {
			for(String playerName : p.getGameManager().getAlivePlayers()) {
				this.setPlayerFreezeState(p.getServer().getPlayer(playerName), true);
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
			p.getGameManager().toggleTimerPause();
			p.getBorderManager().toggleWarningTimePause();
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
			
			// Uhfreezes the timers.
			p.getGameManager().toggleTimerPause();
			p.getBorderManager().toggleWarningTimePause();
		}
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
		if(frozen && !this.frozenPlayers.contains(player.getName())) {
			this.frozenPlayers.add(player.getName());
			
			// Used to prevent the player to be kiked for fly if he was frozen during a fall.
			// He is blocked inside his current block anyway.
			player.setAllowFlight(true);
		}
		else {
			this.frozenPlayers.remove(player.getName());
			if(!player.getGameMode().equals(GameMode.CREATIVE)) {
				player.setFlying(false); // just in case
				player.setAllowFlight(false);
			}
		}
	}
	
	/**
	 * Returns true if the given player is frozen.
	 * 
	 * @param player The player to be checked.
	 * @return
	 */
	public boolean isPlayerFrozen(Player player) {
		return frozenPlayers.contains(player.getName());
	}
	
	/**
	 * (Un)freezes a creature.
	 * 
	 * @param creature The creature to freeze.
	 * @param frozen If true the creature will be frozen. Else...
	 */
	public void freezeCreature(Creature creature, Boolean frozen) {
		if(frozen) {
			creature.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10000000, 100, true));
		}
		else {
			creature.removePotionEffect(PotionEffectType.SLOW);
		}
	}
	
	
	/**
	 * Returns the list of currently frozen players.
	 * 
	 * @return The list.
	 */
	public ArrayList<String> getFrozenPlayers() {
		return this.frozenPlayers;
	}
}
