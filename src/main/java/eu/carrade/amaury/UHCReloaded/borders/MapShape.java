/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
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
package eu.carrade.amaury.UHCReloaded.borders;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.borders.generators.CircularWallGenerator;
import eu.carrade.amaury.UHCReloaded.borders.generators.SquaredWallGenerator;
import eu.carrade.amaury.UHCReloaded.borders.generators.WallGenerator;
import eu.carrade.amaury.UHCReloaded.borders.shapes.CircularMapShape;
import eu.carrade.amaury.UHCReloaded.borders.shapes.MapShapeDescriptor;
import eu.carrade.amaury.UHCReloaded.borders.shapes.SquaredMapShape;
import org.bukkit.Material;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;


public enum MapShape
{

    CIRCULAR(new CircularMapShape(), CircularWallGenerator.class),
    SQUARED(new SquaredMapShape(), SquaredWallGenerator.class);


    private MapShapeDescriptor shape;
    private Class<? extends WallGenerator> generatorClass;

    /**
     * @param generator The wall generator class associated with this shape.
     */
    private MapShape(MapShapeDescriptor shape, Class<? extends WallGenerator> generator)
    {
        this.shape = shape;
        this.generatorClass = generator;
    }

    /**
     * Returns a new instance of the wall generator for this shape.
     *
     * @return The instance.
     */
    public WallGenerator getWallGeneratorInstance(UHCReloaded p, Material wallBlockAir, Material wallBlockSolid)
    {

        try
        {
            Constructor constructor = generatorClass.getConstructor(UHCReloaded.class, Material.class, Material.class);
            return (WallGenerator) constructor.newInstance(p, wallBlockAir, wallBlockSolid);

        }
        catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            p.getLogger().log(Level.SEVERE, "Cannot instantiate the walls generator: invalid class.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the shape descriptor.
     *
     * @return The shape.
     */
    public MapShapeDescriptor getShape()
    {
        return shape;
    }

    /**
     * Returns a shape based on his name.
     *
     * <p>Not case sensitive.</p>
     *
     * @param name The name.
     * @return The MapShape, or {@code null} if not found.
     */
    public static MapShape fromString(String name)
    {
        try
        {
            return MapShape.valueOf(name.trim().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
