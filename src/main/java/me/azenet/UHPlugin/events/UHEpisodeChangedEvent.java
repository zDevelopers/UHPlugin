package me.azenet.UHPlugin.events;

/**
 * Called when an episode changes.
 */
public class UHEpisodeChangedEvent extends UHEvent {
	
	private int newEpisode;
	private EpisodeChangedCause cause;
	
	public UHEpisodeChangedEvent(int newEpisode, EpisodeChangedCause cause) {
		this.newEpisode = newEpisode;
		this.cause = cause;
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
}
