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

import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.Config;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.MapShape;
import org.bukkit.Location;
import org.bukkit.World;


/**
 * Represents a vanilla world border.
 *
 * <p>This border is always squared, so {@link #setShape(MapShape)} does nothing and {@link
 * #getShape()} always returns {@link MapShape#SQUARED}.</p>
 */
public class VanillaWorldBorder extends WorldBorder {
    private final World world;
    private final org.bukkit.WorldBorder border;

    public VanillaWorldBorder(final World world) {
        this.world = world;
        this.border = world.getWorldBorder();
    }

    @Override
    public void init() {
        setDamageBuffer(Config.DAMAGES_BUFFER.get());
        setDamageAmount(Config.DAMAGES_AMOUNT.get());
        setWarningDistance(Config.WARNING_DISTANCE.get());
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public double getDiameter() {
        return border.getSize();
    }

    @Override
    public void setDiameter(final double diameter) {
        border.setSize(diameter);
    }

    @Override
    public void setDiameter(final double diameter, final long time) {
        border.setSize(diameter, time);
    }

    @Override
    public Location getCenter() {
        return border.getCenter();
    }

    @Override
    public void setCenter(final Location center) {
        border.setCenter(center);
    }

    @Override
    public void setCenter(final double x, final double z) {
        border.setCenter(x, z);
    }

    @Override
    public double getDamageBuffer() {
        return border.getDamageBuffer();
    }

    @Override
    public void setDamageBuffer(final double distance) {
        border.setDamageBuffer(distance);
    }

    @Override
    public double getDamageAmount() {
        return border.getDamageAmount();
    }

    @Override
    public void setDamageAmount(final double damageAmount) {
        border.setDamageAmount(damageAmount);
    }

    @Override
    public int getWarningTime() {
        return border.getWarningTime();
    }

    @Override
    public void setWarningTime(final int seconds) {
        border.setWarningTime(seconds);
    }

    @Override
    public int getWarningDistance() {
        return border.getWarningDistance();
    }

    @Override
    public void setWarningDistance(final int blocks) {
        border.setWarningDistance(blocks);
    }

    @Override
    public MapShape getShape() {
        return MapShape.SQUARED;
    }

    @Override
    public void setShape(final MapShape shape) {
    }

    @Override
    public boolean supportsProgressiveResize() {
        return true;
    }
}
