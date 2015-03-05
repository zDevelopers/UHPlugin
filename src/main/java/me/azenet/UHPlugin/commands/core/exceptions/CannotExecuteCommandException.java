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
package me.azenet.UHPlugin.commands.core.exceptions;

import me.azenet.UHPlugin.commands.core.commands.UHCommand;


/**
 * This exception is fired when a command cannot be executed, for whatever reason.
 *
 * @version 1.0
 * @author Amaury Carrade
 */
public class CannotExecuteCommandException extends Exception {

	public enum Reason {
		NOT_ALLOWED,
		ONLY_AS_A_PLAYER,
		BAD_USE,
		UNKNOWN
	}

	private Reason reason;
	private UHCommand origin;

	public CannotExecuteCommandException(Reason reason, UHCommand origin) {
		this.reason = reason;
		this.origin = origin;
	}

	public CannotExecuteCommandException(Reason reason) {
		this(reason, null);
	}

	public Reason getReason() {
		return reason;
	}

	public UHCommand getOrigin() {
		return origin;
	}
}
