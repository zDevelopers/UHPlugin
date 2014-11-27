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

package me.azenet.UHPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import me.azenet.UHPlugin.i18n.I18n;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class UHPluginCommand implements CommandExecutor {
	
	private UHPlugin p = null;
	
	private ArrayList<String> commands = new ArrayList<String>();
	private ArrayList<String> teamCommands = new ArrayList<String>();
	private ArrayList<String> spawnsCommands = new ArrayList<String>();
	private ArrayList<String> tpCommands = new ArrayList<String>();
	private ArrayList<String> timersCommands = new ArrayList<String>();
	private ArrayList<String> specCommands = new ArrayList<String>();
	private ArrayList<String> borderCommands = new ArrayList<String>();
	private ArrayList<String> freezeCommands = new ArrayList<String>();
	
	private I18n i = null;


	public UHPluginCommand(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
		
		commands.add("about");
		commands.add("start");
		commands.add("shift");
		commands.add("team");
		commands.add("spawns");
		commands.add("generatewalls");
		commands.add("border");
		commands.add("heal");
		commands.add("healall");
		commands.add("feed");
		commands.add("feedall");
		commands.add("freeze");
		commands.add("kill");
		commands.add("resurrect");
		commands.add("tpback");
		commands.add("spec");
		commands.add("finish");
		commands.add("tp");
		commands.add("timers");
		
		teamCommands.add("add");
		teamCommands.add("remove");
		teamCommands.add("join");
		teamCommands.add("leave");
		teamCommands.add("list");
		teamCommands.add("reset");
		
		spawnsCommands.add("add");
		spawnsCommands.add("generate");
		spawnsCommands.add("remove");
		spawnsCommands.add("list");
		spawnsCommands.add("dump");
		spawnsCommands.add("reset");
		
		tpCommands.add("team");
		tpCommands.add("spectators");

		timersCommands.add("add");
		timersCommands.add("set");
		timersCommands.add("display");
		timersCommands.add("hide");
		timersCommands.add("start");
		timersCommands.add("pause");
		timersCommands.add("resume");
		timersCommands.add("stop");
		timersCommands.add("remove");
		timersCommands.add("list");
		
		specCommands.add("add");
		specCommands.add("remove");
		specCommands.add("list");
		
		borderCommands.add("current");
		borderCommands.add("set");
		borderCommands.add("warning");
		borderCommands.add("check");
		
		freezeCommands.add("all");
		freezeCommands.add("none");
		freezeCommands.add("on");
		freezeCommands.add("off");
	}

	
	/**
	 * Handles a command.
	 * 
	 * @param sender The sender
	 * @param command The executed command
	 * @param label The alias used for this command
	 * @param args The arguments given to the command
	 * 
	 * @author Amaury Carrade
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		boolean ourCommand = false;
		for(String commandName : p.getDescription().getCommands().keySet()) {
			if(commandName.equalsIgnoreCase(command.getName())) {
				ourCommand = true;
				break;
			}
		}
		
		if(!ourCommand) {
			return false;
		}
		
		/** Team chat commands **/
		
		if(command.getName().equalsIgnoreCase("t")) { 
			doTeamMessage(sender, command, label, args);
			return true;
		}
		if(command.getName().equalsIgnoreCase("g")) { 
			doGlobalMessage(sender, command, label, args);
			return true;
		}
		if(command.getName().equalsIgnoreCase("togglechat")) { 
			doToggleTeamChat(sender, command, label, args);
			return true;
		}
		
		/** /join & /leave commands **/
		
		if(command.getName().equalsIgnoreCase("join")) {
			doJoin(sender, command, label, args);
			return true;
		}
		if(command.getName().equalsIgnoreCase("leave")) {
			doLeave(sender, command, label, args);
			return true;
		}
		
		
		if(args.length == 0) {
			help(sender, args, false);
			return true;
		}
		
		String subcommandName = args[0].toLowerCase();
		
		// First: subcommand existence.
		if(!this.commands.contains(subcommandName)) {
			try {
				Integer.valueOf(subcommandName);
				help(sender, args, false);
			} catch(NumberFormatException e) { // If the subcommand isn't a number, it's an error.
				help(sender, args, true);
			}
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
			help(sender, args, true);
			return true;
			
		} catch(SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(i.t("cmd.errorLoad"));
			e.printStackTrace();
			return false;
		}
	}



	/**
	 * Prints the help.
	 * 
	 * @param sender
	 * @param error True if the help is printed because the user typed an unknown command.
	 */
	private void help(CommandSender sender, String[] args, boolean error) {
		if(error) {
			sender.sendMessage(i.t("cmd.errorUnknown"));
			return;
		}
		
		displaySeparator(sender);
		
		sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
		
		sender.sendMessage(i.t("cmd.legendHelp"));
		
		if(!(sender instanceof Player)) {
			helpPage(sender, 1);
			helpPage(sender, 2);
			helpPage(sender, 3);
		}
		else {
			int page = 0;
			if(args.length == 0) {
				page = 1;
			}
			else {
				page = Integer.valueOf(args[0]) <= 3 ? Integer.valueOf(args[0]) : 3;
			}
			
			helpPage(sender, page);
			if(page < 3) sender.sendMessage(i.t("cmd.helpNextPage", String.valueOf(page + 1)));
		}
		
		displaySeparator(sender);
	}
	
	/**
	 * Prints a page of the help (for the player screen).
	 * 
	 * @param sender The sender
	 * @param page The page to print (1 to 3).
	 */
	private void helpPage(CommandSender sender, int page) {
		switch(page) {
			case 1:
				sender.sendMessage(i.t("cmd.titleGameCmd"));
				sender.sendMessage(i.t("cmd.helpStart"));
				sender.sendMessage(i.t("cmd.helpStartSlow"));
				sender.sendMessage(i.t("cmd.helpShift"));
				sender.sendMessage(i.t("cmd.helpTeam"));
				sender.sendMessage(i.t("cmd.helpSpawns"));
				sender.sendMessage(i.t("cmd.helpSpec"));
				sender.sendMessage(i.t("cmd.helpWall"));
				sender.sendMessage(i.t("cmd.helpBorder"));
				break;
			case 2:
				sender.sendMessage(i.t("cmd.titleBugCmd"));
				sender.sendMessage(i.t("cmd.helpHeal"));
				sender.sendMessage(i.t("cmd.helpHealall"));
				sender.sendMessage(i.t("cmd.helpFeed"));
				sender.sendMessage(i.t("cmd.helpFeedall"));
				sender.sendMessage(i.t("cmd.helpKill"));
				sender.sendMessage(i.t("cmd.helpResurrect"));
				sender.sendMessage(i.t("cmd.helpTpback"));
				break;
			case 3:
				sender.sendMessage(i.t("cmd.titleMiscCmd"));
				sender.sendMessage(i.t("cmd.helpFreeze"));
				sender.sendMessage(i.t("cmd.helpFinish"));
				sender.sendMessage(i.t("cmd.helpTP"));
				sender.sendMessage(i.t("cmd.helpTimers"));
				sender.sendMessage(i.t("cmd.helpAbout"));
				break;
		}
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
	 * This method sends a message to a player who try to use a command without the permission.
	 * 
	 * @param sender
	 * @param command
	 */
	private void unauthorized(CommandSender sender, Command command) {
		sender.sendMessage(i.t("cmd.errorUnauthorized"));
	}
	
	
	/**
	 * This command prints some informations about the plugin and the translation.
	 * 
	 * Usage: /uh about
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doAbout(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) sender.sendMessage("");
		sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
		
		// Authors
		
		String authors = "";
		List<String> listAuthors = p.getDescription().getAuthors();
		for(String author : listAuthors) {
			if(author == listAuthors.get(0)) {
				// Nothing
			}
			else if(author == listAuthors.get(listAuthors.size() - 1)) {
				authors += " " + i.t("about.and") + " ";
			}
			else {
				authors += ", ";
			}
			authors += author;
		}
		sender.sendMessage(i.t("about.authors", authors));
		
		// Build number
		
		String build = null;
		try {
			Class<? extends UHPlugin> clazz = p.getClass();
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			if (classPath.startsWith("jar")) { // Class from JAR
				String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + 
				    "/META-INF/MANIFEST.MF";
				Manifest manifest = new Manifest(new URL(manifestPath).openStream());
				Attributes attr = manifest.getMainAttributes();
				
				build = attr.getValue("Git-Commit");
			}
		} catch (IOException e) {
			// Build not available.
		}
		
		if(build != null) {
			sender.sendMessage(i.t("about.build.number", build));
		}
		else {
			sender.sendMessage(i.t("about.build.notAvailable"));
		}
		
		// Translation
		
		sender.sendMessage(i.t("about.i18n.title"));
		sender.sendMessage(i.t("about.i18n.selected", i.getSelectedLanguage(), i.getTranslator(i.getSelectedLanguage())));
		sender.sendMessage(i.t("about.i18n.fallback", i.getDefaultLanguage(), i.getTranslator(i.getDefaultLanguage())));
		sender.sendMessage(i.t("about.license.title"));
		sender.sendMessage(i.t("about.license.license"));
	}
	
	/**
	 * This command starts the game.
	 * 
	 * Usage: /uh start [slow [go]]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doStart(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1) { // /uh start (standard mode)
			try {
				p.getGameManager().start(sender, false);
			} catch(IllegalStateException e) {
				sender.sendMessage(i.t("start.already"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if(args.length == 2 && args[1].equalsIgnoreCase("slow")) { // /uh start slow
			try {
				p.getGameManager().start(sender, true);
			} catch(IllegalStateException e) {
				sender.sendMessage(i.t("start.already"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if(args.length == 3 && args[1].equalsIgnoreCase("slow") && args[2].equalsIgnoreCase("go")) { // /uh start slow go
			p.getGameManager().finalizeStartSlow(sender);
		}
		else {
			sender.sendMessage(i.t("start.syntax"));
		}
	}
	
	/**
	 * This command generates the walls around the map.
	 * 
	 * Usage: /uh generatewalls
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doGeneratewalls(CommandSender sender, Command command, String label, String[] args) {	
		sender.sendMessage(i.t("wall.startGen"));
		
		World world = null;
		
		if(sender instanceof Player) {
			world = ((Player) sender).getWorld();
		}
		else {
			world = p.getServer().getWorlds().get(0);
			sender.sendMessage(i.t("wall.consoleDefaultWorld", world.getName()));
		}
		
		try {
			UHWallGenerator wallGenerator = new UHWallGenerator(this.p, world);
			Boolean success = wallGenerator.build();
			
			if(!success) {
				sender.sendMessage(i.t("wall.error"));
			}
		}
		catch(Exception e) {
			sender.sendMessage(i.t("wall.unknownError"));
			e.printStackTrace();
		}
		
		sender.sendMessage(i.t("wall.done"));
	}

	/**
	 * This command adds a spawn point for a team or a player.
	 * 
	 * Usage: /uh spawns add (as a player, adds the current location, world included).
	 * Usage: /uh spawns add <x> <z> (as everyone, adds the specified coordinates in the default world).
	 * Usage: /uh spawns list (lists the spawn points).
	 * Usage: /uh spawns generate <circular|grid|random> [size = current size of the map] [distanceMin = 250] [count = number of teams registered]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doSpawns(CommandSender sender, Command command, String label, String[] args) {	
		if(args.length == 1) { // No subcommand given: doc
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.spawnsHelpTitle"));
			sender.sendMessage(i.t("cmd.spawnsHelpAdd"));
			sender.sendMessage(i.t("cmd.spawnsHelpAddXZ"));
			sender.sendMessage(i.t("cmd.spawnsHelpGenerate"));
			sender.sendMessage(i.t("cmd.spawnsHelpList"));
			sender.sendMessage(i.t("cmd.spawnsHelpDump"));
			sender.sendMessage(i.t("cmd.spawnsHelpRemove"));
			sender.sendMessage(i.t("cmd.spawnsHelpReset"));
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) { // /uh spawns add <?>
				
				// World?
				World world;
				if(sender instanceof Player) {
					world = ((Player) sender).getWorld();
				}
				else if(sender instanceof BlockCommandSender) {
					world = ((BlockCommandSender) sender).getBlock().getWorld();
				}
				else {
					world = p.getServer().getWorlds().get(0);
				}
				
				if(args.length == 2) { // /uh spawns add
					if(!(sender instanceof Player)) {
						sender.sendMessage(i.t("spawns.errorCoords"));
						return;
					}
					else {
						Player pl = (Player) sender; // Just a way to avoid casts everywhere.
						try {
							p.getSpawnsManager().addSpawnPoint(pl.getLocation());
							sender.sendMessage(i.t("spawns.add.added", world.getName(), String.valueOf(pl.getLocation().getBlockX()), String.valueOf(pl.getLocation().getBlockZ())));
						} catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("spawns.add.outOfLimits"));
						} catch(RuntimeException e) {
							sender.sendMessage(i.t("spawns.add.noSafeSpot"));
						}
					}
				}
				else if(args.length == 3) { // /uh spawns add <x>: Two coordinates needed!
					sender.sendMessage(i.t("spawns.error2Coords"));
				}
				else { // /uh spawns add <x> <z>
					try {
						p.getSpawnsManager().addSpawnPoint(world, Double.parseDouble(args[2]), Double.parseDouble(args[3]));
						sender.sendMessage(i.t("spawns.add.added", world.getName(), args[2], args[3]));
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("spawns.NaN"));
					} catch(IllegalArgumentException e) {
						sender.sendMessage(i.t("spawns.add.outOfLimits"));
					} catch(RuntimeException e) {
						sender.sendMessage(i.t("spawns.add.noSafeSpot"));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("list")) { // /uh spawns list
				List<Location> spawnPoints = p.getSpawnsManager().getSpawnPoints();
				
				if(spawnPoints.size() == 0) {
					sender.sendMessage(i.t("spawns.list.nothing"));
				}
				else {
					sender.sendMessage(i.t("spawns.list.count", String.valueOf(spawnPoints.size())));
					
					// We want one list per world
					Map<World,List<Location>> spanwsInWorlds = new HashMap<World,List<Location>>();
					for(World world : p.getServer().getWorlds()) {
						spanwsInWorlds.put(world, new LinkedList<Location>());
					}
					
					for(Location spawn : spawnPoints) {
						spanwsInWorlds.get(spawn.getWorld()).add(spawn);
					}
					
					for(Entry<World, List<Location>> spanwsInWorld : spanwsInWorlds.entrySet()) {
						if(spanwsInWorld.getValue().size() == 0) {
							continue;
						}
						
						sender.sendMessage(i.t("spawns.list.world", spanwsInWorld.getKey().getName()));
						
						String itemDisplay;
						if(spanwsInWorld.getKey().getEnvironment() == Environment.NORMAL) {
							itemDisplay = "spawns.list.item.overworld";
						}
						else if(spanwsInWorld.getKey().getEnvironment() == Environment.NETHER) {
							itemDisplay = "spawns.list.item.nether";
						}
						else if(spanwsInWorld.getKey().getEnvironment() == Environment.THE_END) {
							itemDisplay = "spawns.list.item.end";
						}
						else {
							itemDisplay = "spawns.list.item.other";
						}
						
						// Displaying this number of spawn points per line
						final Integer spawnsPerLine = 5;
						
						for(int j = 0; j < Math.ceil(Double.valueOf(spanwsInWorld.getValue().size()) / spawnsPerLine); j++) {
							String line = "";
							
							for(int k = 0; k < spawnsPerLine; k++) {
								if(spawnPoints.size() > j*spawnsPerLine + k) {
									line += i.t(itemDisplay, String.valueOf(spanwsInWorld.getValue().get(j*spawnsPerLine + k).getBlockX()), String.valueOf(spanwsInWorld.getValue().get(j*spawnsPerLine + k).getBlockZ())) + "  ";
								}
							}
							
							sender.sendMessage(line);
						}
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("dump")) { // /uh spawns dump
				
				// We want one list per world
				Map<World,List<Location>> spanwsInWorlds = new HashMap<World,List<Location>>();
				for(World world : p.getServer().getWorlds()) {
					spanwsInWorlds.put(world, new LinkedList<Location>());
				}
				
				for(Location spawn : p.getSpawnsManager().getSpawnPoints()) {
					spanwsInWorlds.get(spawn.getWorld()).add(spawn);
				}
				
				String dump = "";
				
				for(Entry<World, List<Location>> spanwsInWorld : spanwsInWorlds.entrySet()) {
					if(spanwsInWorld.getValue().size() == 0) {
						continue;
					}
					
					dump += "\n* " + spanwsInWorld.getKey().getName() + "\n";
					
					for(Location spawn : spanwsInWorld.getValue()) {
						dump += spawn.getBlockX() + "," + spawn.getBlockZ() + "\n";
					}
				}
				
				sender.sendMessage(dump);
			}
			
			else if(subcommand.equalsIgnoreCase("generate")) { // /uh spawns generate
				// Usage: /uh spawns generate <circular|squared|random> [size = current size of the map] [distanceMin = 250] [count = number of teams registered] [xCenter = xSpawn] [zCenter = zSpawn] [world = sender's world]
				
				if(args.length < 3) { // No enough arguments.
					sender.sendMessage(i.t("spawns.syntaxError"));
					return;
				}
				
				String generationMethod = args[2];
				
				// Default values
				Integer size = p.getBorderManager().getCurrentBorderDiameter() - 25; // Avoid spawn points being too close to the border
				Integer distanceMinBetweenTwoPoints = 250;
				Integer spawnsCount = p.getTeamManager().getTeams().size();
				World world = p.getServer().getWorlds().get(0);
				Double xCenter = world.getSpawnLocation().getX();
				Double zCenter = world.getSpawnLocation().getZ();
				
				if(args.length < 9) {
					if(sender instanceof Player) {
						world = ((Player) sender).getWorld();
					}
					else if(sender instanceof BlockCommandSender) {
						world = ((BlockCommandSender) sender).getBlock().getWorld();
					}
					else {
						world = p.getServer().getWorlds().get(0);
					}
					
					xCenter = world.getSpawnLocation().getX();
					zCenter = world.getSpawnLocation().getZ();
				}
				
				// What if the game is in solo, or some players are out of all team?
				// Only if the spawn count is not provided of course. Else, we don't care, this count
				// will be overwritten.
				if(args.length < 6) {
					if(spawnsCount == 0) { // Solo mode?
						sender.sendMessage(i.t("spawns.assumptions.solo"));
						spawnsCount = p.getServer().getOnlinePlayers().length - p.getGameManager().getSpectators().size();
					}
					else {
						// Trying to found players without team
						int playersWithoutTeam = 0;
						for(Player player : p.getServer().getOnlinePlayers()) {
							if(p.getTeamManager().getTeamForPlayer(player) == null) {
								playersWithoutTeam++;
							}
						}
						
						if(playersWithoutTeam != 0) {
							sender.sendMessage(i.t("spawns.assumptions.partialSolo"));
							spawnsCount += playersWithoutTeam;
						}
					}
				}
				
				try {
					if(args.length >= 4) { // size included
						size = Integer.valueOf(args[3]);
						
						if(args.length >= 5) { // distance minimal included
							distanceMinBetweenTwoPoints = Integer.valueOf(args[4]);
							
							if(args.length >= 6) { // spawn count included
								spawnsCount = Integer.valueOf(args[5]);
								
								if(args.length >= 7) { // xCenter included
									xCenter = Double.parseDouble(args[6]);
									
									if(args.length >= 8) { // zCenter included
										zCenter = Double.parseDouble(args[7]);
										
										if(args.length >= 9) { // world included
											World inputWorld = p.getServer().getWorld(args[8]);
											
											if(inputWorld != null) {
												world = inputWorld;
											}
											else {
												sender.sendMessage(i.t("spawns.generate.unknownWorld", args[8]));
												return;
											}
										}
									}
								}
							}
						}
					}
				} catch(NumberFormatException e) {
					sender.sendMessage(i.t("spawns.NaN"));
					return;
				}
				
				
				if(spawnsCount <= 0) {
					sender.sendMessage(i.t("spawns.generate.nothingToDo"));
					return;
				}
				
				
				boolean success;
				switch(generationMethod) {
					case "random":
						success = p.getSpawnsManager().generateRandomSpawnPoints(world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);
						break;
					
					case "grid":
						success = p.getSpawnsManager().generateGridSpawnPoints(world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);
						break;
					
					case "circular":
						success = p.getSpawnsManager().generateCircularSpawnPoints(world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);
						break;
					
					default:
						sender.sendMessage(i.t("spawns.generate.unsupportedMethod", generationMethod));
						return;
				}
				
				if(success) {
					sender.sendMessage(i.t("spawns.generate.success"));
				}
				else {
					sender.sendMessage(i.t("spawns.generate.impossible"));
				}
			}
			
			else if(subcommand.equalsIgnoreCase("remove")) { // /uh spawns remove <x> <z>
				if(args.length == 2) { // /uh spawns remove
					if(!(sender instanceof Player)) {
						sender.sendMessage(i.t("spawns.errorCoords"));
						return;
					}
					else {
						Player pl = (Player) sender; // Just a way to avoid casts everywhere.
						p.getSpawnsManager().removeSpawnPoint(pl.getLocation(), false);
						sender.sendMessage(i.t("spawns.remove.removed", pl.getWorld().getName(), String.valueOf(pl.getLocation().getBlockX()), String.valueOf(pl.getLocation().getBlockZ())));
					}
				}
				else if(args.length == 3) { // /uh spawns add <x>: Two coordinates needed!
					sender.sendMessage(i.t("spawns.error2Coords"));
				}
				else { // /uh spawns remove <x> <z>
					try {
						World world;
						if(sender instanceof Player) {
							world = ((Player) sender).getWorld();
						}
						else {
							world = p.getServer().getWorlds().get(0);
						}
						
						p.getSpawnsManager().removeSpawnPoint(new Location(world, Double.parseDouble(args[2]), 0, Double.parseDouble(args[3])), true);
						sender.sendMessage(i.t("spawns.remove.removed", p.getServer().getWorlds().get(0).getName(), args[2], args[3]));
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("spawns.NaN"));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("reset")) { // /uh spawns reset
				p.getSpawnsManager().reset();
				sender.sendMessage(i.t("spawns.reset"));
			}
		}
	}

	/**
	 * This command is used to manage the teams.
	 * 
	 * Usage: /uh team (for the doc).
	 * Usage: /uh team <add|remove|join|leave|list|reset> (see doc for details).
	 * 	
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTeam(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // No action provided: doc
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.teamHelpTitle"));
			sender.sendMessage(i.t("cmd.teamHelpAdd"));
			sender.sendMessage(i.t("cmd.teamHelpAddName"));
			sender.sendMessage(i.t("cmd.teamHelpRemove"));
			sender.sendMessage(i.t("cmd.teamHelpJoin"));
			sender.sendMessage(i.t("cmd.teamHelpLeave"));
			sender.sendMessage(i.t("cmd.teamHelpList"));
			sender.sendMessage(i.t("cmd.teamHelpReset"));
			sender.sendMessage(i.t("cmd.teamHelpJoinCmd"));
			sender.sendMessage(i.t("cmd.teamHelpLeaveCmd"));
			displaySeparator(sender);
		}
		else {
			UHTeamManager tm = p.getTeamManager();
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) {
				if(args.length == 3) { // /uh team add <color>
					
					TeamColor color = TeamColor.fromString(args[2]);
					UHTeam team;
					
					if(color == null) {
						sender.sendMessage(i.t("team.add.errorColor"));
					}
					else {
						try {
							team = tm.addTeam(color);
						}
						catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("team.add.errorExists"));
							return;
						}
						
						sender.sendMessage(i.t("team.add.added", team.getDisplayName()));
					}
				
				}
				else if(args.length >= 4) { // /uh team add <color> <name ...>
					
					TeamColor color = TeamColor.fromString(args[2]);
					UHTeam team;
					
					if(color == null) {
						sender.sendMessage(i.t("team.add.errorColor"));
					}
					else {
						String name = UHUtils.getStringFromCommandArguments(args, 3);
						
						try {
							team = tm.addTeam(color, name);
						}
						catch(IllegalArgumentException e) {
							e.printStackTrace();
							sender.sendMessage(i.t("team.add.errorExists"));
							return;
						}
						
						sender.sendMessage(i.t("team.add.added", team.getDisplayName()));
					}
					
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("remove")) {
				if(args.length >= 3) { // /uh team remove <teamName>
					String name = UHUtils.getStringFromCommandArguments(args, 2);
					if(!tm.removeTeam(name)) {
						sender.sendMessage(i.t("team.remove.doesNotExists"));
					}
					else {
						sender.sendMessage(i.t("team.remove.removed", name));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("join")) {
				if(args.length >= 4) { // /uh team join <player> <teamName>
					
					Player player = p.getServer().getPlayer(args[2]);
					String teamName = UHUtils.getStringFromCommandArguments(args, 3);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(i.t("team.addplayer.disconnected", args[2], teamName));
					}
					else {
						try {
							tm.addPlayerToTeam(teamName, player);
						} catch(IllegalArgumentException e) {
							sender.sendMessage(i.t("team.addplayer.doesNotExists"));
							return;
						}
						catch(RuntimeException e) {
							sender.sendMessage(i.t("team.addplayer.full", teamName));
							return;
						}
						UHTeam team = p.getTeamManager().getTeam(teamName);
						sender.sendMessage(i.t("team.addplayer.success", args[2], team.getDisplayName()));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("leave")) {
				if(args.length == 3) { // /uh team leave <player>
					
					Player player = p.getServer().getPlayer(args[2]);
					
					if(player == null || !player.isOnline()) {
						sender.sendMessage(i.t("team.removeplayer.disconnected", args[2]));
					}
					else {
						tm.removePlayerFromTeam(player);
						sender.sendMessage(i.t("team.removeplayer.success", args[2]));
					}
				}
				else {
					sender.sendMessage(i.t("team.syntaxError"));
				}
			}
			
			
			else if(subcommand.equalsIgnoreCase("list")) {
				if(tm.getTeams().size() == 0) {
					sender.sendMessage(i.t("team.list.nothing"));
					return;
				}
				
				for(final UHTeam team : tm.getTeams()) {
					sender.sendMessage(i.t("team.list.itemTeam",  team.getDisplayName(), ((Integer) team.getSize()).toString()));
					for(final OfflinePlayer player : team.getPlayers()) {
						String bullet = null;
						if(player.isOnline()) {
							bullet = i.t("team.list.bulletPlayerOnline");
						}
						else {
							bullet = i.t("team.list.bulletPlayerOffline");
						}
						
						if(!p.getGameManager().isGameRunning()) {
							sender.sendMessage(bullet + i.t("team.list.itemPlayer", player.getName()));
						}
						else {
							if(p.getGameManager().isPlayerDead(player.getUniqueId())) {
								sender.sendMessage(bullet + i.t("team.list.itemPlayerDead", player.getName()));
							}
							else {
								sender.sendMessage(bullet + i.t("team.list.itemPlayerAlive", player.getName()));
							}
						}
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("reset")) {
				tm.reset();
				sender.sendMessage(i.t("team.reset.success"));
			}
			
			else {
				sender.sendMessage(i.t("team.unknownCommand"));
			}
		}
	}
	
	/**
	 * This command shifts an episode.
	 * 
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
				p.getGameManager().shiftEpisode(i.t("shift.consoleName"));
			}
		}
		else {
			sender.sendMessage(i.t("shift.cantNotStarted"));
		}
	}
	
	
	/**
	 * This command heals a player.
	 * 
	 * Usage: /uh heal <player> <half-hearts>
	 * 
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doHeal(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2 || args.length > 3) {
			sender.sendMessage(i.t("heal.usage"));
			return;
		}
		
		Player player = p.getServer().getPlayer(args[1]);
		if(player == null || !player.isOnline()) {
			sender.sendMessage(i.t("heal.offline"));
			return;
		}
		
		double health = 0D;
		boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode
		
		if(args.length == 2) { // /uh heal <player> : full life for player.
			health = 20D;
		}
		else { // /uh heal <player> <hearts>
			double diffHealth = 0D;
			
			try {
				if(args[2].startsWith("+")) {
					diffHealth = Double.parseDouble(args[2].substring(1));
					add = true;
				}
				else if(args[2].startsWith("-")) {
					diffHealth = -1 * Double.parseDouble(args[2].substring(1));
					add = true;
				}
				else {
					diffHealth = Double.parseDouble(args[2]);
				}
			}
			catch(NumberFormatException e) {
				sender.sendMessage(i.t("heal.errorNaN"));
				return;
			}
			
			health = !add ? diffHealth : player.getHealth() + diffHealth;
			
			if(health <= 0D) {
				sender.sendMessage(i.t("heal.errorNoKill"));
				return;
			}
			else if(health > 20D) {
				health = 20D;
			}
		}
		
		player.setHealth(health);
	}
	
	/**
	 * This command heals all players.
	 * 
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
		double diffHealth = 0D;
		double health = 0D;
		boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode
		
		if(args.length == 1) { // /uh healall : full life for all players.
			diffHealth = 20D;
		}
		else { // /uh heal <player> <hearts>
			try {
				if(args[1].startsWith("+")) {
					diffHealth = Double.parseDouble(args[1].substring(1));
					add = true;
				}
				else if(args[1].startsWith("-")) {
					diffHealth = -1 * Double.parseDouble(args[1].substring(1));
					add = true;
				}
				else {
					diffHealth = Double.parseDouble(args[1]);
				}
			}
			catch(NumberFormatException e) {
				sender.sendMessage(i.t("heal.errorNaN"));
				return;
			}
		}
		
		if((!add && diffHealth <= 0) || diffHealth <= -20) {
			sender.sendMessage(i.t("heal.allErrorNoKill"));
			return;
		}
		
		for(final Player player : p.getServer().getOnlinePlayers()) {
			health = !add ? diffHealth : player.getHealth() + diffHealth;
			
			if(health <= 0D) {
				sender.sendMessage(i.t("heal.errorHealthNotUpdatedNoKill", player.getName()));
				continue;
			}
			else if(health > 20D) {
				health = 20D;
			}
			
			player.setHealth(health);
		}
	}
	
	/**
	 * This command feeds a player.
	 * <p>
	 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doFeed(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(i.t("feed.usage"));
			return;
		}
		
		Player target = p.getServer().getPlayer(args[1]);
		if(target == null || !target.isOnline()) {
			sender.sendMessage(i.t("feed.offline"));
			return;
		}
		
		int   foodLevel  = 20;
		float saturation = 20f;
		
		if(args.length > 2) { // /uh feed <player> <foodLevel>
			try {
				foodLevel = Integer.valueOf(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("feed.errorNaN"));
				return;
			}
			
			if(args.length > 3) { // /uh feed <player> <foodLevel> <saturation>
				try {
					// The saturation value cannot be more than the food level.
					saturation = Math.min(foodLevel, Float.valueOf(args[3]));
				} catch(NumberFormatException e) {
					sender.sendMessage(i.t("feed.errorNaN"));
					return;
				}
			}
		}
		
		target.setFoodLevel(foodLevel);
		target.setSaturation(saturation);
	}
	
	/**
	 * This command feeds all player.
	 * <p>
	 * Usage: /uh feed &lt;player> [foodLevel=20] [saturation=20]
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doFeedall(CommandSender sender, Command command, String label, String[] args) {
		
		int   foodLevel  = 20;
		float saturation = 20f;
		
		if(args.length > 1) { // /uh feedall <foodLevel>
			try {
				foodLevel = Integer.valueOf(args[1]);
			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("feed.errorNaN"));
				return;
			}
			
			if(args.length > 2) { // /uh feedall <foodLevel> <saturation>
				try {
					// The saturation value cannot be more than the food level.
					saturation = Math.min(foodLevel, Float.valueOf(args[2]));
				} catch(NumberFormatException e) {
					sender.sendMessage(i.t("feed.errorNaN"));
					return;
				}
			}
		}
		
		for(Player player : p.getServer().getOnlinePlayers()) {
			player.setFoodLevel(foodLevel);
			player.setSaturation(saturation);
		}
	}
	
	
	/**
	 * This command marks a player as dead, even if he is offline.
	 * <p>
	 * If the player is online, this has the same effect as {@code /kill}.
	 * <p>
	 * Usage: /uh kill &lt;player>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doKill(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(i.t("kill.usage"));
			return;
		}
		
		OfflinePlayer player = p.getServer().getOfflinePlayer(args[1]);
		
		if(player == null) {
			sender.sendMessage(i.t("kill.neverPlayed"));
			return;
		}
		
		if(player.isOnline()) {
			((Player) player).setHealth(0);
		}
		else {
			p.getGameManager().addDead(player.getUniqueId());
			p.getGameManager().updateAliveCounters();
		}
		
		sender.sendMessage(i.t("kill.killed", player.getName()));
	}
	
	/**
	 * This command resurrects a player.
	 * 
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
			sender.sendMessage(i.t("resurrect.usage"));
			return;
		}
		
		boolean success = p.getGameManager().resurrect(args[1]);
		
		Player player = p.getServer().getPlayer(args[1]);
		if(player == null || !player.isOnline()) {
			if(!success) { // Player does not exists or is nod dead.
				sender.sendMessage(i.t("resurrect.unknownOrDead"));
			}
			else { // Resurrected
				sender.sendMessage(i.t("resurrect.offlineOk", args[1]));
			}
		}
		else {
			if(!success) { // The player is not dead
				sender.sendMessage(i.t("resurrect.notDead", args[1]));
			}
		}
	}
	
	/**
	 * This command safely teleports back a player to his death location.
	 * 
	 * Usage: /uh tpback <player>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTpback(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			sender.sendMessage(i.t("tpback.usage"));
			return;
		}
		
		Player player = p.getServer().getPlayer(args[1]);
		if(player == null || !player.isOnline()) {
			sender.sendMessage(i.t("tpback.offline", args[1]));
			return;
		}
		else if(!p.getGameManager().hasDeathLocation(player)) {
			sender.sendMessage(i.t("tpback.noDeathLocation", args[1]));
			return;
		}
		
		
		Location deathLocation = p.getGameManager().getDeathLocation(player);
		
		if(args.length >= 3 && args[2].equalsIgnoreCase("force")) {
			UHUtils.safeTP(player, deathLocation, true);
			sender.sendMessage(i.t("tpback.teleported", args[1]));
			p.getGameManager().removeDeathLocation(player);
		}
		else if(UHUtils.safeTP(player, deathLocation)) {
			sender.sendMessage(i.t("tpback.teleported", args[1]));
			p.getGameManager().removeDeathLocation(player);
		}
		else {
			sender.sendMessage(i.t("tpback.notTeleportedNoSafeSpot", args[1]));
			sender.sendMessage(i.t("tpback.notTeleportedNoSafeSpotCmd", args[1]));
		}
	}
	
	
	/**
	 * This command manages spectators (aka ignored players).
	 * 
	 * Usage: /uh spec (doc)
	 * Usage: /uh spec <add|remove|list>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doSpec(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // /uh spec
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			
			sender.sendMessage(i.t("cmd.legendHelp"));
			if(!p.getSpectatorPlusIntegration().isSPIntegrationEnabled()) {
				sender.sendMessage(i.t("cmd.specHelpNoticeSpectatorPlusNotInstalled"));
			}

			sender.sendMessage(i.t("cmd.specHelpTitle"));
			sender.sendMessage(i.t("cmd.specHelpAdd"));
			sender.sendMessage(i.t("cmd.specHelpRemove"));
			sender.sendMessage(i.t("cmd.specHelpList"));
			
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) {
				if(args.length == 2) { // /uh spec add
					sender.sendMessage(i.t("spectators.syntaxError"));
				}
				else { // /uh spec add <player>
					Player newSpectator = p.getServer().getPlayer(args[2]);
					if(newSpectator == null) {
						sender.sendMessage(i.t("spectators.offline", args[2]));
					}
					else {
						p.getGameManager().addSpectator(newSpectator);
						sender.sendMessage(i.t("spectators.add.success", args[2]));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("remove")) {
				if(args.length == 2) { // /uh spec remove
					sender.sendMessage(i.t("spectators.syntaxError"));
				}
				else { // /uh spec remove <player>
					Player oldSpectator = p.getServer().getPlayer(args[2]);
					if(oldSpectator == null) {
						sender.sendMessage(i.t("spectators.offline", args[2]));
					}
					else {
						p.getGameManager().removeSpectator(oldSpectator);
						sender.sendMessage(i.t("spectators.remove.success", args[2]));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("list")) {
				HashSet<String> spectators = p.getGameManager().getSpectators();
				if(spectators.size() == 0) {
					sender.sendMessage(i.t("spectators.list.nothing"));
				}
				else {
					sender.sendMessage(i.t("spectators.list.countSpectators", String.valueOf(spectators.size())));
					sender.sendMessage(i.t("spectators.list.countOnlyInitial"));
					for(String spectator : spectators) {
						sender.sendMessage(i.t("spectators.list.itemSpec", spectator));
					}
				}
			}
		}
	}
	
	
	/**
	 * This command manages borders (gets current, checks if players are out, sets a new size, warnings players
	 * about the future size).
	 * 
	 * Usage: /uh border (doc)
	 * Usage: /uh border <current|set [force]|warning|check>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doBorder(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // /uh border
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.borderHelpTitle"));
			sender.sendMessage(i.t("cmd.borderHelpCurrent"));
			sender.sendMessage(i.t("cmd.borderHelpSet"));
			sender.sendMessage(i.t("cmd.borderHelpWarning"));
			sender.sendMessage(i.t("cmd.borderHelpWarningCancel"));
			sender.sendMessage(i.t("cmd.borderHelpCheck"));
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("current")) { // /uh border current
				if(p.getBorderManager().isCircularBorder()) {
					sender.sendMessage(i.t("borders.current.messageCircular", String.valueOf(p.getBorderManager().getCurrentBorderDiameter())));
				}
				else {
					sender.sendMessage(i.t("borders.current.messageSquared", String.valueOf(p.getBorderManager().getCurrentBorderDiameter())));
				}
			}
			
			else if(subcommand.equalsIgnoreCase("set")) { // /uh border set
				if(args.length == 2) { // /uh border set
					sender.sendMessage(i.t("borders.syntaxError"));					
				}
				else if(args.length == 3) { // /uh border set <?>
					try {
						Integer newDiameter = Integer.valueOf(args[2]);
						
						if(p.getBorderManager().getPlayersOutside(newDiameter).size() != 0) { // Some players are outside
							sender.sendMessage(i.t("borders.set.playersOutsideCanceled"));
							sender.sendMessage(i.t("borders.set.playersOutsideCanceledCmd", args[2]));
							if(!p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
								sender.sendMessage(i.t("borders.set.playersOutsideCanceledWarnWorldBorder"));
							}
							p.getBorderManager().sendCheckMessage(sender, newDiameter);
						}
						else {
							p.getBorderManager().setCurrentBorderDiameter(newDiameter);
							
							if(p.getBorderManager().isCircularBorder()) {
								p.getServer().broadcastMessage(i.t("borders.set.broadcastCircular", args[2]));
							}
							else {
								p.getServer().broadcastMessage(i.t("borders.set.broadcastSquared", args[2]));
							}
						}
						
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("borders.NaN", args[2]));
					}
				}
				else if(args.length == 4 && args[3].equalsIgnoreCase("force")) { // /uh border set <?> force
					try {
						Integer newDiameter = Integer.valueOf(args[2]);
						
						p.getBorderManager().setCurrentBorderDiameter(newDiameter);
						
						if(p.getBorderManager().isCircularBorder()) {
							p.getServer().broadcastMessage(i.t("borders.set.broadcastCircular", args[2]));
						}
						else {
							p.getServer().broadcastMessage(i.t("borders.set.broadcastSquared", args[2]));
						}
						
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("borders.NaN", args[2]));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("warning")) { // /uh border warning 
				if(args.length == 2) { // /uh border warning
					sender.sendMessage(i.t("borders.syntaxError"));					
				}
				else if(args[2].equalsIgnoreCase("cancel")) { // /uh border warning cancel
					p.getBorderManager().cancelWarning();
					sender.sendMessage(i.t("borders.warning.canceled"));
				}
				else { // /uh border warning <?>
					try {
						Integer warnDiameter = Integer.valueOf(args[2]);
						
						Integer warnTime = 0;
						if(args.length >= 4) { // /uh border warning <?> <?>
							warnTime = Integer.valueOf(args[3]);
						}
						
						p.getBorderManager().setWarningSize(warnDiameter, warnTime, sender);
						sender.sendMessage(i.t("borders.warning.set", p.getConfig().getString("map.border.warningInterval", "90")));
						
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("borders.NaN", args[2]));
					}
				}
			}
			else if(subcommand.equalsIgnoreCase("check")) {
				if(args.length == 2) { // /uh border check
					sender.sendMessage(i.t("borders.syntaxError"));					
				}
				else { // /uh border check <?>
					try {
						
						Integer checkDiameter = Integer.valueOf(args[2]);
						p.getBorderManager().sendCheckMessage(sender, checkDiameter);
						
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("borders.NaN", args[2]));
					}
				}
			}
		}
	}
	
	
	/**
	 * This commands broadcast the winner(s) of the game and sends some fireworks at these players.
	 * It fails if there is more than one team alive.
	 * 
	 * Usage: /uh finish
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doFinish(CommandSender sender, Command command, String label, String[] args) {
		
		try {
			p.getGameManager().finishGame();
			
		} catch(IllegalStateException e) {
			
			if(e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_STARTED)) {
				sender.sendMessage(i.t("finish.notStarted"));
			}
			else if(e.getMessage().equals(UHGameManager.FINISH_ERROR_NOT_FINISHED)) {
				sender.sendMessage(i.t("finish.notFinished"));
			}
			else {
				throw e;
			}
		}
		
	}
	
	/**
	 * This command teleports a team or the spectators to a given location.
	 * 
	 * Usage: /uh tp team <x> <y> <z> <team name ...>
	 * Usage: /uh tp team <target> <team name...>
	 * Usage: /uh tp spectators <x> <y> <z>
	 * Usage: /uh tp spectators <target>
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTp(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // No action provided: doc
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.tpHelpTitle"));
			sender.sendMessage(i.t("cmd.tpHelpTeam"));
			sender.sendMessage(i.t("cmd.tpHelpSpectators"));
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			World targetWorld = null;
			if(sender instanceof Player) {
				targetWorld = ((Player) sender).getWorld();
			}
			else if(sender instanceof BlockCommandSender) {
				targetWorld = ((BlockCommandSender) sender).getBlock().getWorld();
			}
			else {
				targetWorld = p.getServer().getWorlds().get(0);
			}
			
			if(subcommand.equalsIgnoreCase("team")) {
				boolean mayBeNaNError = false;
				
				if(args.length >= 6) { // possibly /uh tp team <x> <y> <z> <team ...>					
					String teamName = UHUtils.getStringFromCommandArguments(args, 5);
					UHTeam team = p.getTeamManager().getTeam(teamName);
					
					if(team != null) { // ok, the team exists.
						try {
							double x = Integer.parseInt(args[2]) + 0.5;
							double y = Integer.parseInt(args[3]) + 0.5;
							double z = Integer.parseInt(args[4]) + 0.5;
							
							team.teleportTo(new Location(targetWorld, x, y, z));
							
							return;
						} catch(NumberFormatException e) {
							// It can be either another name for the team, starting by "<y> <z> the name"
							// or a formatting error.
							// The possibility of an error is saved.
							mayBeNaNError = true;
						}
					}
				}
				if(args.length >= 4) { // /uh tp team <target> <team ...>
					String teamName = UHUtils.getStringFromCommandArguments(args, 3);
					UHTeam team = p.getTeamManager().getTeam(teamName);
					
					if(team == null) {
						if(mayBeNaNError) {
							sender.sendMessage(i.t("tp.NaN"));
						}
						else {
							sender.sendMessage(i.t("tp.teamDoesNotExists"));
						}
					}
					else {
						Player target = p.getServer().getPlayer(args[2]);
						
						if(target == null) {
							sender.sendMessage(i.t("tp.targetOffline", args[2]));
						}
						else {
							team.teleportTo(target.getLocation());
						}
					}
				}
			}
			else if(subcommand.equalsIgnoreCase("spectators")) {
				if(args.length == 5) { // /uh tp spectators <x> <y> <z>
					try {
						double x = Integer.parseInt(args[2]) + 0.5;
						double y = Integer.parseInt(args[3]) + 0.5;
						double z = Integer.parseInt(args[4]) + 0.5;
						
						for(Player player : p.getServer().getOnlinePlayers()) {
							if(p.getGameManager().isPlayerDead(player)) {
								player.teleport(new Location(targetWorld, x, y, z), TeleportCause.PLUGIN);
							}
						}
					} catch(NumberFormatException e) {
						sender.sendMessage(i.t("tp.NaN"));
						return;
					}
				}
				else if(args.length == 3) { // /uh tp spectators <target>
					Player target = p.getServer().getPlayer(args[2]);
					
					if(target == null) {
						sender.sendMessage(i.t("tp.targetOffline", args[2]));
					}
					else {
						for(Player player : p.getServer().getOnlinePlayers()) {
							if(p.getGameManager().isPlayerDead(player)) {
								player.teleport(target.getLocation(), TeleportCause.PLUGIN);
							}
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * This command manages timers.
	 * 
	 * Usage: /uh timers < add | set | display | hide | start | pause | resume | stop | remove | list >
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doTimers(CommandSender sender, Command command, String label, String[] args) throws NumberFormatException {
		if(args.length == 1) { // No action provided: doc
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));

			sender.sendMessage(i.t("cmd.timersHelpTitle"));
			sender.sendMessage(i.t("cmd.timersHelpAdd"));
			sender.sendMessage(i.t("cmd.timersHelpSet"));
			sender.sendMessage(i.t("cmd.timersHelpDisplay"));
			sender.sendMessage(i.t("cmd.timersHelpHide"));
			sender.sendMessage(i.t("cmd.timersHelpStart"));
			sender.sendMessage(i.t("cmd.timersHelpPause"));
			sender.sendMessage(i.t("cmd.timersHelpResume"));
			sender.sendMessage(i.t("cmd.timersHelpStop"));
			sender.sendMessage(i.t("cmd.timersHelpRemove"));
			sender.sendMessage(i.t("cmd.timersHelpList"));
			sender.sendMessage(i.t("cmd.timersHelpDurations"));
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("add")) { // /uh timers add <duration> <name ...>
				if(args.length < 4) {
					sender.sendMessage(i.t("timers.syntaxError"));
				}
				else {
					try {
						Integer duration = UHUtils.string2Time(args[2]);
						String timerName = UHUtils.getStringFromCommandArguments(args, 3);
						
						if(p.getTimerManager().getTimer(timerName) != null) {
							sender.sendMessage(i.t("timers.alreadyExists", timerName));
							return;
						}
						
						UHTimer timer = new UHTimer(timerName);
						timer.setDuration(duration);
						
						p.getTimerManager().registerTimer(timer);
						sender.sendMessage(i.t("timers.added", timer.getDisplayName(), args[2]));
						
					} catch(IllegalArgumentException e) {
						sender.sendMessage(i.t("timers.durationSyntaxError"));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("set")) { // /uh timers set <duration> <name ...>
				if(args.length < 4) {
					sender.sendMessage(i.t("timers.syntaxError"));
				}
				else {
					try {
						Integer duration = UHUtils.string2Time(args[2]);
						String timerName = UHUtils.getStringFromCommandArguments(args, 3);
						
						UHTimer timer = p.getTimerManager().getTimer(timerName);
						if(timer == null) {
							sender.sendMessage(i.t("timers.timerDoesNotExists"));
							return;
						}
						
						timer.setDuration(duration);
						sender.sendMessage(i.t("timers.set", timer.getDisplayName(), args[2]));
						
					} catch(IllegalArgumentException e) {
						sender.sendMessage(i.t("timers.durationSyntaxError"));
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("display")) { // /uh timers display <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				p.getScoreboardManager().displayTimer(timer);
				sender.sendMessage(i.t("timers.displayed", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("hide")) { // /uh timers hide <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				p.getScoreboardManager().hideTimer(timer);
				sender.sendMessage(i.t("timers.hidden", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("start")) { // /uh timers start <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				if(timer.isRunning()) {
					timer.stop();
				}
				
				timer.start();
				sender.sendMessage(i.t("timers.started", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("pause")) { // /uh timers pause <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				timer.setPaused(true);
				sender.sendMessage(i.t("timers.paused", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("resume")) { // /uh timers resume <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				timer.setPaused(false);
				sender.sendMessage(i.t("timers.resumed", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("stop")) { // /uh timers stop <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				timer.stop();
				sender.sendMessage(i.t("timers.stopped", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("remove")) { // /uh timers remove <name ...>
				String timerName = UHUtils.getStringFromCommandArguments(args, 2);
				
				UHTimer timer = p.getTimerManager().getTimer(timerName);
				if(timer == null) {
					sender.sendMessage(i.t("timers.timerDoesNotExists"));
					return;
				}
				
				p.getScoreboardManager().hideTimer(timer);
				p.getTimerManager().unregisterTimer(timer);
				timer.stop();
				
				sender.sendMessage(i.t("timers.removed", timer.getDisplayName()));
			}
			
			else if(subcommand.equalsIgnoreCase("list")) { // /uh timers list
				Collection<UHTimer> timers = p.getTimerManager().getTimers();
				
				sender.sendMessage(i.t("timers.list.count", String.valueOf(timers.size())));
				
				for(UHTimer timer : timers) {
					if(timer.isRunning()) {
						if(timer.isPaused()) {
							sender.sendMessage(i.t("timers.list.itemPaused", 
									timer.getDisplayName(),
									String.valueOf(timer.getDuration()),
									p.getScoreboardManager().getTimerText(timer, false, false)));
						}
						else {
							sender.sendMessage(i.t("timers.list.itemRunning", 
									timer.getDisplayName(),
									String.valueOf(timer.getDuration()),
									p.getScoreboardManager().getTimerText(timer, false, false)));

						}
					}
					else {
						sender.sendMessage(i.t("timers.list.itemStopped",
								timer.getDisplayName(),
								String.valueOf(timer.getDuration())));
					}
				}
			}
			
			else {
				sender.sendMessage(i.t("timers.syntaxError"));
			}
		}
	}
	
	
	/**
	 * This command freezes the players.
	 * 
	 * Usage: /uh freeze <on [player]|off [player]|all|none>
	 *  - on [player]: freezes the given player, or the sender if no player was provided.
	 *  - off [player]: unfreezes the given player (or the sender, same condition).
	 *  - all: freezes all the alive players, the mobs and the timer.
	 *  - none: unfreezes all the alive players (even if there where frozen before using
	 *          /uh freeze all), the mobs and the timer.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	@SuppressWarnings("unused")
	private void doFreeze(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1) { // /uh freeze
			displaySeparator(sender);
			sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));
			sender.sendMessage(i.t("cmd.legendHelp"));
			
			sender.sendMessage(i.t("cmd.freezeHelpTitle"));
			sender.sendMessage(i.t("cmd.freezeHelpOn"));
			sender.sendMessage(i.t("cmd.freezeHelpOff"));
			sender.sendMessage(i.t("cmd.freezeHelpAll"));
			sender.sendMessage(i.t("cmd.freezeHelpNone"));
			displaySeparator(sender);
		}
		else {
			String subcommand = args[1];
			
			if(subcommand.equalsIgnoreCase("on") || subcommand.equalsIgnoreCase("off")) {
				
				boolean on = subcommand.equalsIgnoreCase("on");
				
				if(args.length == 2) { // /uh freeze on: freezes the sender
					if(sender instanceof Player) {
						p.getFreezer().setPlayerFreezeState((Player) sender, on);
						if(on) {
							sender.sendMessage(i.t("freeze.frozen", ((Player) sender).getName()));
						}
						else {
							sender.sendMessage(i.t("freeze.unfrozen", ((Player) sender).getName()));
						}
					}
					else {
						sender.sendMessage(i.t("freeze.playerOnly"));
					}
				}
				else if(args.length == 3) { // /uh freeze on <player>: freezes <player>.
					Player player = p.getServer().getPlayer(args[2]);
					if(player == null) {
						sender.sendMessage(i.t("freeze.offline", args[2]));
					}
					else {
						p.getFreezer().setPlayerFreezeState(player, on);
						if(on) {
							player.sendMessage(i.t("freeze.frozen", ((Player) sender).getName()));
							sender.sendMessage(i.t("freeze.playerFrozen", player.getName()));
						}
						else {
							player.sendMessage(i.t("freeze.unfrozen", ((Player) sender).getName()));
							sender.sendMessage(i.t("freeze.playerUnfrozen", player.getName()));
						}
					}
				}
			}
			
			else if(subcommand.equalsIgnoreCase("all") || subcommand.equalsIgnoreCase("none")) {
				
				boolean on = subcommand.equalsIgnoreCase("all");
				
				p.getFreezer().setGlobalFreezeState(on);
				
				if(on) {
					p.getServer().broadcastMessage(i.t("freeze.broadcast.globalFreeze"));
				}
				else {
					p.getServer().broadcastMessage(i.t("freeze.broadcast.globalUnfreeze"));
				}
				
			}
		}
	}
	
	
	
	
	/**
	 * This command, /t <message>, is used to send a team-message.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doTeamMessage(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /t
			sender.sendMessage(i.t("team.message.usage", "t"));
			return;
		}
		
		String message = "";
		for(Integer i = 0; i < args.length; i++) {
			message += args[i] + " ";
		}
		
		p.getTeamChatManager().sendTeamMessage((Player) sender, message);
	}
	
	/**
	 * This command, /g <message>, is used to send a global message.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doGlobalMessage(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /g
			sender.sendMessage(i.t("team.message.usage", "g"));
			return;
		}
		
		String message = "";
		for(Integer i = 0; i < args.length; i++) {
			message += args[i] + " ";
		}
		
		p.getTeamChatManager().sendGlobalMessage((Player) sender, message);
	}
	
	/**
	 * This command, /togglechat, is used to toggle the chat between the global chat and the team chat.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doToggleTeamChat(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(i.t("team.message.noConsole"));
			return;
		}
		
		if(args.length == 0) { // /togglechat
			if(p.getTeamChatManager().toggleChatForPlayer((Player) sender)) {
				sender.sendMessage(i.t("team.message.toggle.nowTeamChat"));
			}
			else {
				sender.sendMessage(i.t("team.message.toggle.nowGlobalChat"));
			}
		}
		else { // /togglechat <another team>
			String teamName = UHUtils.getStringFromCommandArguments(args, 0);
			UHTeam team = p.getTeamManager().getTeam(teamName);
			
			if(team != null) {
				if(p.getTeamChatManager().toggleChatForPlayer((Player) sender, team)) {
					sender.sendMessage(i.t("team.message.toggle.nowOtherTeamChat", team.getDisplayName()));
				}
			}
			else {
				sender.sendMessage(i.t("team.message.toggle.unknownTeam"));
			}
		}
	}
	
	
	
	/**
	 * This command is used to allow a player to join a team.
	 * <p>
	 * Usage: /join [player] &lt;team&gt;
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doJoin(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 0) {
			if(sender instanceof Player) {
				if(sender.hasPermission("uh.player.join.self")) {
					p.getTeamManager().displayTeamChooserChatGUI((Player) sender);
				}
				else {
					if(sender.hasPermission("uh.player.join.others")) {
						sender.sendMessage(i.t("team.addplayer.joinhelp"));
					}
					else {
						unauthorized(sender, command);
					}
				}
			}
			else {
				sender.sendMessage(i.t("team.addplayer.joinhelp"));
			}
			
			return;
		}
		
		UHTeam  team   = null;
		Player  target = null;
		Boolean self   = null;
		
		// /join <team>?
		team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 0));
		if(team != null) {
			if(sender instanceof Player) {
				target = (Player) sender;
				self = true;
			}
			else {
				sender.sendMessage(i.t("team.onlyAsAPlayer"));
				return;
			}
		}
		else if(args.length >= 2) {
			// /join <player> <team>?
			team = p.getTeamManager().getTeam(UHUtils.getStringFromCommandArguments(args, 1));
			if(team != null) {
				target = p.getServer().getPlayer(args[0]);
				self = false;
				if(target == null) {
					sender.sendMessage(i.t("team.addplayer.disconnected", args[0], team.getName()));
					return;
				}
			}
		}
		
		if(team == null) {
			sender.sendMessage(i.t("team.addplayer.doesNotExists"));
		}
		else {
			if((self && sender.hasPermission("uh.player.join.self"))
					|| (!self && sender.hasPermission("uh.player.join.others"))) {
				team.addPlayer(target);
				
				if(!sender.equals(target)) {
					sender.sendMessage(i.t("team.addplayer.success", target.getName(), team.getName()));
				}
			}
			else {
				unauthorized(sender, command);
			}
		}
	}
	
	/**
	 * This command is used to allow a player to quit his team.
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 */
	private void doLeave(CommandSender sender, Command command, String label, String[] args) {
		
		Player target = null;
		
		if(args.length >= 1 && sender.hasPermission("uh.player.leave.others")) {
			if((target = p.getServer().getPlayer(args[0])) == null) {
				sender.sendMessage(i.t("team.removeplayer.disconnected", args[0]));
				return;
			}
		}
		else if(args.length == 0 && sender.hasPermission("uh.player.leave.self")) {
			if(sender instanceof Player) {
				target = (Player) sender;
			}
			else {
				sender.sendMessage(i.t("team.onlyAsAPlayer"));
				return;
			}
		}
		else {
			sender.sendMessage(i.t("cmd.errorUnauthorized"));
			return;
		}
		
		p.getTeamManager().removePlayerFromTeam(target);
		
		if(!target.equals(sender)) {
			sender.sendMessage(i.t("team.removeplayer.success", args[0]));
		}
	}
	
	
	
	/**
	 * Displays a separator around the output of the commands.
	 * <p>
	 * To be called before and after the output (prints a line only).
	 * 
	 * @param sender The line will be displayed for this sender.
	 */
	private void displaySeparator(CommandSender sender) {
		if(!(sender instanceof Player)) {
			return;
		}
		
		sender.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");
	}
	
	
	
	public ArrayList<String> getCommands() {
		return commands;
	}

	public ArrayList<String> getTeamCommands() {
		return teamCommands;
	}
	
	public ArrayList<String> getSpawnsCommands() {
		return spawnsCommands;
	}
	
	public ArrayList<String> getTPCommands() {
		return tpCommands;
	}
	
	public ArrayList<String> getTimersCommands() {
		return timersCommands;
	}
	
	public ArrayList<String> getSpecCommands() {
		return specCommands;
	}
	
	public ArrayList<String> getBorderCommands() {
		return borderCommands;
	}
	
	public ArrayList<String> getFreezeCommands() {
		return freezeCommands;
	}
}
