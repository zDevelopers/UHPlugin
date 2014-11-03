package me.azenet.UHPlugin;

import java.util.UUID;

import me.azenet.UHPlugin.events.TimerEndsEvent;
import me.azenet.UHPlugin.events.TimerStartsEvent;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;


/**
 * Represents a timer.
 * 
 * @author Amaury Carrade
 */
public class UHTimer {

	private UUID id = null;
	private String name = null;
	private Boolean registered = false;
	private Boolean running = false;

	private Long startTime = 0l;
	private Integer duration = 0; // seconds

	// Cached values
	private Integer hoursLeft = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;
	
	// Old values, used by the scoreboard to reset the scores.
	private Integer oldHoursLeft = -1;
	private Integer oldMinutesLeft = -1;
	private Integer oldSecondsLeft = -1;
	
	// Pause
	private Boolean paused = false;
	private Long pauseTime = 0l;

	// Display this timer following the format "hh:mm:ss"?
	private Boolean displayHoursInTimer = false;


	public UHTimer(String name) {
		Validate.notNull(name, "The name cannot be null");
		
		this.id = UUID.randomUUID(); // only used as a hashCode.
		this.name = name;
	}

	/**
	 * Sets the duration of the timer, in seconds.
	 * 
	 * @param seconds The duration.
	 */
	public void setDuration(int seconds) {
		this.duration = seconds;
		
		this.hoursLeft   = (int) Math.floor(this.duration / 3600);
		this.minutesLeft = (int) (Math.floor(this.duration / 60) - (this.hoursLeft * 60));
		this.secondsLeft = this.duration - (this.minutesLeft * 60 + this.hoursLeft * 3600);
		
		// Lower than 100 because else the counter text is longer than 16 characters.
		this.displayHoursInTimer = (this.hoursLeft != 0 && this.hoursLeft < 100);
	}

	/**
	 * Starts this timer.
	 * 
	 * If this is called while the timer is running, the timer is restarted.
	 */
	public void start() {		
		this.running = true;
		this.startTime = System.currentTimeMillis();
		
		Bukkit.getServer().getPluginManager().callEvent(new TimerStartsEvent(this));
	}

	/**
	 * Stops this timer.
	 */
	public void stop() {
		stop(false);
	}
	
	/**
	 * Stops this timer.
	 * 
	 * @param ended If true, the timer was stopped because the timer was up.
	 */
	private void stop(boolean wasUp) {
		TimerEndsEvent event = new TimerEndsEvent(this, wasUp);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if(isRegistered()) {
			if(event.getRestart()) {
				start();
			}
			else {
				this.running = false;
				this.startTime = 0l;
				
				this.hoursLeft   = 0;
				this.minutesLeft = 0;
				this.secondsLeft = 0;
				
				this.oldHoursLeft   = 0;
				this.oldMinutesLeft = 0;
				this.oldSecondsLeft = 0;
			}
		}
	}

	/**
	 * Updates the timer.
	 */
	public void update() {
		if(running && !paused) {			
			oldHoursLeft   = hoursLeft;
			oldMinutesLeft = minutesLeft;
			oldSecondsLeft = secondsLeft;
			
			long timeSinceStart = System.currentTimeMillis() - this.startTime; // ms
			
			if(timeSinceStart >= getDuration() * 1000) {
				stop(true);
			}
			else {
				Integer countSecondsLeft = (int) (getDuration() - Math.floor(timeSinceStart / 1000));
				
				secondsLeft = countSecondsLeft % 60;
				minutesLeft = (countSecondsLeft % 3600) / 60;
				hoursLeft   = (int) Math.floor(countSecondsLeft / 3600);
			}
		}
	}

	/**
	 * Pauses (or restarts after a pause) the timer.
	 * <p>
	 * If the timer is not running, nothing is done.
	 * 
	 * @param pause If true the timer will be paused.
	 */
	public void setPaused(boolean pause) {
		if(this.running) {
			// The pause is only set once (as example if the user executes /uh freeze all twice).
			if(pause && !this.paused) {
				this.paused = true;
				this.pauseTime = System.currentTimeMillis();
			}
			
			if(!pause && this.paused) {
				// We have to add to the time of the start of the episode the elapsed time
				// during the pause.
				this.startTime += (System.currentTimeMillis() - this.pauseTime);
				this.pauseTime = 0l;

				this.paused = false;
			}
		}
	}

