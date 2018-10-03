/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package eu.carrade.amaury.UHCReloaded.spawns;


import eu.carrade.amaury.UHCReloaded.spawns.generators.CircularSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.GridSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.RandomSpawnPointsGenerator;
import eu.carrade.amaury.UHCReloaded.spawns.generators.SpawnPointsGenerator;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;

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
    public SpawnPointsGenerator getInstance()
    {
        try
        {
            return Reflection.instantiate(generatorClass);
        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException e)
        {
            PluginLogger.log(Level.SEVERE, "Cannot instantiate the spawn points generator: invalid class (missing constructor?): " + generatorClass.getName(), e.getCause());
            return null;
        }
        catch (InvocationTargetException e)
        {
            PluginLogger.log(Level.SEVERE, "Error during the spawn points generator instantiation: " + generatorClass.getName(), e.getCause());
            return null;
        }
    }
}
