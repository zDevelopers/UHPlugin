package me.azenet.UHPlugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UHGameManager {
	
	private UHPlugin p = null;
	private UHTeamManager tm = null;
	private Scoreboard sb = null;

	private Random random = null;
	
	private String sbobjname = "KTP";
	private NumberFormat formatter = new DecimalFormat("00");
	private Boolean damageIsOn = false;

	private LinkedList<Location> loc = new LinkedList<Location>();
	private ArrayList<UHTeam> teams = new ArrayList<UHTeam>();
	private HashSet<String> alivePlayers = new HashSet<String>();
	
	private Boolean gameRunning = false;
	private Integer episode = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	
	
	public UHGameManager(UHPlugin plugin) {
		this.p = plugin;
		this.tm = plugin.getTeamManager();
		
		random = new Random();
	}

	
	
	
	
	
	
	public void initEnvironment() {
		p.getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
		p.getServer().getWorlds().get(0).setTime(6000L);
		p.getServer().getWorlds().get(0).setStorm(false);
		p.getServer().getWorlds().get(0).setDifficulty(Difficulty.HARD);
	}
	
	public void initScoreboard() {
		sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = sb.registerNewObjective("Vie", "dummy");
		obj.setDisplayName("Vie");
		obj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}
	
	public void setMatchInfo() {
		Objective obj = null;
		try {
			obj = sb.getObjective(sbobjname);
			obj.setDisplaySlot(null);
			obj.unregister();
		} catch (Exception e) {
			
		}
		Random r = new Random();
		sbobjname = "KTP"+r.nextInt(10000000);
		obj = sb.registerNewObjective(sbobjname, "dummy");
		obj = sb.getObjective(sbobjname);

		obj.setDisplayName(this.getScoreboardName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY+"Episode "+ChatColor.WHITE+episode)).setScore(336);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE+""+alivePlayers.size()+ChatColor.GRAY+" joueurs")).setScore(168);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE+""+getAliveTeams().size()+ChatColor.GRAY+" teams")).setScore(84);
		obj.getScore(Bukkit.getOfflinePlayer("")).setScore(42);
		obj.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE+formatter.format(this.minutesLeft)+ChatColor.GRAY+":"+ChatColor.WHITE+formatter.format(this.secondsLeft))).setScore(21);
	}
	
	public Scoreboard getScoreboard() {
		return sb;
	}
	
	public String getScoreboardName() {
		String s = p.getConfig().getString("scoreboard", "Kill The Patrick");
		return s.substring(0, Math.min(s.length(), 16));
	}
	
	public void updatePlayerListName(Player p) {
		p.setScoreboard(sb);
		Integer he = (int) Math.ceil(((Damageable) p).getHealth());
		sb.getObjective("Vie").getScore(p).setScore(he);
	}

	public void addToScoreboard(Player player) {
		player.setScoreboard(sb);
		sb.getObjective("Vie").getScore(player).setScore(0);
		this.updatePlayerListName(player);
	}

	public void setLifeInScoreboard(Player entity, int i) {
		entity.setScoreboard(sb);
		sb.getObjective("Vie").getScore(entity).setScore(i);
	}
	
	
	
	
	
	
	
	/**
	 * Starts the game.
	 *  - Teleport the teams
	 *  - Change the gamemode, reset the life, clear inventories, etc.
	 *  - Launch the timer
	 */
	public void start(CommandSender sender) {
		// We adds all the connected players to a list of alive players.
		alivePlayers.clear();
		for(final Player player : p.getServer().getOnlinePlayers()) {
			alivePlayers.add(player.getName());
		}
		
		// No team? We creates a team per player.
		if(tm.getTeams().size() == 0) { 
			for(final Player player : p.getServer().getOnlinePlayers()) {
				UHTeam team = new UHTeam(player.getName(), player.getName(), ChatColor.WHITE, this.p);
				team.addPlayer(player);
				tm.addTeam(team);
			}
		}
		
		if(loc.size() < tm.getTeams().size()) {
			sender.sendMessage(ChatColor.RED + "Unable to start the game: not enough teleportation spots.");
			return;
		}
		
		LinkedList<Location> unusedTP = loc;
		for (final UHTeam t : teams) {
			final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
			Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {

				@Override
				public void run() {
					t.teleportTo(lo);
					for (Player player : t.getPlayers()) {
						player.setGameMode(GameMode.SURVIVAL);
						player.setHealth(20);
						player.setFoodLevel(20);
						player.setExhaustion(5F);
						player.getInventory().clear();
						player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), 
								new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
						player.setExp(0L+0F);
						player.setLevel(0);
						player.closeInventory();
						player.getActivePotionEffects().clear();
						player.setCompassTarget(lo);
						setLifeInScoreboard(player, 20);
					}
				}
			}, 10L);
			
			unusedTP.remove(lo);
		}
		
		World w = p.getServer().getWorlds().get(0);
		
		w.setGameRuleValue("doDaylightCycle", ((Boolean)p.getConfig().getBoolean("daylightCycle.do")).toString());
		w.setGameRuleValue("keepInventory", ((Boolean)false).toString()); // Just in case...
		
		w.setTime(p.getConfig().getLong("daylightCycle.time"));
		w.setStorm(false);
		w.setDifficulty(Difficulty.HARD);
		this.episode = 1;
		this.minutesLeft = getEpisodeLength();
		this.secondsLeft = 0;
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(p, new BukkitRunnable() {
			@Override
			public void run() {
				setMatchInfo();
				secondsLeft--;
				if (secondsLeft == -1) {
					minutesLeft--;
					secondsLeft = 59;
				}
				if (minutesLeft == -1) {
					shiftEpisode();
				}
			} 
		}, 20L, 20L);
		
		
		// 30 seconds later, damages are enabled.
		Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable() {

			@Override
			public void run() {
				damageIsOn = true;
			}
		}, 600L);
		
		Bukkit.getServer().broadcastMessage(ChatColor.GREEN+"--- GO ---");
		this.gameRunning = true;
	}
	

	
	
	
	
	
	
	/**
	 * Shifts an episode.
	 * 
	 * @param shifter The player who shifts the episode, an empty string if the episode is shifted because the timer is up.
	 */
	public void shiftEpisode(String shifter) {
		String message = ChatColor.AQUA + "-------- Fin de l'épisode " + episode;
		if(!shifter.equals("")) {
			message += " [forcé par " + shifter + "]";
		}
		message += " --------";
		p.getServer().broadcastMessage(message);
		
		this.episode++;
		this.minutesLeft = getEpisodeLength();
		this.secondsLeft = 0;
	}
	
	/**
	 * Shift an episode because the timer is up.
	 */
	public void shiftEpisode() {
		shiftEpisode("");
	}

	
	
	
	
	
	
	/**
	 * Is the game launched?
	 * 
	 * @return True if the game is running.
	 */
	public boolean isGameRunning() {
		return this.gameRunning;
	}
	
	
	public Integer getEpisodeLength() {
		return p.getConfig().getInt("episodeLength");
	}
	
	public void addLocation(int x, int z) {
		loc.add(new Location(p.getServer().getWorlds().get(0), x, p.getServer().getWorlds().get(0).getHighestBlockYAt(x,z)+120, z));
	}
	
	public boolean isTakingDamage() {
		return damageIsOn;
	}

	public boolean isPlayerDead(String name) {
		return !alivePlayers.contains(name);
	}
	
	public void addDead(String name) {
		alivePlayers.remove(name);
	}

	private ArrayList<UHTeam> getAliveTeams() {
		ArrayList<UHTeam> aliveTeams = new ArrayList<UHTeam>();
		for (UHTeam t : teams) {
			for (Player p : t.getPlayers()) {
				if (p.isOnline() && !aliveTeams.contains(t)) aliveTeams.add(t);
			}
		}
		return aliveTeams;
	}
	
}
