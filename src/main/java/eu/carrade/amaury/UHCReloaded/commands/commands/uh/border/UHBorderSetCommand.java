/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh.border;

import eu.carrade.amaury.UHCReloaded.borders.MapShape;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@Command(name = "set")
public class UHBorderSetCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHBorderSetCommand(UHCReloaded p) {
		this.p = p;
		this.i = p.getI18n();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0) { // /uh border set
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.BAD_USE, this);
		}
		else if(args.length == 1) { // /uh border set <?>
			try {
				Integer newDiameter = Integer.valueOf(args[0]);

				if(p.getBorderManager().getPlayersOutside(newDiameter).size() != 0) { // Some players are outside
					sender.sendMessage(i.t("borders.set.playersOutsideCanceled"));
					sender.sendMessage(i.t("borders.set.playersOutsideCanceledCmd", args[0]));
					if(!p.getWorldBorderIntegration().isWBIntegrationEnabled()) {
						sender.sendMessage(i.t("borders.set.playersOutsideCanceledWarnWorldBorder"));
					}
					p.getBorderManager().sendCheckMessage(sender, newDiameter);
				}
				else {
					p.getBorderManager().setCurrentBorderDiameter(newDiameter);

					if(p.getBorderManager().getMapShape() == MapShape.CIRCULAR) {
						p.getServer().broadcastMessage(i.t("borders.set.broadcastCircular", args[0]));
					}
					else {
						p.getServer().broadcastMessage(i.t("borders.set.broadcastSquared", args[0]));
					}
				}

			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("borders.NaN", args[0]));
			}
		}
		else if(args.length == 2 && args[1].equalsIgnoreCase("force")) { // /uh border set <?> force
			try {
				Integer newDiameter = Integer.valueOf(args[0]);

				p.getBorderManager().setCurrentBorderDiameter(newDiameter);

				if(p.getBorderManager().getMapShape() == MapShape.CIRCULAR) {
					p.getServer().broadcastMessage(i.t("borders.set.broadcastCircular", args[0]));
				}
				else {
					p.getServer().broadcastMessage(i.t("borders.set.broadcastSquared", args[0]));
				}

			} catch(NumberFormatException e) {
				sender.sendMessage(i.t("borders.NaN", args[0]));
			}
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if(args.length == 2) {
			return CommandUtils.getAutocompleteSuggestions(args[1], Arrays.asList("force"));
		}

		else return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.borderHelpSet"));
	}
}
