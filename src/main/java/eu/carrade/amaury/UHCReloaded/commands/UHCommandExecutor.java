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
package eu.carrade.amaury.UHCReloaded.commands;

import eu.carrade.amaury.UHCReloaded.commands.commands.GlobalMessageCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.TeamMessageCommand;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.*;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommandExecutor;


public class UHCommandExecutor extends AbstractCommandExecutor {

	public UHCommandExecutor(UHCReloaded p) {

		super(p);


		registerCommand(new UHRootCommand(p));          //  /uh

		registerCommand(new JoinCommand(p));            //  /join
		registerCommand(new LeaveCommand(p));           //  /leave

		registerCommand(new TeamMessageCommand(p));     //  /t
		registerCommand(new GlobalMessageCommand(p));   //  /g
		registerCommand(new ToggleChatCommand(p));      //  /togglechat

	}
}
