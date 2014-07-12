package me.azenet.UHPlugin.task;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamStartTask extends BukkitRunnable {

	private UHPlugin p = null;
	private UHTeam team = null;
	private Location startPoint = null;
	
	public TeamStartTask(UHPlugin p, UHTeam team, Location startPoint) {
		this.p = p;
		this.team = team;
		this.startPoint = startPoint;
	}
	
	@Override
	public void run() {
		team.teleportTo(startPoint);
		p.getLogger().info("[start] Teleported team " + team.getName());
		for (Player player : team.getPlayers()) {
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setSaturation(14);
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
			player.setExp(0L+0F);
			player.setLevel(0);
			player.closeInventory();
			player.getActivePotionEffects().clear();
			player.setCompassTarget(startPoint);
		}
	}

}
