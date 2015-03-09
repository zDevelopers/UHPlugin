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

package me.azenet.UHPlugin.commands.commands;


import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.commands.uh.team.UHTeamJoinCommand;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


// The permissions are too complex, we need to manage them manually.
@Command(name = "join", noPermission = true, inheritPermission = false)
public class JoinCommand extends UHTeamJoinCommand {

	UHPlugin p;

	public JoinCommand(UHPlugin plugin) {

		super(plugin);
		p = plugin;

	}

	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		if(args.length == 0 && sender instanceof Player
				&& sender.hasPermission("uh.player.join.self")) {

			p.getTeamManager().displayTeamChooserChatGUI((Player) sender);
		}

		else  {
			super.run(sender, args);
		}
	}
}
