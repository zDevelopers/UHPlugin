package me.azenet.UHPlugin;

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
	private ChatColor cs = ChatColor.DARK_GRAY; // success message
	
	public UHPluginCommand(UHPlugin p) {
		this.p = p;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("uh")) {
			return false;
		}
		
		if(args.length == 0) {
			help(sender, false);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("start")) {
			doStart(sender, command, label, args);
			return true;
		}
		else if(args[0].equalsIgnoreCase("shift")) {
			doShift(sender, command, label, args);
			return true;
		}
		else if(args[0].equalsIgnoreCase("team")) {
			doTeam(sender, command, label, args);
			return true;
		}
		else if(args[0].equalsIgnoreCase("addspawn")) {
			doAddSpawn(sender, command, label, args);
			return true;
		}
		else if(args[0].equalsIgnoreCase("generatewalls")) {
			doGenerateWalls(sender, command, label, args);
			return true;
		}
		
		else {
			help(sender, true);
			return true;
		}
	}

	/**
	 * Print the help.
	 * 
	 * @param sender
	 * @param error True if the help is printed because the used typed an unknown command.
	 */
	private void help(CommandSender sender, boolean error) {
		if(error) {
			sender.sendMessage(ce + "This subcommand does not exists.");
			sender.sendMessage("");
		}
		sender.sendMessage(ci + "Available subcommands:");
		sender.sendMessage(cc + "/uh start " + ci + ": launchs the game");
		sender.sendMessage(cc + "/uh shift " + ci + ": shifts an episode");
		sender.sendMessage(cc + "/uh team " + ci + ": manages the teams (execute /uh team for more details)");
		sender.sendMessage(cc + "/uh addspawn " + ci + ": adds a spawn point for a team or a player, at the current location of the sender");
		sender.sendMessage(cc + "/uh addspawn <x> <z> " + ci + ": adds a spawn point for a team or a player, at the provided coordinates");
		sender.sendMessage(cc + "/uh generatewalls " + ci + ": generates the walls according to the configuration");
		sender.sendMessage("");
		sender.sendMessage(ChatColor.DARK_GRAY + "Tip: you can put one coordinate per line, following the format “x,y” in a “plugins/UHPlugin/positions.txt” file instead of using /uh addspawn each time.");
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
	private void doStart(CommandSender sender, Command command, String label, String[] args) {
		if(!isAllowed(sender, "start")) {
			unauthorized(sender, command);
			return;
		}
		p.startGame();
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
	private void doGenerateWalls(CommandSender sender, Command command, String label, String[] args) {
		if(!isAllowed(sender, "generatewalls")) {
			unauthorized(sender, command);
			return;
		}
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ce + "This command must be executed by a player.");
			return;
		}
		
		sender.sendMessage(cs + "Generating the walls...");
		
		World world = ((Player) sender).getWorld();;
		try {
			p.generateWalls(world);
		}
		catch(Exception e) {
			sender.sendMessage(ce + "An error occured, see console for details.");
			e.printStackTrace();
		}
		
		sender.sendMessage(cs + "Generation done.");
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
	private void doAddSpawn(CommandSender sender, Command command, String label, String[] args) {
		if(!isAllowed(sender, "addspawn")) {
			unauthorized(sender, command);
			return;
		}
		
		if(args.length == 1) { // No coordinates given.
			if(!(sender instanceof Player)) {
				sender.sendMessage(ce + "Yo need to specify the coordinates from the console.");
				sender.sendMessage(ce + "Usage: /uh addspawn x z");
				return;
			}
			else {
				Player pl = (Player) sender; // Just a way to avoid casts everywhere.
				p.addLocation(pl.getLocation().getBlockX(), pl.getLocation().getBlockZ());
				sender.sendMessage(cs + "Spawn added: " + pl.getLocation().getBlockX() + "," + pl.getLocation().getBlockZ());
			}
		}
		else if(args.length == 2) { // Two coordinates needed!
			sender.sendMessage(ce + "You need to specify two coordinates.");
		}
		else {
			p.addLocation(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
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
	private void doTeam(CommandSender sender, Command command, String label, String[] args) {
		if(!isAllowed(sender, "team")) {
			unauthorized(sender, command);
			return;
		}
		
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
						tm.addTeam(color, args[2]);
						sender.sendMessage(cs + "Team " + args[2] + " added.");
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
						tm.addTeam(color, args[3]);
						sender.sendMessage(cs + "Team " + args[3] + " (" + args[2] + ") added.");
					}
					
				}
				else {
					sender.sendMessage(ce + "Syntax error, see /uh team.");
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("remove")) {
				if(args.length == 3) { // /uh team remove <teamName>
					if(!tm.removeTeam(args[2])) {
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
				// TODO
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
	private void doShift(CommandSender sender, Command command, String label, String[] args) {
		if(!isAllowed(sender, "shift")) {
			unauthorized(sender, command);
			return;
		}
		
		if(p.isGameRunning()) {
			if(sender instanceof Player) {
				p.shiftEpisode((Player) sender);
			}
			else {
				p.shiftEpisode();
			}
		}
		else {
			sender.sendMessage(ce + "You can't shift the current episode because the game is not started.");
		}
	}
	
}
