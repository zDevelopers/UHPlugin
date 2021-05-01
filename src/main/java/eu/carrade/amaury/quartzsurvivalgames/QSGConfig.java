package eu.carrade.amaury.quartzsurvivalgames;

import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.map;
import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.section;

import fr.zcraft.quartzlib.components.configuration.Configuration;
import fr.zcraft.quartzlib.components.configuration.ConfigurationItem;
import fr.zcraft.quartzlib.components.configuration.ConfigurationMap;
import fr.zcraft.quartzlib.components.configuration.ConfigurationSection;
import java.util.Locale;
import org.bukkit.ChatColor;


public class QSGConfig extends Configuration {
    static public final ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public final ConfigurationItem<String> TITLE =
            item("title", ChatColor.WHITE + "" + ChatColor.BOLD + "Quartz" + ChatColor.RED + "" + ChatColor.BOLD + "SurvivalGames");

    static public final ConfigurationItem<Boolean> BUILT_IN_MODULES = item("built-in-modules", true);
    static public final ConfigurationMap<String, Boolean> MODULES = map("modules", String.class, Boolean.class);

    static public final WorldsSection WORLDS = section("worlds", WorldsSection.class);

    static public class WorldsSection extends ConfigurationSection {

        public final ConfigurationItem<String> OVERWORLD = item("overworld", "world");
        public final ConfigurationItem<String> NETHER = item("nether", "world_nether");
        public final ConfigurationItem<String> THE_END = item("the-end", "world_the_end");

    }
}
