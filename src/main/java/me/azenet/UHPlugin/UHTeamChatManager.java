package me.azenet.UHPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.entity.Player;

public class UHTeamChatManager {
	
	UHPlugin p = null;
	I18n i = null;
	
	List<UUID> teamChatLocked = new ArrayList<UUID>();
	
	public UHTeamChatManager(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	/**
	 * Sends a team-message from the given player.
	 * 
	 * @param sender The sender of this message.
	 * @param message The message to be sent.
	 */
	public void sendTeamMessage(Player sender, String message) {
		UHTeam team = p.getTeamManager().getTeamForPlayer((Player) sender);
		
		if(team == null) {
			sender.sendMessage(i.t("team.message.noTeam"));
			return;
		}
		
		for(final Player player : team.getPlayers()) {
			player.sendMessage(i.t("team.message.format", ((Player) player).getDisplayName(), message));
		}
	}
	
	/**
	 * Sends a global message from the given player.
	 * 
	 * @param sender The sender of this message.
	 * @param message The message to be sent.
	 */
	public void sendGlobalMessage(Player sender, String message) {
		sender.chat(message);
	}
	
	/**
	 * Toggles the chat between the global chat and the team chat.
	 * 
	 * @param player The chat of this player will be toggled.
	 * @return true if the chat is now the team chat; false else.
	 */
	public boolean toggleChatForPlayer(Player player) {
		if(teamChatLocked.contains(player.getUniqueId())) {
			teamChatLocked.remove(player.getUniqueId());
			return false;
		}
		else {
			teamChatLocked.add(player.getUniqueId());
			return true;
		}
	}
	
	/**
	 * Returns true if the team chat is enabled for the given player.
	 * 
	 * @param player The player.
	 * @return
	 */
	public boolean isTeamChatEnabled(Player player) {
		return teamChatLocked.contains(player.getUniqueId());
	}
}
