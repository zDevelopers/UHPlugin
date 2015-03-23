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

import junit.framework.Assert;
import eu.carrade.amaury.UHCReloaded.TestsUtils;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommandExecutor;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;


@RunWith(PowerMockRunner.class)
@PrepareForTest(UHCReloaded.class)
public class TestCommandsAnnotation {

	@Test
	public void testCommandAnnotationIsPresentEverywhere() {

		// Data
		Set<AbstractCommandExecutor> executors = new HashSet<>();
		executors.add(new UHCommandExecutor(TestsUtils.getMockedPluginInstance()));


		// Tests
		for(AbstractCommandExecutor executor : executors) {
			for(AbstractCommand command : executor.getMainCommands().values()) {
				testCommandAnnotationIsPresentEverywhere(command);
			}
		}
	}

	/**
	 * Recursively tests if the command annotation is present on every registered class.
	 *
	 * @param command The command to start with.
	 */
	private void testCommandAnnotationIsPresentEverywhere(AbstractCommand command) {
		Assert.assertTrue("Missing command annotation for the class " + command.getClass().getCanonicalName(), command.getClass().isAnnotationPresent(Command.class));

		for(AbstractCommand subCommand : command.getSubcommands().values()) {
			testCommandAnnotationIsPresentEverywhere(subCommand);
		}
	}
}
