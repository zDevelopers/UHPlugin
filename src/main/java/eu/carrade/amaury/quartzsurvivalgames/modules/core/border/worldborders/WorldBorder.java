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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.border.worldborders;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.MapShape;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import org.bukkit.Location;
import org.bukkit.World;


/**
 * An abstraction layer to manipulate world borders, typically the vanilla world border or
 * the Brettflan one.
 */
public abstract class WorldBorder {
    /**
     * Returns a new instance of a WorldBorder proxy using the requested types.
     *
     * @param motor The border motor; can be "vanilla" or "brettflan" (from config).
     * @param shape The border shape.
     * @return An instance of a WorldBorder proxy.
     */
    public static WorldBorder getInstance(final World world, final WorldBorderMotor motor, final MapShape shape) {
        // For circular shapes, the vanilla motor cannot be used.
        // Without the WorldBorder plugin, a fake world border is used (i.e., no border control).
        if (shape == MapShape.CIRCULAR) {
            if (QSG.module(BorderModule.class).getWorldBorderDependency().isEnabled()) {
                return new BrettflanWorldBorder(world);
            } else {
                return new FakeWorldBorder(world);
            }
        } else {
            if (motor == WorldBorderMotor.VANILLA ||
                    !QSG.module(BorderModule.class).getWorldBorderDependency().isEnabled()) {
                return new VanillaWorldBorder(world);
            } else {
                return new BrettflanWorldBorder(world);
            }
        }
    }

    /**
     * Initializes the world border configuration, if needed.
     * <p>
     * This method does not initializes the shape, size, etc.
     */
    public void init() {
    }

    /**
     * @return The world bordered by this world border.
     */
    public abstract World getWorld();

    /**
     * @return The diameter of the border.
     */
    public abstract double getDiameter();

    /**
     * @param diameter The new diameter of the border.
     */
    public abstract void setDiameter(final double diameter);

    /**
     * @param diameter The new diameter of the border.
     * @param time     The seconds used to change the size from the old size to the new one.
     */
    public abstract void setDiameter(final double diameter, final long time);

    /**
     * @param diameter The new diameter of the border.
     * @param time     The seconds used to change the size from the old size to the new one.
     */
    public void setDiameter(final double diameter, final TimeDelta time) {
        setDiameter(diameter, time.getSeconds());
    }

    /**
     * @return The center of the border.
     */
    public abstract Location getCenter();

    /**
     * @param center The new center of the border.
     */
    public abstract void setCenter(final Location center);

    /**
     * Sets the center of the border.
     *
     * @param x The x coordinate of the new center.
     * @param z The z coordinate of the new center.
     */
    public abstract void setCenter(final double x, final double z);

    /**
     * @return the amount of blocks a player may safely be outside the border before taking damage.
     */
    public abstract double getDamageBuffer();

    /**
     * @param distance the amount of blocks a player may safely be outside the border before taking
     *                 damage.
     */
    public abstract void setDamageBuffer(final double distance);

    /**
     * @return the amount of damage a player takes when outside the border plus the border buffer.
     */
    public abstract double getDamageAmount();

    /**
     * @param damageAmount the amount of damage a player takes when outside the border plus the
     *                     border buffer.
     */
    public abstract void setDamageAmount(final double damageAmount);

    /**
     * @return the warning time that causes the screen to be tinted red when a contracting border
     * will reach the player within the specified time, if applicable.
     */
    public abstract int getWarningTime();

    /**
     * @param seconds the warning time that causes the screen to be tinted red when a contracting
     *                border will reach the player within the specified time.
     */
    public abstract void setWarningTime(final int seconds);

    /**
     * @return the warning distance that causes the screen to be tinted red when the player is
     * within the specified number of blocks from the border.
     */
    public abstract int getWarningDistance();

    /**
     * @param blocks the warning distance that causes the screen to be tinted red when the player is
     *               within the specified number of blocks from the border.
     */
    public abstract void setWarningDistance(final int blocks);

    /**
     * @return The current border shape.
     */
    public abstract MapShape getShape();

    /**
     * @param shape the new border shape.
     */
    public abstract void setShape(final MapShape shape);

    /**
     * @return {@code true} if this border supports progressive resizes using {@link #setDiameter(double, long)}.
     */
    public abstract boolean supportsProgressiveResize();

    public enum WorldBorderMotor {
        /**
         * Uses the vanilla world border (for squared borders only).
         */
        VANILLA,

        /**
         * Uses the Brettflan's WorldBorder plugin (for both squared and circular).
         * <p>
         * If set for squared world borders and WorldBorder is not installed, fallbacks to vanilla.
         */
        BRETTFLAN
    }
}
