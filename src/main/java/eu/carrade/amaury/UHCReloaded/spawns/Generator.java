/**
 * Plugin UltraHardcore Reloaded (UHPlugin)
 * Copyright (C) 2013 azenet
 * Copyright (C) 2014-2015 Amaury Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.UHCReloaded.spawns;


import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.spawns.generators.CircularSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.GridSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.RandomSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.SpawnPointsGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;


public enum Generator
{
	/**
	 * Spawn points generated randomly.
	 */
	RANDOM(RandomSpawnPointsGenerator.class),

	/**
	 * Spawn points generated following the shape of a grid,
	 * with concentric squares (starting from the largest square).
	 */
	GRID(GridSpawnPointsGenerator.class),

	/**
	 * Spawn points generated on concentric circles (starting from the
	 * largest circle).
	 */
	CIRCULAR(CircularSpawnPointsGenerator.class);


	private Class<? extends SpawnPointsGenerator> generatorClass;

	/**
	 * @param generatorClass The generator.
	 */
	Generator(Class<? extends SpawnPointsGenerator> generatorClass)
	{
		this.generatorClass = generatorClass;
	}

	/**
	 * Returns a generator based on his name.
	 *
	 * <p>Not case sensitive.</p>
	 *
	 * @param name The name.
	 * @return The Generator, or null if not found.
	 */
	public static Generator fromString(String name)
	{
		try
		{
			return Generator.valueOf(name.trim().toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	/**
	 * Returns a new instance of the generator.
	 *
	 * @return The instance.
	 */
	public SpawnPointsGenerator getInstance(UHCReloaded p)
	{
		try
		{
			Constructor constructor = generatorClass.getConstructor(UHCReloaded.class);
			return (SpawnPointsGenerator) constructor.newInstance(p);

		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException e)
		{
			p.getLogger().log(Level.SEVERE, "Cannot instantiate the spawn points generator: invalid class (missing constructor?): " + generatorClass.getName());
			e.printStackTrace();

			return null;
		}
		catch (InvocationTargetException e)
		{
			p.getLogger().log(Level.SEVERE, "Error during the spawn points generator instantiation: " + generatorClass.getName());
			e.getCause().printStackTrace();

			return null;
		}
	}
}
