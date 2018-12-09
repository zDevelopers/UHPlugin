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
package eu.carrade.amaury.UHCReloaded.modules.core.border;

import eu.carrade.amaury.UHCReloaded.modules.core.border.worldborders.WorldBorder;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;

import java.io.File;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;


public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    public static final ConfigurationItem<Integer> SIZE = item("size", 2000);
    public static final ConfigurationItem<MapShape> SHAPE = item("shape", MapShape.SQUARED);

    public static final ConfigurationItem<WorldBorder.WorldBorderMotor> MOTOR = item("motor", WorldBorder.WorldBorderMotor.VANILLA);
    public static final ConfigurationItem<Double> DAMAGES_BUFFER = item("damages-buffer", 5d);
    public static final ConfigurationItem<Double> DAMAGES_AMOUNT = item("damages-amount", 0.2);
    public static final ConfigurationItem<Integer> WARNING_DISTANCE = item("warning-distance", 5);

    public static final ShrinkingSection SHRINKING = section("shrinking", ShrinkingSection.class);

    static public class ShrinkingSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", false);
        public final ConfigurationItem<TimeDelta> STARTS_AFTER = item("starts-after", new TimeDelta(1, 30, 0));
        public final ConfigurationItem<TimeDelta> SHRINKS_DURING = item("shrinks-during", new TimeDelta(2, 0, 0));
        public final ConfigurationItem<Integer> DIAMETER_AFTER_SHRINK = item("diameter-after-shrink", 200);
    }

    static public final BorderSection SIDEBAR = section("sidebar", BorderSection.class);

    static public class BorderSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DISPLAYED = item("displayed", true);
        public final ConfigurationItem<Boolean> DISPLAY_DIAMETER = item("display-diameter", false);
    }
}
