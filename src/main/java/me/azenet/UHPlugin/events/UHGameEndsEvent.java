package me.azenet.UHPlugin.events;

import me.azenet.UHPlugin.UHTeam;

public class UHGameEndsEvent extends UHEvent {
	private UHTeam winner;
	
	public UHGameEndsEvent(UHTeam winner) {
		this.winner = winner;
	}
	
	/**
	 * Returns the last team alive.
	 * 
	 * @return The team.
	 */
	public UHTeam getWinnerTeam() {
		return winner;
	}
}
