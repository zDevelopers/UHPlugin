package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class UHTabCompleter implements TabCompleter {
	
	private UHPlugin p = null;
	
	private ArrayList<String> commands = null;
	private ArrayList<String> teamCommands = null;
	private ArrayList<String> specCommands = null;
	private ArrayList<String> borderCommands = null;
	private ArrayList<String> freezeCommands = null;

	private ArrayList<String> colors = new ArrayList<String>();
	
	
	public UHTabCompleter(UHPlugin plugin) {
		this.p = plugin;
		
		this.commands = p.getCommandManager().getCommands();
		this.teamCommands = p.getCommandManager().getTeamCommands();
		this.specCommands = p.getCommandManager().getSpecCommands();
		this.borderCommands = p.getCommandManager().getBorderCommands();
		this.freezeCommands = p.getCommandManager().getFreezeCommands();
		
		this.colors.add("aqua");
		this.colors.add("black");
		this.colors.add("blue");
		this.colors.add("darkaqua");
		this.colors.add("darkblue");
		this.colors.add("darkgray");
		this.colors.add("darkgreen");
		this.colors.add("darkpurple");
		this.colors.add("darkred");
		this.colors.add("gold");
		this.colors.add("gray");
		this.colors.add("green");
		this.colors.add("lightpurple");
		this.colors.add("red");
		this.colors.add("white");
		this.colors.add("yellow");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!command.getName().equalsIgnoreCase("uh")) {
			return null;
		}
		
		/** Autocompletion for subcommands **/
		if(args.length == 1) {
			return getAutocompleteSuggestions(args[0].toLowerCase(), this.commands);
		}
		
		/** Autocompletion for /uh team **/
		// The player names autocomplete is handled by Bukkit.
		if(args[0].equalsIgnoreCase("team")) {
			
			// /uh team <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1].toLowerCase(), this.teamCommands);
			}
			
			// /uh team subcommand <?>
			else if(args.length == 3) {
				
				if(args[1].equalsIgnoreCase("add")) { // Autocompletion for colors
					return getAutocompleteSuggestions(args[2].toLowerCase(), this.colors);
				}
				else if(args[1].equalsIgnoreCase("remove")) { // Autocompletion for teams names
					ArrayList<String> teamNames = new ArrayList<String>();
					for(UHTeam team : this.p.getTeamManager().getTeams()) {
						teamNames.add(team.getName());
					}
					return getAutocompleteSuggestions(args[2].toLowerCase(), teamNames);
				}	
			}
			
			else if(args.length == 4) {
				if(args[1].equalsIgnoreCase("addplayer")) { // Autocompletion for team names
					ArrayList<String> teamNames = new ArrayList<String>();
					for(UHTeam team : this.p.getTeamManager().getTeams()) {
						teamNames.add(team.getName());
					}
					return getAutocompleteSuggestions(args[3].toLowerCase(), teamNames);
				}
			}
		}
		
		/** Autocompletion for /uh spec **/
		else if(args[0].equalsIgnoreCase("spec")) {
			
			// /uh spec <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1].toLowerCase(), this.specCommands);
			}
			
			if(args.length == 3) {
				
				if(args[1].equalsIgnoreCase("remove")) { // /uh spec remove <?>: autocompletion for spectators only (not all players)
					ArrayList<String> spectatorsList = new ArrayList<String>();
					for(String spectator : p.getGameManager().getSpectators()) {
						spectatorsList.add(spectator);
					}
					return getAutocompleteSuggestions(args[2].toLowerCase(), spectatorsList);
				}
				
			}
		}
		
		/** Autocompletion for /uh start **/
		else if(args[0].equalsIgnoreCase("start")) {
			ArrayList<String> commandSuggested = new ArrayList<String>();
			
			if(args.length == 2) { // /uh start <?>
				commandSuggested.add("slow");
				return getAutocompleteSuggestions(args[1].toLowerCase(), commandSuggested);
			}
			
			else if(args.length == 3 && args[1].equalsIgnoreCase("slow")) { // /uh start slow <?>
				commandSuggested.add("go");
				return getAutocompleteSuggestions(args[2].toLowerCase(), commandSuggested);
			}
		}
		
		/** Autocompletion for /uh tpback **/
		else if(args[0].equalsIgnoreCase("tpback")) {
			if(args.length == 3) { // /uh tpback <player> <?=force>
				ArrayList<String> tpBackSuggest = new ArrayList<String>();
				tpBackSuggest.add("force");
				return getAutocompleteSuggestions(args[2].toLowerCase(), tpBackSuggest);
			}
		}
		
		/** Autocompletion for /uh freeze **/
		else if(args[0].equalsIgnoreCase("freeze")) {
			
			// /uh freeze <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1].toLowerCase(), this.freezeCommands);
			}
			
		}
		
		/** Autocompletion for /uh border **/
		else if(args[0].equalsIgnoreCase("border")) {
			
			// /uh border <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1].toLowerCase(), this.borderCommands);
			}
			
			else if(args[1].equalsIgnoreCase("warning") && args.length == 3) { // /uh border warning <?=cancel>
				ArrayList<String> commandSuggested = new ArrayList<String>();
				commandSuggested.add("cancel");
				return getAutocompleteSuggestions(args[2].toLowerCase(), commandSuggested);
			}
			
			else if(args[1].equalsIgnoreCase("set") && args.length == 4) { // /uh border set <diameter> <?=force>
				ArrayList<String> commandSuggested = new ArrayList<String>();
				commandSuggested.add("force");
				return getAutocompleteSuggestions(args[3].toLowerCase(), commandSuggested);
			}
			
		}
		
		return null;
	}
	
	private List<String> getAutocompleteSuggestions(String typed, List<String> commandsList) {
		List<String> list = new ArrayList<String>();
		
		for(String c : commandsList) {
			if(c.startsWith(typed)) {
				list.add(c);
			}
		}
		
		return list;
	}

}
