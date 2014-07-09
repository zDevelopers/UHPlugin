package me.azenet.UHPlugin.task;

import me.azenet.UHPlugin.UHPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateTimerTask extends BukkitRunnable {

	private UHPlugin p = null;
	
	public UpdateTimerTask(UHPlugin p) {
		this.p = p;
	}
	
	@Override
	public void run() {
		// TODO sync the timer with a clock to avoid “long” minutes due to lag.
		p.getGameManager().setMatchInfo();
		p.getGameManager().updateTimer();
	}

}
