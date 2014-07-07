package me.azenet.UHPlugin;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.PlayerNamePrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class UHPrompts {

	private UHPlugin p = null;
	
	public UHPrompts(UHPlugin p) {
		this.p = p;
	}
	
	private class TeamNamePrompt extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.GRAY+"Veuillez entrer un nom pour la team. /cancel pour annuler.";
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.length() > 16) {
				context.getForWhom().sendRawMessage(ChatColor.RED+"Le nom de la team doit faire 16 caractères maximum.");
				return this;
			}
			context.setSessionData("nomTeam", input);
			return new TeamColorPrompt();
		}
		
	}
	
	private class TeamColorPrompt extends ValidatingPrompt {

		private ArrayList<String> colors;
		
		public TeamColorPrompt() {
			super();

			colors = new ArrayList<String>();
			colors.add(ChatColor.AQUA+"Aqua");
			colors.add(ChatColor.BLACK+"Black");
			colors.add(ChatColor.BLUE+"Blue");
			colors.add(ChatColor.DARK_AQUA+"Darkaqua");
			colors.add(ChatColor.DARK_BLUE+"Darkblue"); 
			colors.add(ChatColor.DARK_GRAY+"Darkgray");
			colors.add(ChatColor.DARK_GREEN+"Darkgreen");
			colors.add(ChatColor.DARK_PURPLE+"Darkpurple");
			colors.add(ChatColor.DARK_RED+"Darkred");
			colors.add(ChatColor.GOLD+"Gold"); 
			colors.add(ChatColor.GRAY+"Gray");
			colors.add(ChatColor.GREEN+"Green");
			colors.add(ChatColor.LIGHT_PURPLE+"Lightpurple");
			colors.add(ChatColor.RED+"Red");
			colors.add(ChatColor.WHITE+"White");
			colors.add(ChatColor.YELLOW+"Yellow");
		}
		
		@Override
		public String getPromptText(ConversationContext context) {
			String colorsString = "";
			for(String s : colors) {
				colorsString += s+ChatColor.WHITE+", ";
			}
			colorsString = colorsString.substring(0, colorsString.length()-2);
			return ChatColor.GRAY+"Veuillez entrer une couleur pour la team. /cancel pour annuler.\n"+colorsString;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context,
				String input) {
			context.setSessionData("color", StringToChatColor.getChatColorByName(ChatColor.stripColor(input)));
			p.createTeam((String) context.getSessionData("nomTeam"), (ChatColor) context.getSessionData("color"));
			context.getForWhom().sendRawMessage(ChatColor.GRAY+"Team "+((ChatColor)context.getSessionData("color"))+context.getSessionData("nomTeam")+ChatColor.GRAY+" créée.");
			return Prompt.END_OF_CONVERSATION;
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			for (String s : colors) {
				if (ChatColor.stripColor(s).equalsIgnoreCase(input)) return true;
			}
			return false;
		}
		
	}
	
	private class PlayerPrompt extends PlayerNamePrompt {

		public PlayerPrompt(Plugin plugin) {
			super(plugin);
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.GRAY+"Entrez le nom du joueur à ajouter dans la team "+((ChatColor)context.getSessionData("color"))+context.getSessionData("nomTeam")+ChatColor.WHITE+".";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context,
				Player input) {
			p.getTeam((String) context.getSessionData("nomTeam")).addPlayer(input);
			context.getForWhom().sendRawMessage(ChatColor.GREEN+input.getName()+ChatColor.DARK_GREEN+" a été ajouté à l'équipe "+((ChatColor)context.getSessionData("color"))+context.getSessionData("nomTeam")+".");
			return Prompt.END_OF_CONVERSATION;
		}
		
	}
	
	private enum StringToChatColor {
		AQUA("Aqua", ChatColor.AQUA),
		BLACK("Black", ChatColor.BLACK),
		BLUE("Blue", ChatColor.BLUE), 
		DARK_AQUA("Darkaqua", ChatColor.DARK_AQUA),
		DARK_BLUE("Darkblue", ChatColor.DARK_BLUE), 
		DARK_GRAY("Darkgray", ChatColor.DARK_GRAY),
		DARK_GREEN("Darkgreen", ChatColor.DARK_GREEN),
		DARK_PURPLE("Darkpurple", ChatColor.DARK_PURPLE), 
		DARK_RED("Darkred", ChatColor.DARK_RED),
		GOLD("Gold", ChatColor.GOLD),
		GRAY("Gray", ChatColor.GRAY),
		GREEN("Green", ChatColor.GREEN),
		LIGHT_PURPLE("Lightpurple", ChatColor.LIGHT_PURPLE),
		RED("Red", ChatColor.RED), 
		WHITE("White", ChatColor.WHITE),
		YELLOW("Yellow", ChatColor.YELLOW);
		
		private String name;
		private ChatColor color;
		
		StringToChatColor(String name, ChatColor color) {
			this.name = name;
			this.color = color;
		}
		
		public static ChatColor getChatColorByName(String name) {
			for(StringToChatColor stcc : values()) {
				if (stcc.name.equalsIgnoreCase(name)) return stcc.color;
			}
			return null;
		}
	}
	
	public TeamNamePrompt getTNP() {
		return new TeamNamePrompt();
	}
	
	public TeamColorPrompt getTCoP() {
		return new TeamColorPrompt();
	}
	
	public PlayerPrompt getPP() {
		return new PlayerPrompt(p);
	}
}
