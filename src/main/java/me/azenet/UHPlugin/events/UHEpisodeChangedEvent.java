package me.azenet.UHPlugin.events;

/**
 * Called when an episode changes.
 */
public class UHEpisodeChangedEvent extends UHEvent {
	
	private int newEpisode;
	private EpisodeChangedCause cause;
	private String shifter;
	
	public UHEpisodeChangedEvent(int newEpisode, EpisodeChangedCause cause, String shifter) {
		this.newEpisode = newEpisode;
		this.cause = cause;
		this.shifter = shifter;
	}
	
	/**
	 * Returns the new episode.
	 * 
	 * @return The new episode.
	 */
	public int getNewEpisode() {
		return newEpisode;
	}
	
	/**
	 * Why the episode changed?
	 * 
	 * @return The cause.
	 * 
	 * @see EpisodeChangedCause
	 */
	public EpisodeChangedCause getCause() {
		return cause;
	}
	
	/**
	 * Returns the name of the shifter (the one that executed the /uh shift command, or "" if
	 * the episode was shifted because the previous one was finished).
	 * 
	 * @return The shifter.
	 */
	public String getShifter() {
		return shifter;
	}
}
