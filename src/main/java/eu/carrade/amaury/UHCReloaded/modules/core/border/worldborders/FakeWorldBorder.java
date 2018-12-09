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
package eu.carrade.amaury.UHCReloaded.modules.core.border.worldborders;

import eu.carrade.amaury.UHCReloaded.modules.core.border.MapShape;
import org.bukkit.Location;
import org.bukkit.World;


/**
 * A fake world border, it does nothing.
 *
 * <p>Used when a circular world border is requested without the WorldBorder plugin.</p>
 */
public class FakeWorldBorder extends WorldBorder
{
    private final World world;

    private Location center;
    private Double diameter;
    private MapShape shape;

    public FakeWorldBorder(final World world)
    {
        this.world = world;
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Override
    public double getDiameter()
    {
        return diameter;
    }

    @Override
    public void setDiameter(final double diameter)
    {
        this.diameter = diameter;
    }

    @Override
    public void setDiameter(final double diameter, final long time)
    {
        this.diameter = diameter;
    }

    @Override
    public Location getCenter()
    {
        return center;
    }

    @Override
    public void setCenter(final double x, final double z)
    {
        this.center = new Location(world, x, 0, z);
    }

    @Override
    public void setCenter(final Location center)
    {
        setCenter(center.getX(), center.getZ());
    }

    @Override
    public double getDamageBuffer() { return 0; }

    @Override
    public void setDamageBuffer(final double distance) {}

    @Override
    public double getDamageAmount() { return 0; }

    @Override
    public void setDamageAmount(final double damageAmount) {}

    @Override
    public int getWarningTime() { return 0; }

    @Override
    public void setWarningTime(final int seconds) {}

    @Override
    public int getWarningDistance() { return 0; }

    @Override
    public void setWarningDistance(final int blocks) {}

    @Override
    public MapShape getShape()
    {
        return shape;
    }

    @Override
    public void setShape(final MapShape shape)
    {
        this.shape = shape;
    }

    @Override
    public boolean supportsProgressiveResize()
    {
        return false;
    }
}
