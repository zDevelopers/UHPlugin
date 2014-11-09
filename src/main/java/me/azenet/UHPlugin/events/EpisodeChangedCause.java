package me.azenet.UHPlugin.events;

public enum EpisodeChangedCause {
	/**
	 * The episode changed because the previous episode was finished.
	 */
	FINISHED,
	
	/**
	 * The episode changed because the previous episode was shifted by someone using
	 * the {@code /uh shift} command.
	 */
	SHIFTED;
};
