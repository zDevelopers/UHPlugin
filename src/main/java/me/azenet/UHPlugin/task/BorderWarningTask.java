package me.azenet.UHPlugin.task;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.i18n.I18n;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderWarningTask extends BukkitRunnable {

	private UHPlugin p = null;
	private I18n i = null;
	
	public BorderWarningTask(UHPlugin p) {
		this.p = p;
		this.i = p.getI18n();
	}
	
	@Override
	public void run() {
		
		for(Player player : p.getBorderManager().getPlayersOutside(p.getBorderManager().getWarningSize())) {
			int distance = p.getBorderManager().getDistanceToBorder(player.getLocation(), p.getBorderManager().getWarningSize());
			
			if(p.getBorderManager().isCircularBorder()) {
				player.sendMessage(i.t("borders.warning.messageCircular", String.valueOf(p.getBorderManager().getWarningSize())));
			}
			else {
				player.sendMessage(i.t("borders.warning.messageSquared", String.valueOf(p.getBorderManager().getWarningSize())));
			}
			player.sendMessage(i.t("borders.warning.messageDistance", String.valueOf(distance)));
		}
		
	}

}
