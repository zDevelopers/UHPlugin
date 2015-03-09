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

import junit.framework.Assert;
import me.azenet.UHPlugin.TestsUtils;
import me.azenet.UHPlugin.commands.core.AbstractCommandExecutor;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.commands.UHCommand;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;


public class TestCommandsAnnotation {

	@Test
	public void testCommandAnnotationIsPresentEverywhere() {

		// Data
		Set<AbstractCommandExecutor> executors = new HashSet<>();
		executors.add(new UHCommandExecutor(TestsUtils.getMookedPluginInstance()));


		// Tests
		for(AbstractCommandExecutor executor : executors) {
			for(UHCommand command : executor.getMainCommands().values()) {
				testCommandAnnotationIsPresentEverywhere(command);
			}
		}
	}

	/**
	 * Recursively tests if the command annotation is present on every registered class.
	 *
	 * @param command The command to start with.
	 */
	private void testCommandAnnotationIsPresentEverywhere(UHCommand command) {
		Assert.assertTrue("Missing command annotation for the class " + command.getClass().getCanonicalName(), command.getClass().isAnnotationPresent(Command.class));

		for(UHCommand subCommand : command.getSubcommands().values()) {
			testCommandAnnotationIsPresentEverywhere(subCommand);
		}
	}
}