	/**
	 * Checks if the timer is registered in the TimerManager.
	 * 
	 * @return true if the timer is registered.
	 */
	public Boolean isRegistered() {
		return registered;
	}

	/**
	 * Marks a timer as registered, or not.
	 * 
	 * @param registered true if the timer is now registered.
	 */
	protected void setRegistered(Boolean registered) {
		this.registered = registered;
	}

	/**
	 * Returns the name of the timer.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the display name of the timer.
	 * <p>
	 * The display name is the name with all &-based color codes replaced by §-based ones.
	 * 
	 * @return The name.
	 */
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&', name);
	}


	/**
	 * Checks if the timer is currently running.
	 * 
	 * @return true if the timer is running.
	 */
	public Boolean isRunning() {
		return running;
	}

	/**
	 * Returns the duration of the timer, in seconds.
	 * 
	 * @return The duration.
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Returns the number of hours left until the end of this countdown.
	 * 
	 * @return The number of hours left.
	 */
	public Integer getHoursLeft() {
		return hoursLeft;
	}

	/**
	 * Returns the number of minutes left until the end of this countdown.
	 * 
	 * @return The number of minutes left.
	 */
	public Integer getMinutesLeft() {
		return minutesLeft;
	}

	/**
	 * Returns the number of seconds left until the end of this countdown.
	 * 
	 * @return The number of seconds left.
	 */
	public Integer getSecondsLeft() {
		return secondsLeft;
	}

	/**
	 * Returns the number of hours left until the end of this countdown, before the last update.
	 * <p>
	 * Used by the scoreboard, to remove the old score.
	 * 
	 * @return The old number of hours left, or -1 if the timer was never updated.
	 */
	public Integer getOldHoursLeft() {
		return oldHoursLeft;
	}

	/**
	 * Returns the number of minutes left until the end of this countdown, before the last update.
	 * <p>
	 * Used by the scoreboard, to remove the old score.
	 * 
	 * @return The old number of minutes left, or -1 if the timer was never updated.
	 */
	public Integer getOldMinutesLeft() {
		return oldMinutesLeft;
	}

	/**
	 * Returns the number of seconds left until the end of this countdown, before the last update.
	 * <p>
	 * Used by the scoreboard, to remove the old score.
	 * 
	 * @return The old number of seconds left, or -1 if the timer was never updated.
	 */
	public Integer getOldSecondsLeft() {
		return oldSecondsLeft;
	}

	/**
	 * Checks if this timer is paused.
	 * 
	 * @return true if the timer is paused.
	 */
	public Boolean isPaused() {
		return paused;
	}

	/**
	 * Returns true if this timer is displayed as "hh:mm:ss" in the scoreboard.
	 * 
	 * @return true if this timer is displayed as "hh:mm:ss" in the scoreboard.
	 */
	public Boolean getDisplayHoursInTimer() {
		return displayHoursInTimer;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof UHTimer)) {
			return false;
		}
		
		return ((UHTimer) other).getName().equals(this.getName());
	}
	
	@Override
	public String toString() {
		return "UHTimer ["
				+ (name != null ? "name=" + name + ", " : "")
				+ (registered != null ? "registered=" + registered + ", " : "")
				+ (isRunning() ? "running, " : "")
				+ (startTime != null ? "startTime=" + startTime + ", " : "")
				+ (duration != null ? "duration=" + duration + ", " : "")
				+ (isRunning() ? "currentTime=" + getHoursLeft() + "h" + getMinutesLeft() + "m" + getSecondsLeft() + "s" : "")
				+ (isRunning() && paused != null ? "paused=" + paused + ", " : "")
				+ (isRunning() && paused && pauseTime != null ? "pauseTime=" + pauseTime : "") + "]";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
