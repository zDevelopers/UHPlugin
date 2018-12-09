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
package eu.carrade.amaury.UHCReloaded.modules.core.teams;

import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;

import java.io.File;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;


public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    public static final SidebarSection SIDEBAR = section("sidebar", SidebarSection.class);

    static public class SidebarSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);

        public final TitleSection TITLE = section("title", TitleSection.class);

        static public class TitleSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> COLOR = item("color", "");
            public final ConfigurationItem<Boolean> USE_TEAM_NAME = item("use-team-name", false);
        }

        public final ContentSection CONTENT = section("content", ContentSection.class);

        static public class ContentSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DISPLAY_HEARTS = item("display-hearts", true);
            public final ConfigurationItem<Boolean> COLOR_NAME = item("color-name", false);
            public final ConfigurationItem<Boolean> STRIKE_DEAD_PLAYERS = item("strike-dead-players", false);

            public final LoginStateSection LOGIN_STATE = section("login-state", LoginStateSection.class);

            static public class LoginStateSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> ITALIC = item("italic", true);
                public final ConfigurationItem<String> SUFFIX = item("suffix", "➥");
            }

            public final DisplayMetPlayersOnlySection DISPLAY_MET_PLAYERS_ONLY = section("display-met-players-only", DisplayMetPlayersOnlySection.class);

            static public class DisplayMetPlayersOnlySection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> ENABLED = item("enabled", false);
                public final ConfigurationItem<Double> DISPLAYED_WHEN_CLOSER_THAN = item("displayed-when-closer-than", 10d);
            }
        }
    }
}
