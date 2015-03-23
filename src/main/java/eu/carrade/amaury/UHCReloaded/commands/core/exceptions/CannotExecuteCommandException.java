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
package eu.carrade.amaury.UHCReloaded.commands.core.exceptions;

import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;


/**
 * This exception is fired when a command cannot be executed, for whatever reason.
 *
 * @version 1.0
 * @author Amaury Carrade
 */
public class CannotExecuteCommandException extends Exception {

	public enum Reason {
		/**
		 * Use this if the player is not allowed to execute the command.
		 */
		NOT_ALLOWED,

		/**
		 * Use this if the command can only be executed as a player, and
		 * the sender is not a player.
		 */
		ONLY_AS_A_PLAYER,

		/**
		 * Use this if the sender used the command badly.
		 *
		 * <p>
		 *     This will display the documentation and an error message.
		 * </p>
		 */
		BAD_USE,

		/**
		 * Use this to have the documentation of the command displayed.
		 */
		NEED_DOC,

		/**
		 * Use this in other cases.
		 */
		UNKNOWN
	}

	private Reason reason;
	private AbstractCommand origin;

	public CannotExecuteCommandException(Reason reason, AbstractCommand origin) {
		this.reason = reason;
		this.origin = origin;
	}

	public CannotExecuteCommandException(Reason reason) {
		this(reason, null);
	}

	public Reason getReason() {
		return reason;
	}

	public AbstractCommand getOrigin() {
		return origin;
	}
}
