package me.azenet.UHPlugin.task;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.UHTeam;
import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamStartTask extends BukkitRunnable {

	private UHPlugin p = null;
	private I18n i = null;
	private UHTeam team = null;
	private Location startPoint = null;
	private Boolean slow = false;
	private CommandSender sender = null;
	private Integer teamsTeleported = 0;
	
	public TeamStartTask(UHPlugin p, UHTeam team, Location startPoint) {
		this.p = p;
		this.i = p.getI18n();
		this.team = team;
		this.startPoint = startPoint;
	}
	
	public TeamStartTask(UHPlugin p, UHTeam team, Location startPoint, Boolean slow, CommandSender sender, Integer teamsTeleported) {
		this.p = p;
		this.i = p.getI18n();
		this.team = team;
		this.startPoint = startPoint;
		this.slow = slow;
		this.sender = sender;
		this.teamsTeleported = teamsTeleported;
	}
	
	@Override
	public void run() {
		team.teleportTo(startPoint);
		
		for (Player player : team.getPlayers()) {
			player.setGameMode(GameMode.SURVIVAL);
			
			if(slow) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
			
			player.setHealth(20D);
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
		
		p.getDynmapIntegration().showSpawnLocation(team, startPoint);
		
		if(slow) {
			try {
				sender.sendMessage(i.t("start.startSlowTeamTP", team.getChatColor() + team.getName()));
			} catch(NullPointerException e) { }
			
			if(p.getGameManager().getAliveTeamsCount() == this.teamsTeleported) {
				p.getGameManager().setSlowStartTPFinished(true);
				
				try {
					sender.sendMessage(i.t("start.startSlowAllTeamsTP"));
					sender.sendMessage(i.t("start.startSlowAllTeamsTPCmd"));
				} catch(NullPointerException e) { }
			}
		}
	}
}
