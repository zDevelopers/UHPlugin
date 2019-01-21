package eu.carrade.amaury.UHCReloaded;

import fr.zcraft.zlib.components.configuration.*;
import org.bukkit.ChatColor;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.*;


public class UHConfig extends Configuration
{
    static public final ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public final ConfigurationItem<String> TITLE = item("title", ChatColor.GREEN + "" + ChatColor.BOLD + "UHC Reloaded");

    static public final ConfigurationItem<Boolean> BUILT_IN_MODULES = item("built-in-modules", true);
    static public final ConfigurationMap<String, Boolean> MODULES = map("modules", String.class, Boolean.class);

    static public final WorldsSection WORLDS = section("worlds", WorldsSection.class);
    static public class WorldsSection extends ConfigurationSection
    {

        public final ConfigurationItem<String> OVERWORLD = item("overworld", "world");
        public final ConfigurationItem<String> NETHER = item("nether", "world_nether");
        public final ConfigurationItem<String> THE_END = item("the-end", "world_the_end");

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
}
