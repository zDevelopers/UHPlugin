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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
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
			return getAutocompleteSuggestions(args[0], this.commands);
		}
		
		/** Autocompletion for /uh team **/
		// The player names autocomplete is handled by Bukkit.
		if(args[0].equalsIgnoreCase("team")) {
			
			// /uh team <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], this.teamCommands);
			}
			
			// /uh team subcommand <?>
			else if(args.length == 3) {
				
				if(args[1].equalsIgnoreCase("add")) { // Autocompletion for colors
					return getAutocompleteSuggestions(args[2], this.colors);
				}
				else if(args[1].equalsIgnoreCase("remove")) { // Autocompletion for teams names
					ArrayList<String> teamNames = new ArrayList<String>();
					for(UHTeam team : this.p.getTeamManager().getTeams()) {
						teamNames.add(team.getName());
					}
					return getAutocompleteSuggestions(args[2], teamNames);
				}	
			}
			
			else if(args.length >= 4) { // Autocompletion for team names – multiple words autocompletion
				if(args[1].equalsIgnoreCase("addplayer") || args[1].equalsIgnoreCase("remove")) {
					ArrayList<String> teamNames = new ArrayList<String>();
					for(UHTeam team : this.p.getTeamManager().getTeams()) {
						teamNames.add(team.getName());
					}
					if(args[1].equalsIgnoreCase("addplayer")) { // /uh team addplayer Sth <?>
						return getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 3), teamNames, args.length - 4);
					}
					else if(args[1].equalsIgnoreCase("remove")) { // /uh team remove <?>
						return getAutocompleteSuggestions(UHUtils.getStringFromCommandArguments(args, 2), teamNames, args.length - 3);
					}
					//return getAutocompleteSuggestions(args[3], teamNames);
				}
			}
		}
		
		/** Autocompletion for /uh spec **/
		else if(args[0].equalsIgnoreCase("spec")) {
			
			// /uh spec <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], this.specCommands);
			}
			
			if(args.length == 3) {
				
				if(args[1].equalsIgnoreCase("remove")) { // /uh spec remove <?>: autocompletion for spectators only (not all players)
					ArrayList<String> spectatorsList = new ArrayList<String>();
					for(String spectator : p.getGameManager().getSpectators()) {
						spectatorsList.add(spectator);
					}
					return getAutocompleteSuggestions(args[2], spectatorsList);
				}
				
			}
		}
		
		/** Autocompletion for /uh start **/
		else if(args[0].equalsIgnoreCase("start")) {
			ArrayList<String> commandSuggested = new ArrayList<String>();
			
			if(args.length == 2) { // /uh start <?>
				commandSuggested.add("slow");
				return getAutocompleteSuggestions(args[1], commandSuggested);
			}
			
			else if(args.length == 3 && args[1].equalsIgnoreCase("slow")) { // /uh start slow <?>
				commandSuggested.add("go");
				return getAutocompleteSuggestions(args[2], commandSuggested);
			}
		}
		
		/** Autocompletion for /uh tpback **/
		else if(args[0].equalsIgnoreCase("tpback")) {
			if(args.length == 3) { // /uh tpback <player> <?=force>
				ArrayList<String> tpBackSuggest = new ArrayList<String>();
				tpBackSuggest.add("force");
				return getAutocompleteSuggestions(args[2], tpBackSuggest);
			}
		}
		
		/** Autocompletion for /uh freeze **/
		else if(args[0].equalsIgnoreCase("freeze")) {
			
			// /uh freeze <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], this.freezeCommands);
			}
			
		}
		
		/** Autocompletion for /uh border **/
		else if(args[0].equalsIgnoreCase("border")) {
			
			// /uh border <?>
			if(args.length == 2) {
				return getAutocompleteSuggestions(args[1], this.borderCommands);
			}
			
			else if(args[1].equalsIgnoreCase("warning") && args.length == 3) { // /uh border warning <?=cancel>
				ArrayList<String> commandSuggested = new ArrayList<String>();
				commandSuggested.add("cancel");
				return getAutocompleteSuggestions(args[2], commandSuggested);
			}
			
			else if(args[1].equalsIgnoreCase("set") && args.length == 4) { // /uh border set <diameter> <?=force>
				ArrayList<String> commandSuggested = new ArrayList<String>();
				commandSuggested.add("force");
				return getAutocompleteSuggestions(args[3], commandSuggested);
			}
			
		}
		
		return null;
	}
	
	
	/**
	 * Returns a list of autocompletion suggestions based on what the user typed and on a list of
	 * available commands.
	 * 
	 * @param typed What the user typed. This string needs to include <em>all</em> the words typed.
	 * @param suggestionsList The list of the suggestions.
	 * @param numberOfWordsToIgnore If non-zero, this number of words will be ignored at the beginning of the string. This is used to handle multiple-words autocompletion.
	 * 
	 * @return The list of matching suggestions.
	 */
	private List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList, int numberOfWordsToIgnore) {
		List<String> list = new ArrayList<String>();
		
		// For each suggestion:
		//  - if there isn't any world to ignore, we just compare them;
		//  - else, we removes the correct number of words at the beginning of the string;
		//    then, if the raw suggestion matches the typed text, we adds to the suggestion list
		//    the filtered suggestion, because the Bukkit's autocompleter works on a “per-word” basis.
		
		for(String rawSuggestion : suggestionsList) {			
			String suggestion = "";
			
			if(numberOfWordsToIgnore == 0) {
				suggestion = rawSuggestion;
			}
			else {
				// Not the primary use, but, hey! It works.
				suggestion = UHUtils.getStringFromCommandArguments(rawSuggestion.split(" "), numberOfWordsToIgnore);
			}
			
			if(rawSuggestion.toLowerCase().startsWith(typed.toLowerCase())) {
				list.add(suggestion);
			}
		}
		
		Collections.sort(list, Collator.getInstance());
		
		return list;
	}
	
	/**
	 * Returns a list of autocompletion suggestions based on what the user typed and on a list of
	 * available commands.
	 * 
	 * @param typed What the user typed.
	 * @param suggestionsList The list of the suggestions.
	 * 
	 * @return The list of matching suggestions.
	 */
	private List<String> getAutocompleteSuggestions(String typed, List<String> suggestionsList) {
		return getAutocompleteSuggestions(typed, suggestionsList, 0);
	}

}
