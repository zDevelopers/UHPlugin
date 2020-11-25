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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.teams;

import fr.zcraft.quartzlib.components.configuration.ConfigurationInstance;
import fr.zcraft.quartzlib.components.configuration.ConfigurationItem;
import fr.zcraft.quartzlib.components.configuration.ConfigurationSection;
import fr.zcraft.zteams.guis.TeamsGUIItemType;

import java.io.File;

import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.section;


public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    static public final ConfigurationItem<Boolean> CAN_SEE_FRIENDLY_INVISIBLES = item("can-see-friendly-invisibles", true);
    static public final ConfigurationItem<Boolean> ALLOW_FRIENDLY_FIRE = item("allow-friendly-fire", true);
    static public final ConfigurationItem<Integer> MAX_PLAYERS_PER_TEAM = item("max-players-per-team", 0);

    static public final ConfigurationItem<Boolean> COLORIZE_CHAT = item("colorize-chat", true);

    static public final BannerSection BANNER = section("banner", BannerSection.class);

    static public class BannerSection extends ConfigurationSection
    {
        public final ShapeSection SHAPE = section("shape", ShapeSection.class);

        static public class ShapeSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> WRITE_LETTER = item("write-letter", true);
            public final ConfigurationItem<Boolean> ADD_BORDER = item("add-border", true);
        }

        public final GiveSection GIVE = section("give", GiveSection.class);

        static public class GiveSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> PLACE_ON_SPAWN = item("place-on-spawn", true);
            public final ConfigurationItem<Boolean> GIVE_IN_HOTBAR = item("give-in-hotbar", false);
            public final ConfigurationItem<Boolean> GIVE_ON_HEAD = item("give-on-head", false);
        }

        public final ShieldSection SHIELDS = section("shields", ShieldSection.class);

        static public class ShieldSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ADD_ON_SHIELDS = item("add-on-shields", true);
        }
    }

    static public final ChestGuiSection GUI = section("gui", ChestGuiSection.class);

    static public class ChestGuiSection extends ConfigurationSection
    {
        public final DisplaySection DISPLAY = section("display", DisplaySection.class);

        static public class DisplaySection extends ConfigurationSection
        {
            public final ConfigurationItem<TeamsGUIItemType> TEAM_ITEM = item("team-item", TeamsGUIItemType.BANNER);
            public final ConfigurationItem<Boolean> GLOW_ON_SELECTED_TEAM = item("glow-on-selected-team", true);
        }
    }

    static public final TeamChatSection TEAM_CHAT = section("team-chat", TeamChatSection.class);

    static public class TeamChatSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DISABLE_LOCK_ON_DEATH = item("disable-lock-on-death", true);
        public final ConfigurationItem<Boolean> LOG = item("log", false);
    }

    static public final SidebarSection SIDEBAR = section("sidebar", SidebarSection.class);

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
