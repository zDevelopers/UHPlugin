package me.azenet.UHPlugin;

import org.apache.commons.lang.Validate;


/**
 * Represents a timer.
 * 
 * @author Amaury Carrade
 */
public class UHTimer {

	private String name = null;
	private Boolean registered = false;
	private Boolean running = false;

	private Long startTime = 0l;
	private Integer duration = 0; // seconds

	// Cached values
	private Integer hoursLeft = 0;
	private Integer minutesLeft = 0;
	private Integer secondsLeft = 0;

	// Pause
	private Boolean paused = false;
	private Long pauseTime = 0l;

	// Display this timer following the format "hh:mm:ss"?
	private Boolean displayHoursInTimer = false;


	public UHTimer(String name) {
		Validate.notNull(name, "The name cannot be null");
		
		this.name = name;
	}

	/**
	 * Sets the duration of the timer, in seconds.
	 * 
	 * @param seconds The duration.
	 */
	public void setDuration(int seconds) {
		this.duration = seconds;
	}

	/**
	 * Starts this timer.
	 * 
	 * If this is called while the timer is running, the timer is restarted.
	 */
	public void start() {
		this.running = true;
		this.startTime = System.currentTimeMillis();
		
		this.hoursLeft   = (int) Math.floor(this.duration / 3600);
		this.minutesLeft = (int) (Math.floor(this.duration / 60) - (this.hoursLeft * 60));
		this.secondsLeft = this.duration - (this.minutesLeft * 60 + this.hoursLeft * 3600);
		
		// Lower than 100 because else the counter text is longer than 16 characters.
		this.displayHoursInTimer = (this.hoursLeft != 0 && this.hoursLeft < 100);
	}

	/**
	 * Stops this timer.
	 */
	public void stop() {
		this.running = false;
		this.startTime = 0l;
		
		this.hoursLeft   = 0;
		this.minutesLeft = 0;
		this.secondsLeft = 0;
	}

	/**
	 * Updates the timer.
	 */
	public void update() {
		long timeSinceStart = System.currentTimeMillis() - this.startTime;
		long diffSeconds = timeSinceStart / 1000 % 60;
		long diffMinutes = timeSinceStart / (60 * 1000) % 60;
		
		double durationInMinutes = Math.floor(this.duration / 60);
		
		if(diffMinutes >= durationInMinutes) {
			stop();
		}
		else {
			if(displayHoursInTimer) {
				int rawMinutesLeft = (int) ((durationInMinutes - diffMinutes) - 1);
				hoursLeft   = (int) Math.floor(rawMinutesLeft / 60);
				minutesLeft = (int) rawMinutesLeft - (60 * hoursLeft);
				secondsLeft = (int) (60 - diffSeconds) - 1;
			}
			else {
				minutesLeft = (int) (durationInMinutes - diffMinutes) - 1;
				secondsLeft = (int) (60 - diffSeconds) - 1;
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
	 * Checks if the timer is currently running.
	 * 
	 * @return true if the timer is running.
	 */
	public Boolean isRunning() {
		return running;
	}

	/**
	 * Returns the duration if the timer, in seconds.
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
}
