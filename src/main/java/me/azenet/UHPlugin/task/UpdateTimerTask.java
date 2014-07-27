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
		p.getGameManager().updateTimer();
		p.getGameManager().getScoreboardManager().updateScoreboard();
	}

}
