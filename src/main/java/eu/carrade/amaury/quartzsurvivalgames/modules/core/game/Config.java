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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.game;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import fr.zcraft.quartzlib.components.configuration.ConfigurationInstance;
import fr.zcraft.quartzlib.components.configuration.ConfigurationItem;
import fr.zcraft.quartzlib.components.configuration.ConfigurationSection;

import java.io.File;

import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.section;


public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    public static final ConfigurationItem<Integer> COUNTDOWN = item("countdown", 7);
    public static final ConfigurationItem<Boolean> STARTUP_TITLE = item("startup-title", true);

    public static final ConfigurationItem<Boolean> RANDOM_COLORS_IN_SOLO_GAMES = item("random-color-in-solo-games", true);

    public static final ConfigurationItem<Boolean> BROADCAST_PROGRESS = item("broadcast-progress", true);

    public static final SlowSection SLOW = section("slow", SlowSection.class);

    public static final class SlowSection extends ConfigurationSection
    {
        public final ConfigurationItem<Long> DELAY_BETWEEN_TP = item("delay-between-teleportations", 3L);
    }

    public static final BeginningSection BEGINNING = section("beginning", BeginningSection.class);

    public static final class BeginningSection extends ConfigurationSection
    {
        public final ConfigurationItem<TimeDelta> GRACE_PERIOD = item("grace-period", new TimeDelta(0, 0, 30));
        public final ConfigurationItem<Boolean> BROADCAST_GRACE_END = item("broadcast-grace-end", true);

        public final ConfigurationItem<TimeDelta> PEACE_PERIOD = item("peace-period", new TimeDelta(0, 1, 0));
        public final ConfigurationItem<TimeDelta> SURFACE_MOBS_FREE_PERIOD = item("surface-mobs-free-period", new TimeDelta(0, 20, 0));
    }

    public static final SidebarSection SIDEBAR = section("sidebar", SidebarSection.class);

    public static final class SidebarSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> PLAYERS = item("players", true);
        public final ConfigurationItem<Boolean> TEAMS = item("teams", true);
    }
}
