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

package me.azenet.UHPlugin.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
	/**
	 * The name of the command, needed to type in the console/chat to execute
	 * the (sub-)command.
	 */
	public String name();

	/**
	 * The permission needed to execute this command.
	 *
	 * <p>
	 *     If the {@code inheritPermission} option is unset or set to <code>false</code>,
	 *     this permission is <strong>concatened to the parent permissions</strong>.<br />
	 *     As example, if the permission is set to {@code sb}, and if the parent command
	 *     have the permission {@code cmd.norris}, the real permission of the command will
	 *     be {@code cmd.norris.sb}.
	 * </p>
	 * <p>
	 *     If this is left empty, or not set, the permission of the parent command will be used.<br />
	 *     If the parent command is {@code null} (i.e. this command is a root one), the command will be
	 *     accessible to everyone.
	 * </p>
	 */
	public String permission() default "";

	/**
	 * If this is set to {@code false}, the permission will be interpreted <em>as-is</em>,
	 * without concatenation with the permissions of the parent commands.
	 *
	 * <p>
	 *     You should not set this to {@code false} if the command have sub-commands with this
	 *     set to {@code true}, or weired behavior may happens.
	 * </p>
	 */
	public boolean inheritPermission() default true;
}
