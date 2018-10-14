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
package eu.carrade.amaury.UHCReloaded.old.borders.worldborders;

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.borders.MapShape;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;


/**
 * Represents a Brettflan world border, from the WorldBorder Bukkit plugin.
 *
 * <p>These methods are not supported as the border cannot be bypassed and does not have
 * warnings:</p>
 * <ul>
 *     <li>{@link #setDamageBuffer(double)};</li>
 *     <li>{@link #setDamageAmount(double)};</li>
 *     <li>{@link #setWarningDistance(int)};</li>
 *     <li>{@link #setWarningTime(int)}.</li>
 * </ul>
 */
public class BrettflanWorldBorder extends WorldBorder
{
    private World world;
    private BorderData border;

    private Double diameter = 0d;

    private BukkitTask slowReductionTask = null;


    public BrettflanWorldBorder(World world)
    {
        this.world = world;

        com.wimbli.WorldBorder.WorldBorder wb = UHCReloaded.get().getWorldBorderIntegration().getWorldBorder();
        if (wb != null)
        {
            border = wb.getWorldBorder(world.getName());

            if (border == null)
            {
                border = new BorderData(world.getSpawnLocation().getX(), world.getSpawnLocation().getZ(), 3000000);
                Config.setBorder(world.getName(), border);
            }
        }
    }

    @Override
    public void init()
    {
        Config.setPortalRedirection(true);
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Override
    public double getDiameter()
    {
        // If squared, the size is not changed
        if (!border.getShape())
            return diameter;


        Double realDiameter = (double) (border.getRadiusX() * 2);

        // Returns the stored diameter, except if it was changed
        // manually with /wb (see #setDiameterInternal(Double) for
        // details).
        if (realDiameter - diameter >= 8)
            diameter = realDiameter;

        return diameter;
    }

    @Override
    public void setDiameter(double diameter)
    {
        setDiameterInternal(diameter);

        if (slowReductionTask != null)
        {
            slowReductionTask.cancel();
            slowReductionTask = null;
        }
    }

    @Override
    public void setDiameter(final double diameter, final long time)
    {
        // The behavior of the vanilla reduction is emulated.
        final double currentDiameter = getDiameter();

        final long ticksPerBlockRemoved = (int) Math.rint(time / (currentDiameter - diameter)) * 20L;
        final long movement = (diameter >= currentDiameter) ? 1 : -1;

        if (slowReductionTask != null)
        {
            slowReductionTask.cancel();
            slowReductionTask = null;
        }

        slowReductionTask = RunTask.timer(() -> {
            double newDiameter = getDiameter() + movement;

            // If the final size is achieved, we set the exact requested size and we stop here.
            // Calling setDiameter cancels this task.
            if ((movement < 0 && newDiameter <= diameter) || (movement > 0 && newDiameter >= diameter))
            {
                setDiameter(diameter);
            }
            else
            {
                setDiameterInternal(newDiameter);
            }
        }, ticksPerBlockRemoved, ticksPerBlockRemoved);
    }

    private void setDiameterInternal(double diameter)
    {
        this.diameter = diameter;

        // If the wall is circular, the diameter used to check must be bigger to avoid false positives
        // if a player is in an angle of the circular wall. The original diameter set is stored and
        // returned by the getDiameter value (except if the diameter was changed using /wb), for nicer
        // display (avoids rounding errors).
        // “+3” ? Experimental.
        int offset = (getShape() == MapShape.CIRCULAR) ? 3 : 0;

        border.setRadius((int) Math.floor((diameter + offset) / 2));
    }

    @Override
    public Location getCenter()
    {
        return new Location(world, border.getX(), 0, border.getZ());
    }

    @Override
    public void setCenter(double x, double z)
    {
        border.setX(x);
        border.setZ(z);
    }

    @Override
    public void setCenter(Location center)
    {
        setCenter(center.getX(), center.getZ());
    }

    @Override
    public double getDamageBuffer() { return 0; }

    @Override
    public void setDamageBuffer(double distance) {}

    @Override
    public double getDamageAmount() { return 0; }

    @Override
    public void setDamageAmount(double damageAmount) {}

    @Override
    public int getWarningTime() { return 0; }

    @Override
    public void setWarningTime(int seconds) {}

    @Override
    public int getWarningDistance() { return 0; }

    @Override
    public void setWarningDistance(int blocks) {}

    @Override
    public MapShape getShape()
    {
        return border.getShape() ? MapShape.CIRCULAR : MapShape.SQUARED;
    }

    @Override
    public void setShape(MapShape shape)
    {
        border.setShape(shape == MapShape.CIRCULAR);
    }
}
