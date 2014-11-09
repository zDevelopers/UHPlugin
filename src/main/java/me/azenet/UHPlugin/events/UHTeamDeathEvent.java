package me.azenet.UHPlugin.events;

import me.azenet.UHPlugin.UHTeam;

/**
 * Event fired when the last member of a team die.
 */
public class UHTeamDeathEvent extends UHEvent {
	
	private UHTeam team;
	
	public UHTeamDeathEvent(UHTeam team) {
		this.team = team;
	}
	
	/**
	 * Returns the now-dead team.
	 * 
	 * @return The team.
	 */
	public UHTeam getTeam() {
		return team;
	}
}
