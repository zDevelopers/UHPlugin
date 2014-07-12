package me.azenet.UHPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHPluginCommand implements CommandExecutor {
	
	private UHPlugin p = null;
	
	private ChatColor ce = ChatColor.RED; // error
	private ChatColor ci = ChatColor.WHITE; // info
	private ChatColor cc = ChatColor.GOLD; // command
	private ChatColor cs = ChatColor.GREEN; // success message
	private ChatColor cst = ChatColor.GRAY; // status
	
	private ArrayList<String> commands = new ArrayList<String>();
	private ArrayList<String> teamCommands = new ArrayList<String>();


	public UHPluginCommand(UHPlugin p) {
		this.p = p;
		
		commands.add("start");
		commands.add("shift");
		commands.add("team");
		commands.add("addspawn");
		commands.add("generatewalls");
		commands.add("heal");
		commands.add("healall");
		commands.add("resurrect");
		
		teamCommands.add("add");
		teamCommands.add("remove");
		teamCommands.add("addplayer");
		teamCommands.add("removeplayer");
		teamCommands.add("list");
		teamCommands.add("reset");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("uh")) {
			return false;
		}
		
		if(args.length == 0) {
			help(sender, false);
			return true;
		}
		
		String subcommandName = args[0].toLowerCase();
		
		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			help(sender, true);
			return true;
		}
		
		// Second: is the sender allowed?
		if(!isAllowed(sender, subcommandName)) {
			unauthorized(sender, command);
			return true;
		}
		
		// Third: instantiation
		try {
			Class<? extends UHPluginCommand> cl = this.getClass();
			Class[] parametersTypes = new Class[]{CommandSender.class, Command.class, String.class, String[].class};
			
			Method doMethod = cl.getDeclaredMethod("do" + WordUtils.capitalize(subcommandName), parametersTypes);
			
			doMethod.invoke(this, new Object[]{sender, command, label, args});
			
			return true;
			
		} catch (NoSuchMethodException e) {
			// Unknown method => unknown subcommand.
			help(sender, true);
			return true;
			
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(ce + "An error occured, see console for details. This is probably a bug.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Print the help.
	 * 
	 * @param sender
	 * @param error True if the help is printed because the used typed an unknown command.
	 */
	private void help(CommandSender sender, boolean error) {
		sender.sendMessage(ChatColor.YELLOW + p.getDescription().getDescription() + " - version " + p.getDescription().getVersion());
		
		if(error) {
			sender.sendMessage(ce + "This subcommand does not exists.");
		}
		
		sender.sendMessage(ci + "Available subcommands are listed below. Legend: " + cc + "/uh command <required> [optional=default]" + ci + ".");
		sender.sendMessage(ChatColor.GRAY + "------ Game-related commands ------");
		sender.sendMessage(cc + "/uh start " + ci + ": launchs the game.");
		sender.sendMessage(cc + "/uh shift " + ci + ": shifts an episode.");
		sender.sendMessage(cc + "/uh team " + ci + ": manages the teams (execute /uh team for more details).");
		sender.sendMessage(cc + "/uh addspawn " + ci + ": adds a spawn point for a team or a player, at the current location of the sender.");
		sender.sendMessage(cc + "/uh addspawn <x> <z> " + ci + ": adds a spawn point for a team or a player, at the provided coordinates.");
		sender.sendMessage(cc + "/uh generatewalls " + ci + ": generates the walls according to the configuration.");
		sender.sendMessage(ChatColor.GRAY + "------ Bugs-related commands ------");
		sender.sendMessage(cc + "/uh heal <player> [half-hearts=20] " + ci + ": heals a player to the number of half-hearts provided (default 20).");
		sender.sendMessage(cc + "/uh healall [half-hearts=20] " + ci + ": heals all players instead of only one.");
		sender.sendMessage(cc + "/uh resurrect <player> " + ci + ": resurrects a player.");
		sender.sendMessage(ChatColor.GRAY + "Tip: you can put one coordinate per line, following the format “x,y” in a “plugins/UHPlugin/positions.txt” file instead of using /uh addspawn each time.");
	}
	
	/**
	 * This method checks if an user is allowed to send a command.
	 * 
	 * @param sender
	 * @param subcommand
	 * @return boolean The allowance status.
	 */
	private boolean isAllowed(CommandSender sender, String subcommand) {
		if(sender instanceof Player) {
			if(sender.isOp()) {
				return true;
			}
			else if(sender.hasPermission("uh." + subcommand)) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * This method send a message to a player who try to use a command without the permission.
	 * 
	 * @param sender
	 * @param command
	 */
	private void unauthorized(CommandSender sender, Command command) {
		sender.sendMessage(ce + "LOLnope.");
	}
	
	/**
	 * This command starts the game.
	 * Usage: /uh start
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doStart(CommandSender sender, Command command, String label, String[] args) {
		p.getGameManager().start(sender);
	}
	
	/**
	 * This command generates the walls around the map.
	 * Usage: /uh generatewalls
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doGeneratewalls(CommandSender sender, Command command, String label, String[] args) {	
		sender.sendMessage(cst + "Generating the walls...");
		
		World world = null;
		
		if(sender instanceof Player) {
			world = ((Player) sender).getWorld();
		}
		else {
			world = p.getServer().getWorlds().get(0);
			sender.sendMessage(ci + "From the console, generating the walls of the default world, " + world.getName());
		}
		
		
		try {
			p.generateWalls(world);
		}
		catch(Exception e) {
			sender.sendMessage(ce + "An error occured, see console for details.");
			e.printStackTrace();
		}
		
		sender.sendMessage(cst + "Generation done.");
	}

	/**
	 * This command adds a spawn point for a team or a player.
	 * Usage: /uh addspawn (as a player).
	 * Usage: /uh addspawn <x> <z> (as everyone).
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doAddspawn(CommandSender sender, Command command, String label, String[] args) {	
		if(args.length == 1) { // No coordinates given.
			if(!(sender instanceof Player)) {
				sender.sendMessage(ce + "Yo need to specify the coordinates from the console.");
				sender.sendMessage(ce + "Usage: /uh addspawn <x> <z>");
				return;
			}
			else {
				Player pl = (Player) sender; // Just a way to avoid casts everywhere.
				p.getGameManager().addLocation(pl.getLocation().getBlockX(), pl.getLocation().getBlockZ());
				sender.sendMessage(cs + "Spawn added: " + pl.getLocation().getBlockX() + "," + pl.getLocation().getBlockZ());
			}
		}
		else if(args.length == 2) { // Two coordinates needed!
			sender.sendMessage(ce + "You need to specify two coordinates.");
			sender.sendMessage(ce + "Usage: /uh addspawn <x> <z>");
		}
		else {
			p.getGameManager().addLocation(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
			sender.sendMessage(cs + "Spawn added: " + args[1] + "," + args[2]);
		}
	}
	
	/**
	 * This command is used to manage the teams.
	 * Usage: /uh team (for the doc).
	 * Usage: /uh team <add|remove|addplayer|removeplayer|list|reset> (see doc for details).
	 * 	
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTeam(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // No action provided: doc
			sender.sendMessage(ce + "You need to provide an action.");
			sender.sendMessage("");
			sender.sendMessage(ci + "Available options are listed below.");
			sender.sendMessage(cc + "/uh team add <color> " + ci + ": adds a team with the provided color.");
			sender.sendMessage(cc + "/uh team add <color> <name> " + ci + ": adds a named team with the provided name and color. Usefull if you need more than 16 teams.");
			sender.sendMessage(cc + "/uh team remove <name> " + ci + ": removes a team.");
			sender.sendMessage(cc + "/uh team addplayer <teamName> <player> " + ci + ": adds a player inside the given team. The name of the team is it color, or the explicit name given.");
			sender.sendMessage(cc + "/uh team removeplayer <player> " + ci + ": removes a player from his team.");
			sender.sendMessage(cc + "/uh team list " + ci + ": list the teams and their players.");
			sender.sendMessage(cc + "/uh team reset " + ci + ": removes all teams.");
		}
		else {
			UHTeamManager tm = p.getTeamManager();
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) {
				if(args.length == 3) { // /uh team add <color>
					
					ChatColor color = p.getTeamManager().getChatColorByName(args[2]);
					
					if(color == null) {
						sender.sendMessage(ce + "Unable to add the team, check the color name.");
					}
					else {
						try {
							tm.addTeam(color, args[2].toLowerCase());
						}
						catch(IllegalArgumentException e) {
							sender.sendMessage(ce + "This team already exists.");
						}
						sender.sendMessage(cs + "Team " + color + args[2] + cs + " added.");
					}
				
				}
				else if(args.length == 4) { // /uh team add <color> <name>
					
					ChatColor color = p.getTeamManager().getChatColorByName(args[2]);
					
					if(color == null) {
						sender.sendMessage(ce + "Unable to add the team, check the color name.");
					}
					else if(args[3].length() > 16) {
						sender.sendMessage(ce + "Unable to add the team, because the name is too long (max 16 characters).");
					}
					else {
						try {
							tm.addTeam(color, args[3].toLowerCase());
						}
						catch(IllegalArgumentException e) {
							sender.sendMessage(ce + "This team already exists.");
							return;
						}
						sender.sendMessage(cs + "Team " + args[3] + " (" + color + args[2] + cs + ") added.");
					}
					
				}
				else {
					sender.sendMessage(ce + "Syntax error, see /uh team.");
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("remove")) {
				if(args.length == 3) { // /uh team remove <teamName>
					if(!tm.removeTeam(args[2].toLowerCase())) {
						sender.sendMessage(ce + "This team does not exists.");
					}
					else {
						sender.sendMessage(cs + "Team " + args[2] + " removed.");
					}
				}
				else {
					sender.sendMessage(ce + "Syntax error, see /uh team.");
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("addplayer")) {
				if(args.length == 4) { // /uh team addplayer <teamName> <player>
					
					Player player = p.getServer().getPlayer(args[3]);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(ce + "Unable to add the player " + args[3] + " to the team " + args[2] + ". The player must be connected.");
					}
					else {
						tm.addPlayerToTeam(args[2], player);
						sender.sendMessage(cs + "The player " + args[3] + " was successfully added to the team " + args[2] + ".");
					}
				}
				else {
					sender.sendMessage(ce + "Syntax error, see /uh team.");
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("removeplayer")) {
				if(args.length == 3) { // /uh team removeplayer <player>
					
					Player player = p.getServer().getPlayer(args[3]);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(ce + "Unable to remove the player " + args[2] + " from his team. Either he is not connected or doesn't belong to a team.");
					}
					else {
						tm.removePlayerFromTeam(player);
						sender.sendMessage(cs + "The player " + args[2] + " was successfully removed from his team.");
					}
				}
				else {
					sender.sendMessage(ce + "Syntax error, see /uh team.");
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("list")) {
				if(tm.getTeams().size() == 0) {
					sender.sendMessage(ce + "There isn't any team to show.");
					return;
				}
				for(final UHTeam team : tm.getTeams()) {
					sender.sendMessage(team.getChatColor() + team.getName() + ChatColor.WHITE + " - " + ((Integer) team.getPlayers().size()).toString() + " players");
					for(final Player player : team.getPlayers()) {
						sender.sendMessage(ChatColor.LIGHT_PURPLE + " - " + player.getName());
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("reset")) {
				tm.reset();
				sender.sendMessage(cs + "All teams where removed.");
			}
			else {
				sender.sendMessage(ce + "Unknown command. See /uh team for availables commands.");
			}
		}
	}
	
	/**
	 * This command shifts an episode.
	 * Usage: /uh shift (during the game).
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doShift(CommandSender sender, Command command, String label, String[] args) {
		if(p.getGameManager().isGameRunning()) {
			if(sender instanceof Player) {
				p.getGameManager().shiftEpisode((((Player) sender).getName()));
			}
			else {
				p.getGameManager().shiftEpisode("la console");
			}
		}
		else {
			sender.sendMessage(ce + "You can't shift the current episode because the game is not started.");
		}
	}
	
	
	/**
	 * This command heals a player.
	 * Usage: /uh heal <player> <half-hearts>
	 * 
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doHeal(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2 || args.length > 3) {
			sender.sendMessage(ce + "Usage: /uh heal <player> [number of half-hearts = 20]");
			return;
		}
		
		Player player = p.getServer().getPlayer(args[1]);
		if(player == null) {
			sender.sendMessage(ce + "The player " + args[1] + " is not online.");
			return;
		}
		
		double health = 0D;
		
		if(args.length == 2) { // /uh heal <player> : full life for player.
			health = 20D;
		}
		else { // /uh heal <player> <hearts>
			try {
				health = Double.parseDouble(args[2]);
			}
			catch(NumberFormatException e) {
				sender.sendMessage(ce + "Hey, this is not a number of half-hearts. It's a text. Pfff.");
				return;
			}
			
			if(health <= 0D) {
				sender.sendMessage(ce + "You can't kill a player with this command, to avoid typo fails.");
				return;
			}
			else if(health > 20D) {
				health = 20D;
			}
		}
		
		player.setHealth(health);
		p.getGameManager().updatePlayerListName(player);
	}
	
	/**
	 * This command heals all players.
	 * Usage: /uh healall <half-hearts>
	 * 
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doHealall(CommandSender sender, Command command, String label, String[] args) {
		String healthArg = null;
		if(args.length == 1) {
			healthArg = "20";
		}
		else {
			healthArg = args[1];
		}
		
		try {
			if(Double.parseDouble(healthArg) <= 0D) {
				sender.sendMessage(ce + "Serial killer!");
				return;
			}
		}
		catch(NumberFormatException e) { } // See this.doHeal(..) .
		
		for(final Player player : p.getServer().getOnlinePlayers()) {
			String[] argsToHeal = {"heal", player.getName(), healthArg};
			doHeal(sender, command, label, argsToHeal);
		}
	}
	
	
	/**
	 * This command resurrect a player.
	 * Usage: /uh resurrect <player>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doResurrect(CommandSender sender, Command command, String label, String[] args) {
		if(args.length != 2) {
			sender.sendMessage(ce + "Usage: /uh resurrect <player>");
			return;
		}
		
		Player player = p.getServer().getPlayer(args[1]);
		if(player == null) {
			sender.sendMessage(ce + "The player " + args[1] + " is not online.");
			return;
		}
		
		p.getGameManager().resurrect(player);
		
	}
	
	
	
	
	public ArrayList<String> getCommands() {
		return commands;
	}

	public ArrayList<String> getTeamCommands() {
		return teamCommands;
	}
	
}
