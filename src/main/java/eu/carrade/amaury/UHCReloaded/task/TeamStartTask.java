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

package eu.carrade.amaury.UHCReloaded.task;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;


public class TeamStartTask extends BukkitRunnable {

	private UHCReloaded p = null;
	private I18n i = null;

	private UHTeam team = null;
	private List<Location> startPoints = null;

	private Boolean slow = false;
	private Boolean ignoreTeams = false;

	private CommandSender sender = null;
	private Integer teamsTeleported = 0;

	private Random random = new Random();

	public TeamStartTask(UHCReloaded p, UHTeam team, List<Location> startPoints, Boolean ignoreTeams) {
		this.p = p;
		this.i = p.getI18n();
		this.team = team;
		this.startPoints = startPoints;
		this.ignoreTeams = ignoreTeams;
	}

	public TeamStartTask(UHCReloaded p, UHTeam team, List<Location> startPoints, Boolean ignoreTeams, Boolean slow, CommandSender sender, Integer teamsTeleported) {
		this.p = p;
		this.i = p.getI18n();
		this.team = team;
		this.startPoints = startPoints;
		this.slow = slow;
		this.sender = sender;
		this.teamsTeleported = teamsTeleported;
		this.ignoreTeams = ignoreTeams;
	}

	@Override
	public void run() {

		if(!ignoreTeams) {
			Location spawn = getRandomPoint();

			team.teleportTo(spawn);
			p.getDynmapIntegration().showSpawnLocation(team, spawn);
		}
		else {
			for(Player player : team.getOnlinePlayers()) {
				Location spawn = getRandomPoint();

				player.teleport(spawn);
				p.getDynmapIntegration().showSpawnLocation(player, team.getColor(), spawn);
			}
		}

		for (Player player : team.getOnlinePlayers()) {
			player.setGameMode(GameMode.SURVIVAL);

			if(slow) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}

			player.setHealth(20D);
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
			player.setExp(0L+0F);
			player.setLevel(0);
			player.closeInventory();

			for(PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}

			player.setCompassTarget(player.getWorld().getSpawnLocation());
		}

		if(slow) {
			try {
				sender.sendMessage(i.t("start.startSlowTeamTP", team.getDisplayName()));
			} catch(NullPointerException ignored) { }

			if(p.getGameManager().getAliveTeamsCount() == this.teamsTeleported) {
				p.getGameManager().setSlowStartTPFinished(true);

				try {
					sender.sendMessage(i.t("start.startSlowAllTeamsTP"));
					sender.sendMessage(i.t("start.startSlowAllTeamsTPCmd"));
				} catch(NullPointerException ignored) { }
			}
		}
	}

	/**
	 * Returns a random location in the list, and removes the location from
	 * this list, to avoid a teleportation of multiple people/teams at the same spot.
	 *
	 * @return The location of a randomly-chosen spawn point.
	 */
	private Location getRandomPoint() {
		Location point = startPoints.get(random.nextInt(startPoints.size()));
		startPoints.remove(point);

		return point;
	}
}
