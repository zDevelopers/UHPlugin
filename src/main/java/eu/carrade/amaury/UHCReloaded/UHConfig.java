package eu.carrade.amaury.UHCReloaded;

import fr.zcraft.zlib.components.configuration.*;
import org.bukkit.ChatColor;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.*;


public class UHConfig extends Configuration
{
    static public final ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public final ConfigurationItem<String> TITLE = item("title", ChatColor.GREEN + "" + ChatColor.BOLD + "UHC Reloaded");

    static public final ConfigurationMap<String, Boolean> MODULES = map("modules", String.class, Boolean.class);

    static public final WorldsSection WORLDS = section("worlds", WorldsSection.class);
    static public class WorldsSection extends ConfigurationSection
    {

        public final ConfigurationItem<String> OVERWORLD = item("overworld", "world");
        public final ConfigurationItem<String> NETHER = item("nether", "world_nether");
        public final ConfigurationItem<String> THE_END = item("the-end", "world_the_end");

    }

    static public final PlayersListSection PLAYERS_LIST = section("playersList", PlayersListSection.class);

    static public class PlayersListSection extends ConfigurationSection
    {
        public final WaitingTimeSection WAITING_TIME = section("waitingTime", WaitingTimeSection.class);

        static public class WaitingTimeSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> HEADER = item("header", "{title}");
            public final ConfigurationItem<String> FOOTER = item("footer", "");
        }

        public final InGameTimeSection IN_GAME_TIME = section("inGameTime", InGameTimeSection.class);

        static public class InGameTimeSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> HEADER = item("header", "{title}");
            public final ConfigurationItem<String> FOOTER = item("footer", "§a{episodeText} §7- §a{playersText} §7- §a{teamsText}");
        }
    }

    static public final FinishSection FINISH = section("finish", FinishSection.class);

    static public class FinishSection extends ConfigurationSection
    {
        public final AutoSection AUTO = section("auto", AutoSection.class);

        static public class AutoSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DO = item("do", true);
            public final ConfigurationItem<Integer> TIME_AFTER_LAST_DEATH = item("timeAfterLastDeath", 3);
        }

        public final ConfigurationItem<Boolean> MESSAGE = item("message", true);
        public final ConfigurationItem<Boolean> TITLE = item("title", true);

        public final FireworksSection FIREWORKS = section("fireworks", FireworksSection.class);

        static public class FireworksSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
            public final ConfigurationItem<Long> DURATION = item("duration", 10l);
            public final ConfigurationItem<Double> AREA_SIZE = item("areaSize", 6d);
        }
    }

    static public final CommandsSection COMMANDS = section("commands", CommandsSection.class);

    static public class CommandsSection extends ConfigurationSection
    {
        public final ConfigurationList<String> EXECUTE_SERVER_START = list("execute-server-start", String.class);
        public final ConfigurationList<String> EXECUTE_START = list("execute-start", String.class);
        public final ConfigurationList<String> EXECUTE_END = list("execute-end", String.class);
    }
}
